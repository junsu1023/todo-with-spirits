using System;
using System.IO;
using System.Linq;
using NUnit.Framework;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class ForestSessionTests
    {
        private sealed class MemorySave : ISpiritSaveRepository
        {
            public string Json; public bool Fail;
            public string SavePath => "forest-test-memory";
            public PrototypeSaveData Load()=>Json==null?new PrototypeSaveData():JsonUtility.FromJson<PrototypeSaveData>(Json);
            public void Save(PrototypeSaveData save){if(Fail)throw new IOException("disk full");Json=JsonUtility.ToJson(save);}
        }
        private PrototypeApplicationService Create(MemorySave save)
        {
            var app=new PrototypeApplicationService(new EmptyTodoCompletionSource(),save,new TaskClassifier(),new SpiritDayGenerator(),new DateTime(2026,1,25),true);
            app.Initialize();if(!app.SaveData.Forest.Enabled)app.EnableForest();return app;
        }
        [Test] public void StartEmptyAndToggleNeverFarmsRewardsAndPersistsCancellation()
        {
            var save=new MemorySave();var app=Create(save);Assert.That(app.CurrentCompletedTasks.Count,Is.Zero);
            string id=app.AddForestTodo("작은 약속",UserTaskCategory.Unspecified,true);
            app.ToggleForestTodo(id);int reward=app.EssenceWallet.Balance;Assert.That(reward,Is.GreaterThan(0));
            app.ToggleForestTodo(id);app.ToggleForestTodo(id);Assert.That(app.EssenceWallet.Balance,Is.EqualTo(reward));
            app.ToggleForestTodo(id);app=Create(save);Assert.That(app.CurrentCompletedTasks.Count,Is.Zero);
            Assert.That(app.ForestActiveDays,Is.Zero);
        }
        [Test] public void UnspecifiedDoesNotInventLifestyleButGrantsGrowth()
        {
            var app=Create(new MemorySave());string id=app.AddForestTodo("휴식 공부 산책",UserTaskCategory.Unspecified,true);app.ToggleForestTodo(id);
            Assert.That(app.CurrentClassifications[0].ActionTags,Is.Empty);
            Assert.That(app.ForestDayReflection(),Does.Contain("미정"));app.AdvanceLifeDay();
            Assert.That(app.CurrentLife.ActivityDays,Is.EqualTo(1));Assert.That(app.CurrentLife.Actions,Is.Empty);
        }
        [Test] public void HabitRecursUncheckedAndAbsenceDoesNotFabricateActivity()
        {
            var app=Create(new MemorySave());string id=app.AddForestTodo("산책",UserTaskCategory.Health,true);app.ToggleForestTodo(id);
            app.SynchronizeLifeDate(new DateTime(2026,2,12));Assert.That(app.TodayTodos.Count,Is.EqualTo(1));
            Assert.That(app.CurrentCompletedTasks.Count,Is.Zero);Assert.That(app.CurrentLife.ActivityDays,Is.EqualTo(1));
            Assert.That(app.MonthlyTrace("2026-01",app.CurrentLife.SpiritId),Does.Contain("건강 1회"));
            Assert.That(app.MonthlyTrace("2026-02",app.CurrentLife.SpiritId),Does.Contain("0일"));
        }
        [Test] public void MixedUnspecifiedTaskDoesNotBecomeEvidenceForKnownCategory()
        {
            var app=Create(new MemorySave());
            app.ToggleForestTodo(app.AddForestTodo("저녁 산책",UserTaskCategory.Health,false));
            app.ToggleForestTodo(app.AddForestTodo("이름 없는 약속",UserTaskCategory.Unspecified,false));
            Assert.That(app.ForestDayReflection(),Does.Contain("건강").And.Contain("저녁 산책").And.Not.Contain("이름 없는 약속"));
        }
        [Test] public void SaveFailureRollsBackTodoAndRewardTogether()
        {
            var save=new MemorySave();var app=Create(save);string id=app.AddForestTodo("읽기",UserTaskCategory.WorkStudy,false);
            save.Fail=true;Assert.Throws<IOException>(()=>app.ToggleForestTodo(id));
            Assert.That(app.CurrentCompletedTasks.Count,Is.Zero);Assert.That(app.EssenceWallet.Balance,Is.Zero);
            Assert.That(app.TodayTodos[0].CompletedDates,Is.Empty);
        }
        [Test] public void EditingCompletedTodoUpdatesTodaysRecordWithoutDuplicatingCompletionOrReward()
        {
            var save = new MemorySave(); var app = Create(save);
            string id = app.AddForestTodo("아침 산책", UserTaskCategory.Health, false);
            app.ToggleForestTodo(id);
            int reward = app.EssenceWallet.Balance;

            app.EditForestTodo(id, "  책 읽기  ", UserTaskCategory.WorkStudy, true);
            app.EditForestTodo(id, "책 읽기", UserTaskCategory.Unspecified, true);
            app = Create(save);

            Assert.That(app.TodayTodos.Count, Is.EqualTo(1));
            Assert.That(app.CurrentCompletedTasks.Count, Is.EqualTo(1));
            Assert.That(app.CurrentCompletedTasks[0].TaskId, Is.EqualTo(id));
            Assert.That(app.CurrentCompletedTasks[0].Title, Is.EqualTo("책 읽기"));
            Assert.That(app.CurrentCompletedTasks[0].UserCategory, Is.EqualTo(UserTaskCategory.Unspecified));
            Assert.That(app.CurrentCompletedTasks[0].RoutineId, Is.EqualTo(id));
            Assert.That(app.CurrentClassifications[0].ActionTags, Is.Empty);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(reward));
            Assert.That(app.TodayTodos[0].CompletedDates, Has.Count.EqualTo(1));
            Assert.That(app.SaveData.Records.FindAll(r => r.Date == app.CurrentDateKey && r.SpiritId == app.CurrentLife.SpiritId), Has.Count.EqualTo(1));
        }
        [Test] public void EditingEarlierHabitIntoTodayTaskPreservesPreviousRecordAndRemainsVisibleToday()
        {
            var save = new MemorySave(); var app = Create(save);
            string id = app.AddForestTodo("아침 산책", UserTaskCategory.Health, true);
            app.ToggleForestTodo(id);
            string yesterday = app.CurrentDateKey;
            app.AdvanceLifeDay();
            string previousRecord = JsonUtility.ToJson(app.SaveData.Records.Find(r => r.Date == yesterday));
            app.ToggleForestTodo(id);
            app.EditForestTodo(id, "그림 그리기", UserTaskCategory.Hobby, false);
            app = Create(save);

            Assert.That(app.TodayTodos.Count, Is.EqualTo(1));
            Assert.That(app.TodayTodos[0].CreatedOn, Is.EqualTo(app.CurrentDateKey));
            Assert.That(app.TodayTodos[0].Habit, Is.False);
            CollectionAssert.Contains(app.TodayTodos[0].CompletedDates, yesterday);
            Assert.That(app.CurrentCompletedTasks[0].RoutineId, Is.Empty);
            Assert.That(JsonUtility.ToJson(app.SaveData.Records.Find(r => r.Date == yesterday)), Is.EqualTo(previousRecord));
            Assert.That(app.MonthlyTrace("2026-01", app.CurrentLife.SpiritId), Does.Contain("건강 1회").And.Contain("취미 1회"));

            app.AdvanceLifeDay();
            Assert.That(app.TodayTodos, Is.Empty);
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
        }
        [Test] public void InvalidTodoEditsLeaveSavedAndActiveStateUnchanged()
        {
            var save = new MemorySave(); var app = Create(save);
            string id = app.AddForestTodo("책 읽기", UserTaskCategory.WorkStudy, false);
            app.ToggleForestTodo(id);
            string before = JsonUtility.ToJson(app.SaveData);
            string persisted = save.Json;

            foreach (string title in new[] { "", "   ", new string('가', 81), "책\n읽기", "\t책 읽기" })
                Assert.Throws<ArgumentException>(() => app.EditForestTodo(id, title, UserTaskCategory.Health, true));
            Assert.Throws<ArgumentException>(() => app.EditForestTodo(id, "산책", (UserTaskCategory)999, true));
            Assert.Throws<ArgumentException>(() => app.EditForestTodo(null, "산책", UserTaskCategory.Health, true));
            Assert.Throws<ArgumentException>(() => app.EditForestTodo("missing", "산책", UserTaskCategory.Health, true));

            Assert.That(JsonUtility.ToJson(app.SaveData), Is.EqualTo(before));
            Assert.That(save.Json, Is.EqualTo(persisted));
            Assert.That(app.CurrentCompletedTasks[0].Title, Is.EqualTo("책 읽기"));
        }
        [Test] public void FailedEditSaveRestoresTodoRecordRewardAndRecurringStartDate()
        {
            var save = new MemorySave(); var app = Create(save);
            string id = app.AddForestTodo("산책", UserTaskCategory.Health, true);
            app.ToggleForestTodo(id); app.AdvanceLifeDay(); app.ToggleForestTodo(id);
            string before = JsonUtility.ToJson(app.SaveData);
            string persisted = save.Json;
            int reward = app.EssenceWallet.Balance;
            save.Fail = true;

            Assert.Throws<IOException>(() => app.EditForestTodo(id, "책 읽기", UserTaskCategory.WorkStudy, false));

            Assert.That(JsonUtility.ToJson(app.SaveData), Is.EqualTo(before));
            Assert.That(save.Json, Is.EqualTo(persisted));
            Assert.That(app.CurrentCompletedTasks[0].Title, Is.EqualTo("산책"));
            Assert.That(app.TodayTodos[0].CreatedOn, Is.EqualTo("2026-01-25"));
            Assert.That(app.TodayTodos[0].Habit, Is.True);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(reward));
            save.Fail = false;
            Assert.That(JsonUtility.ToJson(Create(save).SaveData), Is.EqualTo(before));
        }
        [Test] public void TwentyOneActualDaysAdultChoiceFarewellAndReunionSurviveReload()
        {
            var save=new MemorySave();var app=Create(save);string id=app.AddForestTodo("운동",UserTaskCategory.Health,true);
            Assert.Throws<InvalidOperationException>(()=>app.ChooseForestStory(0));
            for(int day=0;day<21;day++){
                app.ToggleForestTodo(id);if(app.HasForestChoice)app.ChooseForestStory(app.CurrentLife.Story.Choices.Count==2?2:0);
                app.AdvanceLifeDay();
            }
            Assert.That(app.CurrentLife.Stage,Is.EqualTo(CompanionStage.Adult));
            Assert.That(app.CurrentLife.Farewell,Is.EqualTo(FarewellStep.Growing));
            Assert.That(app.CurrentLife.Route,Is.EqualTo(AdultRoute.Artisan));
            app.BeginFarewell();
            app.AdvanceLifeFarewell();app.AdvanceLifeFarewell();app.NameCurrentCompanion("솜이");app.ObserveNamedCompanion();app.LetCompanionLeave();
            string oldId=app.CurrentLife.SpiritId;string departureDay=app.CurrentDateKey;
            app.SelectNextEgg(2);Assert.That(app.CurrentDateKey,Is.EqualTo(departureDay),"An empty today can welcome the next spirit without skipping a day.");Assert.That(app.ReunionGuest,Is.Null);
            app.ToggleForestTodo(id);app.AdvanceLifeDay();app.ToggleForestTodo(id);Assert.That(app.ReunionGuest.SpiritId,Is.EqualTo(oldId));
            string letter=app.ClaimForestReunion();Assert.That(letter,Does.Contain("공예가").And.Contain("이웃"));
            app=Create(save);Assert.That(app.ReunionGuest,Is.Null);
            Assert.That(app.SaveData.Companions.Find(l=>l.SpiritId==oldId).Story.ReunionGift,Is.Not.Empty);
            Assert.Throws<InvalidOperationException>(()=>app.ClaimForestReunion());
        }
        [Test] public void ChoiceResultAndNextEpisodeDependOnPriorChoice()
        {
            var app=Create(new MemorySave());string id=app.AddForestTodo("할 일",UserTaskCategory.Unspecified,true);app.ToggleForestTodo(id);app.ChooseForestStory(1);
            Assert.That(ForestStoryRules.Question(app.CurrentLife),Does.Contain("바람 장식"));
            Assert.Throws<InvalidOperationException>(()=>app.ChooseForestStory(0));
            Assert.That(app.CurrentLife.Story.Choices.Count,Is.EqualTo(1));
        }
        [Test] public void NextEggCarriesOnlyTodaysUnfinishedLocalOneOffWithoutChangingCompletedHistory()
        {
            var save = new MemorySave(); var app = Create(save);
            string oldTask = app.AddForestTodo("지난날에 남겨 둔 일", UserTaskCategory.Living, false);
            app.AdvanceLifeDay();
            string carried = app.AddForestTodo("이어 할 책 읽기", UserTaskCategory.WorkStudy, false);
            string finished = app.AddForestTodo("오늘 마친 산책", UserTaskCategory.Health, false);
            string removed = app.AddForestTodo("지운 약속", UserTaskCategory.Unspecified, false);
            string habit = app.AddForestTodo("매일 물 마시기", UserTaskCategory.Health, true);
            app.ArchiveForestTodo(removed);
            app.ToggleForestTodo(finished); app.ToggleForestTodo(habit);
            string departingDate = app.CurrentDateKey, departingSpirit = app.CurrentLife.SpiritId;
            string oldRecord = JsonUtility.ToJson(app.CurrentRecord);
            var unchangedTasks = app.SaveData.Forest.Todos.Where(t => t.Id != carried).ToDictionary(t => t.Id, t => JsonUtility.ToJson(t));
            int balance = app.EssenceWallet.Balance;
            DepartForTaskTransfer(app);

            app.SelectNextEgg(1);
            app = Create(save);

            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-01-26"));
            CollectionAssert.AreEquivalent(new[] { carried, finished, habit }, app.TodayTodos.Select(t => t.Id));
            Assert.That(app.SaveData.Forest.Todos.Find(t => t.Id == carried).CreatedOn, Is.EqualTo(app.CurrentDateKey));
            Assert.That(app.SaveData.Forest.Todos.Find(t => t.Id == carried).CompletedDates, Is.Empty);
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            Assert.That(app.CurrentLife.ActivityDays, Is.Zero);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance));
            foreach (var task in unchangedTasks)
                Assert.That(JsonUtility.ToJson(app.SaveData.Forest.Todos.Find(t => t.Id == task.Key)), Is.EqualTo(task.Value));
            Assert.That(app.SaveData.Forest.Todos.Find(t => t.Id == oldTask).CreatedOn, Is.EqualTo("2026-01-25"));
            Assert.That(JsonUtility.ToJson(app.SaveData.Records.Find(r => r.Date == departingDate && r.SpiritId == departingSpirit)), Is.EqualTo(oldRecord));

            app.ToggleForestTodo(carried);
            Assert.That(app.CurrentCompletedTasks.Select(t => t.TaskId), Is.EquivalentTo(new[] { carried }));
            Assert.That(JsonUtility.ToJson(app.SaveData.Records.Find(r => r.Date == departingDate && r.SpiritId == departingSpirit)), Is.EqualTo(oldRecord));
        }
        [Test] public void ReturningToChooseNextEggCarriesUnfinishedTaskToActualReturnDateWithoutInventingCompletions()
        {
            var save = new MemorySave(); var app = Create(save);
            string carried = app.AddForestTodo("돌아와서 이어 할 일", UserTaskCategory.Hobby, false);
            string departingSpirit = app.CurrentLife.SpiritId;
            string oldRecord = JsonUtility.ToJson(app.CurrentRecord);
            DepartForTaskTransfer(app);

            app.SelectNextEgg(0, new DateTime(2026, 2, 12));
            app = Create(save);

            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-02-12"));
            Assert.That(app.TodayTodos.Single().Id, Is.EqualTo(carried));
            Assert.That(app.TodayTodos.Single().CreatedOn, Is.EqualTo("2026-02-12"));
            Assert.That(app.TodayTodos.Single().CompletedDates, Is.Empty);
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            Assert.That(app.EssenceWallet.Balance, Is.Zero);
            Assert.That(app.SaveData.Records, Has.Count.EqualTo(2));
            Assert.That(JsonUtility.ToJson(app.SaveData.Records.Find(r => r.SpiritId == departingSpirit)), Is.EqualTo(oldRecord));
        }
        [Test] public void NextEggKeepsRealHostDateAndSourceFactsWhileGrowthOwnershipStaysWithPreviousCompanion()
        {
            var save = new MemorySave(); var app = Create(save);
            var snapshot = Snapshot(1);
            snapshot.items.Add(new ForestTodoSnapshotItem { id = "host-one-off", title = "서버의 약속", category = "LIFE", isHabit = false, isCompleted = false });
            app.ApplyTodoSnapshot(snapshot);
            DepartForTaskTransfer(app);
            var originalTodos = app.SaveData.Forest.Todos.ToDictionary(t => t.Id, t => JsonUtility.ToJson(t));
            string oldRecord = JsonUtility.ToJson(app.CurrentRecord);
            string departingSpirit = app.CurrentLife.SpiritId;

            app.SelectNextEgg(2);
            app = Create(save);

            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-01-25"));
            foreach (var todo in originalTodos)
                Assert.That(JsonUtility.ToJson(app.SaveData.Forest.Todos.Single(t => t.Id == todo.Key)), Is.EqualTo(todo.Value));
            Assert.That(app.SaveData.Forest.Todos.Find(t => t.Id == "host-one-off").CreatedOn, Is.EqualTo("2026-01-25"));
            Assert.That(app.TodayTodos.Any(t => t.Id == "host-one-off"), Is.True);
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            Assert.That(JsonUtility.ToJson(app.SaveData.Records.Find(r => r.SpiritId == departingSpirit)), Is.EqualTo(oldRecord));

            var next = Snapshot(2, false); next.date = app.CurrentDateKey;
            next.items.Add(new ForestTodoSnapshotItem { id = "host-one-off", title = "서버의 약속", category = "LIFE", isHabit = false, isCompleted = false });
            Assert.That(app.ApplyTodoSnapshot(next), Is.True);
            Assert.That(app.TodayTodos.Any(t => t.Id == "host-one-off"), Is.True);
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            var corrected = app.SaveData.Records.Find(r => r.SpiritId == departingSpirit);
            Assert.That(corrected.CompletedTasks, Is.Empty, "Cancellation updates facts for their original owner.");
            Assert.That(JsonUtility.ToJson(corrected.SpiritDay), Is.EqualTo(JsonUtility.ToJson(JsonUtility.FromJson<DailyCompanionRecord>(oldRecord).SpiritDay)),
                "The previously observed scene remains unchanged.");
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(10));
        }
        [Test] public void FailedNextEggSaveRollsBackTaskTransferDateAndCompanionTogether()
        {
            var save = new MemorySave(); var app = Create(save);
            string carried = app.AddForestTodo("이어 할 약속", UserTaskCategory.Living, false);
            app.ToggleForestTodo(app.AddForestTodo("이미 마친 약속", UserTaskCategory.Health, false));
            DepartForTaskTransfer(app);
            string before = JsonUtility.ToJson(app.SaveData), persisted = save.Json;
            string departingSpirit = app.CurrentLife.SpiritId;
            save.Fail = true;

            Assert.Throws<IOException>(() => app.SelectNextEgg(0));

            Assert.That(JsonUtility.ToJson(app.SaveData), Is.EqualTo(before));
            Assert.That(save.Json, Is.EqualTo(persisted));
            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-01-25"));
            Assert.That(app.CurrentLife.SpiritId, Is.EqualTo(departingSpirit));
            Assert.That(app.SaveData.Forest.Todos.Find(t => t.Id == carried).CreatedOn, Is.EqualTo("2026-01-25"));
            save.Fail = false;
            app.SelectNextEgg(0);
            Assert.That(app.SaveData.Companions, Has.Count.EqualTo(2));
            Assert.That(app.TodayTodos.Single(t => !t.CompletedDates.Contains(app.CurrentDateKey)).Id, Is.EqualTo(carried));
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
        }
        private static void DepartForTaskTransfer(PrototypeApplicationService app)
        {
            // Growth and farewell progression have a full-cycle test above. Isolate the task
            // handoff here so completed and unfinished tasks can share the departure date.
            app.CurrentLife.Stage = CompanionStage.Adult;
            app.CurrentLife.Farewell = FarewellStep.ReadyToLeave;
            app.CurrentLife.Name = "솜이";
            app.LetCompanionLeave();
        }
        private ForestTodoSnapshot Snapshot(long revision, bool complete=true) => new ForestTodoSnapshot {
            schemaVersion=1,accountId="test-account",revision=revision,date="2026-01-25",
            items=new System.Collections.Generic.List<ForestTodoSnapshotItem>{new ForestTodoSnapshotItem{id="habit-1",title="운동",category="HEALTH",isHabit=true,isCompleted=complete}}
        };
        [Test] public void HostRevisionReplayCancellationAndReloadAreIdempotent()
        {
            var save=new MemorySave();var app=Create(save);Assert.That(app.ApplyTodoSnapshot(Snapshot(1)),Is.True);
            int balance=app.EssenceWallet.Balance;Assert.That(app.ApplyTodoSnapshot(Snapshot(1)),Is.False);
            app.ApplyTodoSnapshot(Snapshot(2,false));Assert.That(app.CurrentCompletedTasks,Is.Empty);
            app=Create(save);app.ApplyTodoSnapshot(Snapshot(3));Assert.That(app.EssenceWallet.Balance,Is.EqualTo(balance));
            Assert.Throws<InvalidOperationException>(()=>app.ToggleForestTodo("habit-1"));
        }
        [Test] public void HostRejectsCrossAccountUnknownCategoryAndWrongDateWithoutMutation()
        {
            var app=Create(new MemorySave());app.ApplyTodoSnapshot(Snapshot(1));string before=JsonUtility.ToJson(app.SaveData);
            var wrong=Snapshot(2);wrong.accountId="other";Assert.Throws<InvalidOperationException>(()=>app.ApplyTodoSnapshot(wrong));
            wrong=Snapshot(2);wrong.items[0].category="UNKNOWN";Assert.Throws<ArgumentException>(()=>app.ApplyTodoSnapshot(wrong));
            wrong=Snapshot(2);wrong.date="2026-01-24";Assert.Throws<ArgumentException>(()=>app.ApplyTodoSnapshot(wrong));
            Assert.That(JsonUtility.ToJson(app.SaveData),Is.EqualTo(before));
        }
        [Test] public void HostRejectsLocalJournalOverwriteAndRollsBackFailedSave()
        {
            var save=new MemorySave();var app=Create(save);save.Fail=true;Assert.Throws<IOException>(()=>app.ApplyTodoSnapshot(Snapshot(1)));
            Assert.That(app.IsHostConnected,Is.False);Assert.That(app.EssenceWallet.Balance,Is.Zero);
            save.Fail=false;app.AddForestTodo("개인 기록",UserTaskCategory.Unspecified,false);
            Assert.Throws<InvalidOperationException>(()=>app.ApplyTodoSnapshot(Snapshot(2)));
        }
    }
}
