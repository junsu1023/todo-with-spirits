using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using NUnit.Framework;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class ForestDailyExperienceTests
    {
        private sealed class MemorySave : ISpiritSaveRepository
        {
            public string Json;
            public bool Fail;
            public string SavePath => "daily-experience-tests";
            public PrototypeSaveData Load() => Json == null ? new PrototypeSaveData() : JsonUtility.FromJson<PrototypeSaveData>(Json);
            public void Save(PrototypeSaveData data) { if (Fail) throw new IOException("disk unavailable"); Json = JsonUtility.ToJson(data); }
        }
        private static PrototypeApplicationService App(MemorySave save = null)
        {
            var app = new PrototypeApplicationService(new EmptyTodoCompletionSource(), save ?? new MemorySave(),
                new TaskClassifier(), new SpiritDayGenerator(), new DateTime(2026, 1, 25), true);
            app.Initialize(); if (!app.SaveData.Forest.Enabled) app.EnableForest(); return app;
        }
        private static ForestTodoSnapshot Snapshot(long revision, params ForestTodoSnapshotItem[] items) => new ForestTodoSnapshot {
            schemaVersion = 1, accountId = "daily-user", revision = revision, date = "2026-01-25", items = items.ToList()
        };
        private static ForestTodoSnapshotItem Item(string id, string category, bool done, string title = null) => new ForestTodoSnapshotItem {
            id = id, title = title ?? id, category = category, isHabit = true, isCompleted = done
        };

        [Test] public void AwaitingErrorOfflineAndConfirmedEmptyAreDifferentAndNeverInventRewards()
        {
            var app = App();
            Assert.That(app.GetForestTodaySummary().SyncState, Is.EqualTo(ForestSyncState.Awaiting));
            Assert.That(app.GetForestTodaySummary().HasConfirmedData, Is.False);
            Assert.That(app.GetForestTodaySummary().Activities, Is.Empty);
            app.SetForestSyncState(ForestSyncState.Error);
            Assert.That(app.GetForestTodaySummary().SyncState, Is.EqualTo(ForestSyncState.Error));
            app.SetForestSyncState(ForestSyncState.Offline);
            Assert.That(app.GetForestTodaySummary().SyncState, Is.EqualTo(ForestSyncState.Offline));
            app.ApplyTodoSnapshot(Snapshot(1));
            var summary = app.GetForestTodaySummary();
            Assert.That(summary.SyncState, Is.EqualTo(ForestSyncState.Confirmed));
            Assert.That(summary.HasConfirmedData, Is.True);
            Assert.That(summary.Activities.Single().IsAutonomous, Is.True);
            Assert.That(summary.Activities.Single().IsCompleted, Is.False);
            Assert.That(summary.TotalEssence, Is.Zero);
            Assert.That(app.EssenceWallet.Balance, Is.Zero);
        }

        [Test] public void BoundAccountOfflineVisitStaysAcknowledgedWhenFirstSnapshotFinallyArrives()
        {
            var save = new MemorySave(); var app = App(save); app.BindForestHostAccount("daily-user");
            app.SetForestSyncState(ForestSyncState.Offline);
            var offline = app.GetForestTodaySummary();
            Assert.That(offline.AccountId, Is.EqualTo("daily-user"));
            Assert.That(offline.HasConfirmedData, Is.False);
            app.AcknowledgeForestToday(offline);
            app.ApplyTodoSnapshot(Snapshot(1, Item("read", "WORK_STUDY", true)));
            app = App(save);
            Assert.That(app.GetForestTodaySummary().IsFirstVisit, Is.False);
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.EqualTo(10));
            Assert.That(app.SaveData.Forest.Days.Select(d => d.AccountId), Is.EquivalentTo(new[] { "daily-user" }));
            Assert.Throws<InvalidOperationException>(() => app.BindForestHostAccount("other-user"));
        }

        [Test] public void FiftyOriginalTitlesArePreservedAndUnfinishedTasksNeverBecomeDoneActivities()
        {
            var app = App();
            var rows = Enumerable.Range(0, 50).Select(i => Item(i.ToString(), i < 25 ? "WORK_STUDY" : "HEALTH", i < 25,
                i == 0 ? new string('가', 255) : "원래 약속 " + i)).ToArray();
            app.ApplyTodoSnapshot(Snapshot(1, rows));
            var summary = app.GetForestTodaySummary();
            Assert.That(summary.Todos, Has.Count.EqualTo(50));
            Assert.That(summary.Todos[0].Title, Has.Length.EqualTo(255));
            Assert.That(summary.Todos.Where(t => !t.IsCompleted).All(t => t.ActivityKey == "walk"), Is.True);
            Assert.That(summary.Activities, Has.Count.LessThanOrEqualTo(3));
            foreach (var activity in summary.Activities)
                Assert.That(summary.Todos.Where(t => activity.SourceTodoIds.Contains(t.Id)).All(t => t.IsCompleted == activity.IsCompleted), Is.True);
            Assert.That(summary.TotalEssence, Is.EqualTo(25));
            Assert.That(app.CurrentRecord.CandidateActions.All(a => a.TaskScore <= 48f), Is.True,
                "50 same-category rows retain facts but contribute at most twice to action scoring.");
        }

        [Test] public void UnspecifiedKeepsItsCategoryAndDoesNotInventRestOrStudy()
        {
            var app = App(); app.ApplyTodoSnapshot(Snapshot(1, Item("none", "NONE", true, "공부 산책 휴식")));
            var todo = app.GetForestTodaySummary().Todos.Single();
            Assert.That(todo.Category, Is.EqualTo(UserTaskCategory.Unspecified));
            Assert.That(todo.ActivityKey, Is.EqualTo("companion"));
            Assert.That(todo.Reason, Does.Contain("미정"));
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.EqualTo(10));
            Assert.That(app.CurrentClassifications.Single().ActionTags, Is.Empty);
        }

        [Test] public void DismissingAnOlderOpenSummaryDoesNotAcknowledgeUnseenRewardDelta()
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("read", "WORK_STUDY", true)));
            var displayed = app.GetForestTodaySummary();
            app.ApplyTodoSnapshot(Snapshot(2, Item("read", "WORK_STUDY", true), Item("walk", "HEALTH", true)));
            app.AcknowledgeForestToday(displayed);
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.EqualTo(8));
            Assert.That(app.GetForestTodaySummary().IsFirstVisit, Is.False);
            app = App(save);
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.EqualTo(8));
            app.AcknowledgeForestToday();
            app.ApplyTodoSnapshot(Snapshot(3));
            app.ApplyTodoSnapshot(Snapshot(4, Item("read", "WORK_STUDY", true), Item("walk", "HEALTH", true)));
            Assert.That(app.GetForestTodaySummary().TotalEssence, Is.EqualTo(18));
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.Zero);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(18));
        }

        [Test] public void FailedAcknowledgeAndFailedSnapshotLeaveVisitAndWalletUncommitted()
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("read", "WORK_STUDY", true)));
            string previous = save.Json;
            save.Fail = true;
            Assert.Throws<IOException>(() => app.AcknowledgeForestToday());
            Assert.That(app.GetForestTodaySummary().IsFirstVisit, Is.True);
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.EqualTo(10));
            Assert.Throws<IOException>(() => app.ApplyTodoSnapshot(Snapshot(2, Item("read", "WORK_STUDY", true), Item("walk", "HEALTH", true))));
            Assert.That(app.GetForestTodaySummary().TotalEssence, Is.EqualTo(10));
            Assert.That(save.Json, Is.EqualTo(previous));
        }

        [Test] public void MidnightDoesNotCarryYesterdayHabitOrAcknowledgementIntoConfirmedToday()
        {
            var app = App(); app.ApplyTodoSnapshot(Snapshot(1, Item("walk", "HEALTH", true)));
            var old = app.GetForestTodaySummary(); app.AcknowledgeForestToday(old);
            app.SynchronizeLifeDate(new DateTime(2026, 1, 26));
            var today = app.GetForestTodaySummary();
            Assert.That(today.IsFirstVisit, Is.True);
            Assert.That(today.HasConfirmedData, Is.False);
            Assert.That(today.Todos, Is.Empty);
            Assert.That(today.SyncState, Is.EqualTo(ForestSyncState.Awaiting));
            Assert.Throws<InvalidOperationException>(() => app.AcknowledgeForestToday(old));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(10));
        }

        [Test] public void FailedConnectionUsesCachedFactsWithoutClaimingLatestAndNotifiesObservers()
        {
            var save = new MemorySave(); var app = App(save); app.ApplyTodoSnapshot(Snapshot(1, Item("read", "WORK_STUDY", true)));
            app = App(save); int notifications = 0; app.ForestTodayChanged += () => notifications++;
            app.SetForestSyncState(ForestSyncState.Offline);
            var summary = app.GetForestTodaySummary();
            Assert.That(summary.Todos, Has.Count.EqualTo(1));
            Assert.That(summary.HasConfirmedData, Is.True);
            Assert.That(summary.SyncState, Is.EqualTo(ForestSyncState.Offline));
            Assert.That(summary.LastConfirmedAtUtc, Is.Not.Empty);
            Assert.That(app.NextForestDialogue(), Does.Contain("아직"));
            Assert.That(notifications, Is.EqualTo(1));
        }

        [TestCase("WORK_STUDY", ForestStoryFamily.Focus)]
        [TestCase("HEALTH", ForestStoryFamily.Movement)]
        [TestCase("NONE", ForestStoryFamily.Everyday)]
        public void StoryFamilyAndChoiceSurviveCorrectionAndReload(string category, ForestStoryFamily expected)
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("task", category, true)));
            Assert.That(app.CurrentLife.Story.Family, Is.EqualTo(expected));
            string question = ForestStoryRules.Question(app.CurrentLife);
            app.ChooseForestStory(0); string prop = ForestStoryRules.VisiblePropName(app.CurrentLife);
            string choices = JsonUtility.ToJson(app.CurrentLife.Story);
            app.ApplyTodoSnapshot(Snapshot(2, Item("changed", "HOBBY", true)));
            app = App(save);
            Assert.That(app.CurrentLife.Story.Family, Is.EqualTo(expected));
            Assert.That(JsonUtility.ToJson(app.CurrentLife.Story), Is.EqualTo(choices));
            Assert.That(ForestStoryRules.Question(app.CurrentLife), Is.Not.EqualTo(question));
            Assert.That(ForestStoryRules.ContinuingDialogue(app.CurrentLife), Does.Contain(prop));
        }

        [Test] public void EachInputFamilyOffersDifferentImmediateAndNextVisitResultsForBothChoices()
        {
            foreach (string category in new[] { "WORK_STUDY", "HEALTH", "NONE" }) {
                var a = App(); var b = App();
                a.ApplyTodoSnapshot(Snapshot(1, Item("task", category, true)));
                b.ApplyTodoSnapshot(Snapshot(1, Item("task", category, true)));
                Assert.That(a.ChooseForestStory(0), Is.Not.EqualTo(b.ChooseForestStory(1)));
                Assert.That(ForestStoryRules.VisiblePropName(a.CurrentLife), Is.Not.EqualTo(ForestStoryRules.VisiblePropName(b.CurrentLife)));
                Assert.That(ForestStoryRules.Question(a.CurrentLife), Is.Not.EqualTo(ForestStoryRules.Question(b.CurrentLife)));
                Assert.That(ForestStoryRules.ContinuingDialogue(a.CurrentLife), Is.Not.EqualTo(ForestStoryRules.ContinuingDialogue(b.CurrentLife)));
            }
        }

        [TestCase("WORK_STUDY")]
        [TestCase("HEALTH")]
        [TestCase("NONE")]
        public void EveryInputFamilyCanReachAllThreeScenesAndKeepItsCompletionAfterReload(string category)
        {
            var save = new MemorySave(); var app = App(save); var captions = new List<string>();
            for (int day = 0; day < 7; day++) {
                var snapshot = Snapshot(day + 1, Item("one", category, true)); snapshot.date = app.CurrentDateKey;
                app.ApplyTodoSnapshot(snapshot);
                if (app.HasForestChoice) captions.Add(app.ChooseForestStory(app.CurrentLife.Story.Choices.Count == 2 ? 2 : 1));
                app.AcknowledgeForestToday();
                app.AdvanceLifeDay();
            }
            app = App(save);
            Assert.That(app.CurrentLife.Story.Choices, Has.Count.EqualTo(3));
            Assert.That(captions.Distinct().Count(), Is.EqualTo(3));
            Assert.That(app.CurrentLife.Route, Is.EqualTo(AdultRoute.Artisan));
            Assert.That(ForestStoryRules.VisiblePropName(app.CurrentLife), Is.EqualTo("손으로 만든 기념물"));
            Assert.That(app.SaveData.Forest.Days.Count(d => d.Acquisitions.Count > 0), Is.EqualTo(3));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(70));
            Assert.That(app.CurrentLife.Stage, Is.Not.EqualTo(CompanionStage.Adult));
        }

        [Test] public void PendingDialogueKeepsTitleAndDoesNotClaimCompletionOrGuessUserMood()
        {
            var app = App(); app.ApplyTodoSnapshot(Snapshot(1, Item("task", "HEALTH", false, "가벼운 산책")));
            string a = app.NextForestDialogue(), b = app.NextForestDialogue();
            Assert.That(a, Does.Contain("가벼운 산책").And.Contain("약속"));
            Assert.That(b, Does.Contain("가벼운 산책").And.Not.Contain("마쳤"));
            Assert.That(a, Is.Not.EqualTo(b));
            Assert.That(app.GetForestTodaySummary().Mood, Is.EqualTo("기대"));
            Assert.That(app.EssenceWallet.Balance, Is.Zero);
        }

        [Test] public void TwoMonthsKeepObservedScenesWhileHistoricalFactsAreCorrectedWithoutRewardReplay()
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("read", "WORK_STUDY", true)));
            app.ChooseForestStory(0); app.AcknowledgeForestToday();
            string spirit = app.CurrentLife.SpiritId;
            var january = app.SaveData.Forest.Days.Single();
            string observed = JsonUtility.ToJson(january.ObservedActivities[0]);
            string choice = app.CurrentLife.Story.Choices.Single().Caption;
            app.SynchronizeLifeDate(new DateTime(2026, 2, 3));
            var february = Snapshot(2, Item("walk", "HEALTH", true)); february.date = "2026-02-03";
            app.ApplyTodoSnapshot(february); app.AcknowledgeForestToday();
            int balance = app.EssenceWallet.Balance;
            Assert.That(app.CorrectForestDayFacts(Snapshot(3, Item("read", "WORK_STUDY", false))), Is.True);
            app = App(save);
            Assert.That(app.SaveData.Forest.Days, Has.Count.EqualTo(2));
            january = app.SaveData.Forest.Days.Single(d => d.Date.StartsWith("2026-01"));
            Assert.That(january.Todos.Single().IsCompleted, Is.False);
            Assert.That(JsonUtility.ToJson(january.ObservedActivities[0]), Is.EqualTo(observed));
            Assert.That(january.ObservedRevision, Is.EqualTo(1));
            Assert.That(january.Revision, Is.EqualTo(3));
            Assert.That(app.CurrentLife.Story.Choices.Single().Caption, Is.EqualTo(choice));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance));
            Assert.That(app.MonthlyTrace("2026-01", spirit), Does.Contain("0일"));
            Assert.That(app.MonthlyTrace("2026-02", spirit), Does.Contain("건강 1회"));
        }

        [Test] public void FirstDayNameCanBeSkippedOrChangedWithoutStartingFarewell()
        {
            var save = new MemorySave(); var app = App(save);
            app.SkipCompanionNaming(); app = App(save);
            Assert.That(app.CurrentLife.NamingOffered, Is.True);
            app.SetCompanionNameEarly("분홍 바람"); app.SetCompanionNameEarly("솜이"); app = App(save);
            Assert.That(app.CurrentLife.Name, Is.EqualTo("솜이"));
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Growing));
            Assert.Throws<InvalidOperationException>(() => app.BeginFarewell());
            Assert.Throws<ArgumentException>(() => app.SetCompanionNameEarly("<bad>"));
        }

        [TestCase("WORK_STUDY", AdultRoute.Recorder)]
        [TestCase("HEALTH", AdultRoute.Explorer)]
        [TestCase("NONE", AdultRoute.Artisan)]
        public void AdultCanKeepLivingAndExplicitFarewellSurvivesEveryReloadWithoutMovingOldRecords(string category, AdultRoute route)
        {
            var save = new MemorySave(); var app = App(save);
            app.SetCompanionNameEarly("바람이");
            for (int day = 0; day < 21; day++) {
                var snapshot = Snapshot(day + 1, Item("daily-habit", category, true));
                snapshot.date = app.CurrentDateKey;
                app.ApplyTodoSnapshot(snapshot);
                if (app.HasForestChoice) {
                    int option = app.CurrentLife.Story.Choices.Count == 2 ? (int)route : day % 2;
                    app.ChooseForestStory(option);
                    string choiceState = JsonUtility.ToJson(app.CurrentLife.Story);
                    app = App(save);
                    Assert.That(JsonUtility.ToJson(app.CurrentLife.Story), Is.EqualTo(choiceState), "Every chosen scene survives immediate restart.");
                }
                app.AcknowledgeForestToday();
                app.AdvanceLifeDay();
            }
            app = App(save);
            Assert.That(app.CurrentLife.Stage, Is.EqualTo(CompanionStage.Adult));
            Assert.That(app.CurrentLife.Route, Is.EqualTo(route));
            Assert.That(app.CurrentLife.RouteReasons, Has.Count.EqualTo(3));
            Assert.That(app.CurrentLife.RouteReasons[2], Does.Contain(category == "NONE" ? "미정" : "활동일"));
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Growing));
            Assert.That(app.CurrentLife.Name, Is.EqualTo("바람이"));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(210));

            // A real extra day after adulthood must keep the same companion present and active.
            string oldSpirit = app.CurrentLife.SpiritId;
            var continued = Snapshot(22, Item("daily-habit", category, true)); continued.date = app.CurrentDateKey;
            app.ApplyTodoSnapshot(continued); app.AcknowledgeForestToday(); app.AdvanceLifeDay();
            app = App(save);
            Assert.That(app.CurrentLife.SpiritId, Is.EqualTo(oldSpirit));
            Assert.That(app.CurrentLife.Stage, Is.EqualTo(CompanionStage.Adult));
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Growing));
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(22));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(220));
            Assert.Throws<InvalidOperationException>(() => app.SelectNextEgg(0));

            app.BeginFarewell(); app = App(save);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.AdultReveal));
            app.AdvanceLifeFarewell(); app = App(save);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Reflection));
            app.AdvanceLifeFarewell(); app = App(save);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Naming));
            app.NameCurrentCompanion("바람이"); app = App(save);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.LastCompanionship));
            app.ObserveNamedCompanion(); app = App(save);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.ReadyToLeave));
            app.LetCompanionLeave(); app = App(save);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Independent));
            string oldLife = JsonUtility.ToJson(app.CurrentLife);
            var oldRecords = app.SaveData.Records.Where(r => r.SpiritId == oldSpirit)
                .ToDictionary(r => r.Date, r => JsonUtility.ToJson(r));

            app.SelectNextEgg((int)route); app = App(save);
            Assert.That(app.CurrentLife.SpiritId, Is.Not.EqualTo(oldSpirit));
            Assert.That(app.CurrentLife.ActivityDays, Is.Zero);
            Assert.That(app.CurrentLife.Name, Is.Empty);
            Assert.That(app.CurrentLife.Farewell, Is.EqualTo(FarewellStep.Growing));
            Assert.That(app.GetForestTodaySummary().HasConfirmedData, Is.False);
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            var next = Snapshot(23, Item("new-habit", "NONE", true)); next.date = app.CurrentDateKey;
            app.ApplyTodoSnapshot(next); app.AcknowledgeForestToday(); app = App(save);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(230));
            Assert.That(app.CurrentCompletedTasks.Single().TaskId, Is.EqualTo("new-habit"));
            Assert.That(JsonUtility.ToJson(app.SaveData.Companions.Single(l => l.SpiritId == oldSpirit)), Is.EqualTo(oldLife));
            foreach (var record in oldRecords)
                Assert.That(JsonUtility.ToJson(app.SaveData.Records.Single(r => r.SpiritId == oldSpirit && r.Date == record.Key)), Is.EqualTo(record.Value));
        }

        [Test] public void SameDayHandoffKeepsCompletionOwnerAcrossReplayCancellationAndNewWork()
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("before", "WORK_STUDY", true)));
            app.AcknowledgeForestToday();
            string date = app.CurrentDateKey, oldId = app.CurrentLife.SpiritId;
            DepartForOwnershipTest(app);
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(1));
            string oldScene = JsonUtility.ToJson(app.CurrentRecord.SpiritDay);

            // A fresh host revision while no companion is present must be accepted, not block the egg UI.
            app.ApplyTodoSnapshot(Snapshot(2, Item("before", "WORK_STUDY", true)));
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(1));
            app.SelectNextEgg(0); app = App(save);
            string newId = app.CurrentLife.SpiritId;
            Assert.That(app.CurrentDateKey, Is.EqualTo(date));
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.Zero);
            Assert.That(app.GetForestTodaySummary().IsFirstVisit, Is.False);
            Assert.That(app.GetForestTodaySummary().Todos.Single().CompletedWithPreviousSpirit, Is.True);
            Assert.That(app.ApplyTodoSnapshot(Snapshot(2, Item("before", "WORK_STUDY", true))), Is.False);
            app.ApplyTodoSnapshot(Snapshot(3, Item("before", "WORK_STUDY", false)));
            app = App(save);
            app.ApplyTodoSnapshot(Snapshot(4, Item("before", "WORK_STUDY", true)));
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            Assert.That(app.SaveData.Forest.CompletionOwners.Single(o => o.Date == date && o.TodoId == "before").SpiritId, Is.EqualTo(oldId));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(10));
            app.ApplyTodoSnapshot(Snapshot(5, Item("before", "WORK_STUDY", true), Item("after", "HEALTH", true)));
            Assert.That(app.CurrentCompletedTasks.Select(t => t.TaskId), Is.EquivalentTo(new[] { "after" }));
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.EqualTo(8));
            Assert.That(app.GetForestTodaySummary().TotalEssence, Is.EqualTo(18));
            Assert.That(app.ForestActiveDays, Is.EqualTo(1));
            Assert.That(JsonUtility.ToJson(app.SaveData.Records.Single(r => r.Date == date && r.SpiritId == oldId).SpiritDay), Is.EqualTo(oldScene));
            app.AcknowledgeForestToday(); app = App(save);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(18));
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.Zero);
            Assert.That(app.SaveData.Records.Single(r => r.Date == date && r.SpiritId == oldId).CompletedTasks.Single().TaskId, Is.EqualTo("before"));
            Assert.That(app.SaveData.Records.Single(r => r.Date == date && r.SpiritId == newId).CompletedTasks.Single().TaskId, Is.EqualTo("after"));
            Assert.That(app.SaveData.Forest.Days.Single(d => d.Date == date && d.SpiritId == newId).Todos.Single(t => t.Id == "before").CompletedWithPreviousSpirit, Is.True);

            app.SynchronizeLifeDate(new DateTime(2026, 1, 26));
            app.CorrectForestDayFacts(Snapshot(6, Item("before", "WORK_STUDY", false), Item("after", "HEALTH", true)));
            Assert.That(app.SaveData.Records.Single(r => r.Date == date && r.SpiritId == oldId).CompletedTasks, Is.Empty);
            Assert.That(app.SaveData.Records.Single(r => r.Date == date && r.SpiritId == newId).CompletedTasks.Single().TaskId, Is.EqualTo("after"));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(18));
        }

        [Test] public void IndependentCompanionCanReturnOnRealDateAndPendingCompletionsWaitForNewCompanion()
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("before", "WORK_STUDY", true)));
            DepartForOwnershipTest(app);
            string oldId = app.CurrentLife.SpiritId, oldLife = JsonUtility.ToJson(app.CurrentLife);
            int oldRecordCount = app.SaveData.Records.Count;
            Assert.That(app.SynchronizeLifeDate(new DateTime(2026, 1, 27)), Is.True);
            app = App(save);
            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-01-27"));
            Assert.That(app.CurrentRecord, Is.Null);
            Assert.That(app.SaveData.Records.Count, Is.EqualTo(oldRecordCount));
            var pending = Snapshot(2, Item("new-day", "HEALTH", true)); pending.date = app.CurrentDateKey;
            Assert.That(app.ApplyTodoSnapshot(pending), Is.True);
            Assert.That(app.GetForestTodaySummary().Todos.Single().AwaitingCompanion, Is.True);
            Assert.That(app.GetForestTodaySummary().Activities, Is.Empty);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(20));
            app.AcknowledgeForestToday(); app = App(save);
            Assert.That(app.CurrentRecord, Is.Null);
            Assert.That(JsonUtility.ToJson(app.CurrentLife), Is.EqualTo(oldLife));
            Assert.That(app.SaveData.Forest.CompletionOwners.Single(o => o.Date == "2026-01-27").SpiritId, Is.Empty);
            app.SelectNextEgg(1, new DateTime(2026, 1, 27)); app = App(save);
            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-01-27"));
            Assert.That(app.CurrentCompletedTasks.Single().TaskId, Is.EqualTo("new-day"));
            Assert.That(app.GetForestTodaySummary().NewEssence, Is.Zero);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(20));
            Assert.That(app.SaveData.Records.Count(r => r.SpiritId == oldId), Is.EqualTo(oldRecordCount));
            Assert.That(JsonUtility.ToJson(app.SaveData.Companions.Single(l => l.SpiritId == oldId)), Is.EqualTo(oldLife));
        }

        [Test] public void OwnershipMigrationAndFailedSnapshotKeepPriorOwnerWalletAndExactState()
        {
            var save = new MemorySave(); var app = App(save);
            app.ApplyTodoSnapshot(Snapshot(1, Item("legacy", "WORK_STUDY", true)));
            DepartForOwnershipTest(app);
            string oldId = app.CurrentLife.SpiritId;
            // Simulate a valid pre-ownership save while retaining its authoritative daily record.
            app.SaveData.Forest.CompletionOwners.Clear(); save.Save(app.SaveData);
            app = App(save);
            Assert.That(app.SaveData.Forest.CompletionOwners.Single().SpiritId, Is.EqualTo(oldId));
            app.SelectNextEgg(0);
            string before = JsonUtility.ToJson(app.SaveData), persisted = save.Json;
            save.Fail = true;
            Assert.Throws<IOException>(() => app.ApplyTodoSnapshot(Snapshot(2, Item("legacy", "WORK_STUDY", true), Item("later", "HEALTH", true))));
            Assert.That(JsonUtility.ToJson(app.SaveData), Is.EqualTo(before));
            Assert.That(save.Json, Is.EqualTo(persisted));
            Assert.That(app.CurrentCompletedTasks, Is.Empty);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(10));
            save.Fail = false;
            app.ApplyTodoSnapshot(Snapshot(2, Item("legacy", "WORK_STUDY", true), Item("later", "HEALTH", true)));
            Assert.That(app.CurrentCompletedTasks.Single().TaskId, Is.EqualTo("later"));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(18));
        }

        [Test] public void HandoffSharesTheDailyTwentyFiveCapAcrossCompanions()
        {
            var app = App();
            app.ApplyTodoSnapshot(Snapshot(1, Item("a", "WORK_STUDY", true), Item("b", "WORK_STUDY", true),
                Item("c", "HEALTH", true), Item("d", "NONE", true)));
            DepartForOwnershipTest(app); app.SelectNextEgg(2);
            app.ApplyTodoSnapshot(Snapshot(2, Item("a", "WORK_STUDY", true), Item("b", "WORK_STUDY", true),
                Item("c", "HEALTH", true), Item("d", "NONE", true), Item("e", "HOBBY", true)));
            Assert.That(app.CurrentCompletedTasks.Single().TaskId, Is.EqualTo("e"));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(25));
            Assert.That(app.GetForestTodaySummary().TotalEssence, Is.EqualTo(25));
        }

        [Test] public void OnlyExplicitPrototypeDemoCanRestoreCachedConnectionWithoutReapplyingFacts()
        {
            var save = new MemorySave(); var app = App(save);
            var example = Snapshot(1, Item("example", "WORK_STUDY", true)); example.accountId = "prototype-demo-v1";
            app.ApplyTodoSnapshot(example); app = App(save);
            string before = save.Json;
            Assert.That(app.GetForestTodaySummary().SyncState, Is.EqualTo(ForestSyncState.Awaiting));
            app.RestorePrototypeDemoConnection();
            Assert.That(app.GetForestTodaySummary().SyncState, Is.EqualTo(ForestSyncState.Confirmed));
            Assert.That(save.Json, Is.EqualTo(before));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(10));
            var real = App(); real.ApplyTodoSnapshot(Snapshot(1));
            Assert.Throws<InvalidOperationException>(() => real.RestorePrototypeDemoConnection());
        }

        private static void DepartForOwnershipTest(PrototypeApplicationService app)
        {
            // The full 21-day policy is covered above. These tests isolate the handoff boundary.
            app.CurrentLife.Stage = CompanionStage.Adult;
            app.CurrentLife.Farewell = FarewellStep.ReadyToLeave;
            app.SetCompanionNameEarly("기억할 친구");
            app.LetCompanionLeave();
        }
    }
}
