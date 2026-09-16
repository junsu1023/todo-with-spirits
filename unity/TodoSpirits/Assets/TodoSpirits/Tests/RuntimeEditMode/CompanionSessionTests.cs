using System;
using System.IO;
using NUnit.Framework;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class CompanionSessionTests
    {
        private sealed class MemorySave : ISpiritSaveRepository
        {
            public string Json;
            public bool Fail;
            public string SavePath => "memory";
            public PrototypeSaveData Load() => Json == null ? new PrototypeSaveData() : JsonUtility.FromJson<PrototypeSaveData>(Json);
            public void Save(PrototypeSaveData data)
            {
                if (Fail) throw new IOException("Test save failure");
                Json = JsonUtility.ToJson(data);
            }
        }

        private static PrototypeApplicationService Create(MemorySave save)
        {
            var app = new PrototypeApplicationService(new MockTodoCompletionSource(), save,
                new TaskClassifier(), new SpiritDayGenerator(), new DateTime(2026, 1, 1), true);
            app.Initialize();
            return app;
        }

        [Test]
        public void DailyPresentationIsPreservedAcrossSaveReload()
        {
            var save=new MemorySave();
            var app=Create(save);
            string line=app.CurrentRecord.SpiritDay.Dialogue;
            int pose=app.CurrentRecord.SpiritDay.PresentationVariant;
            app=Create(save);
            Assert.That(app.CurrentRecord.SpiritDay.Dialogue,Is.EqualTo(line));
            Assert.That(app.CurrentRecord.SpiritDay.PresentationVariant,Is.EqualTo(pose));
            Assert.That(pose,Is.InRange(1,3));
        }

        [Test]
        public void FirstMeetingLocationIsSavedAndSurvivesReload()
        {
            var save = new MemorySave();
            var app = Create(save);
            string location = app.CurrentRecord.SpiritDay.Location;
            Assert.That(location, Is.EqualTo(CompanionStagePresentation.PlaceName(app.CurrentLife.Stage, app.CurrentRecord.SpiritDay.PrimaryAction)));
            app = Create(save);
            Assert.That(app.CurrentRecord.SpiritDay.Location, Is.EqualTo(location));
        }

        [Test]
        public void ClassificationMemoryPersistsWithoutRerollAndRollsBackOnFailure()
        {
            var save = new MemorySave();
            var app = Create(save);
            while (app.CurrentCompletedTasks.Count == 0) app.AdvanceLifeDay();
            var task = app.CurrentCompletedTasks[0];
            string record = JsonUtility.ToJson(app.CurrentRecord);
            app.RememberTaskClassification(task.TaskId, SpiritActionId.SocialTea);
            Assert.That(JsonUtility.ToJson(app.CurrentRecord), Is.EqualTo(record));
            app = Create(save);
            Assert.That(new TaskClassifier().Classify(task, app.SaveData.ClassificationMemory).ActionTags[0], Is.EqualTo(SpiritActionId.SocialTea));
            save.Fail = true;
            Assert.Throws<IOException>(() => app.RememberTaskClassification(task.TaskId, SpiritActionId.Rest));
            Assert.That(new TaskClassifier().Classify(task, app.SaveData.ClassificationMemory).ActionTags[0], Is.EqualTo(SpiritActionId.SocialTea));
            save.Fail = false;
            bool found = false;
            for (int i = 0; i < 3; i++)
            {
                var day = app.AdvanceLifeDay();
                for (int j = 0; j < day.CompletedTasks.Count; j++)
                    if (day.CompletedTasks[j].Title == task.Title)
                    {
                        Assert.That(day.Classifications[j].ActionTags[0], Is.EqualTo(SpiritActionId.SocialTea));
                        found = true;
                    }
            }
            Assert.That(found, Is.True);
        }

        [Test]
        public void RoutineClassificationSurvivesRestartAndUsesStableRoutineIdentity()
        {
            var save = new MemorySave();
            var app = Create(save);
            for (int i = 0; i < 3 && app.SaveData.ClassificationMemory.Routines.Count == 0; i++) app.AdvanceLifeDay();
            Assert.That(app.SaveData.ClassificationMemory.Routines.Count, Is.EqualTo(1));
            app = Create(save);
            var next = new CompletedTask("new-occurrence", "수면", UserTaskCategory.Health, "") { RoutineId = "mock-routine:daily-walk" };
            var result = new TaskClassifier().Classify(next, app.SaveData.ClassificationMemory);
            Assert.That(result.ActionTags[0], Is.EqualTo(SpiritActionId.WalkForest));
            Assert.That(result.Reason, Does.Contain("루틴"));
        }

        [Test]
        public void InitialSaveFailure_DoesNotExposeInitializedState_AndRetryGrantsOnce()
        {
            var save = new MemorySave { Fail = true };
            var app = new PrototypeApplicationService(new MockTodoCompletionSource(), save,
                new TaskClassifier(), new SpiritDayGenerator(), new DateTime(2026,1,1), true);
            Assert.Throws<IOException>(() => app.Initialize());
            Assert.That(app.IsInitialized, Is.False);
            Assert.That(app.CurrentRecord, Is.Null);
            save.Fail = false;
            app.Initialize();
            Assert.That(app.IsInitialized, Is.True);
            Assert.That(app.SaveData.Records.Count, Is.EqualTo(1));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(18));
            app.Initialize();
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(18));
        }

        [Test]
        public void OngoingProject_IsSavedAndAddsCandidateWithoutChangingTaskClassification()
        {
            var save = new MemorySave();
            var app = Create(save);
            for(int i=0;i<24 && app.CurrentLife.ActiveProject==null;i++)
            {
                app.ObserveLifeStage();
                app.AdvanceLifeDay();
            }
            Assert.That(app.CurrentLife.ActiveProject, Is.Not.Null);
            string title = app.CurrentLife.ActiveProject.Title;
            var action = app.CurrentLife.ActiveProject.Action;
            Assert.That(app.CurrentRecord.CandidateActions.Find(c=>c.Action==action).Reason, Does.Contain("이어가는"));
            Assert.That(app.CurrentRecord.Classifications.Count, Is.EqualTo(app.CurrentRecord.CompletedTasks.Count));
            app = Create(save);
            Assert.That(app.CurrentLife.ActiveProject.Title, Is.EqualTo(title));
        }

        [Test]
        public void RealDateRollover_FinalizesOnlyKnownDay_AndDoesNotRewindOrDuplicate()
        {
            var save = new MemorySave();
            var app = Create(save);
            Assert.That(app.SynchronizeLifeDate(new DateTime(2026, 2, 1)), Is.True);
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(1));
            Assert.That(app.SaveData.Records.Count, Is.EqualTo(2));
            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-02-01"));
            var balance = app.EssenceWallet.Balance;
            Assert.That(app.SynchronizeLifeDate(new DateTime(2026, 2, 1)), Is.False);
            Assert.That(app.SynchronizeLifeDate(new DateTime(2026, 1, 1)), Is.False);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance));
            app = Create(save);
            Assert.That(app.CurrentDateKey, Is.EqualTo("2026-02-01"));
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(1));
        }

        [Test]
        public void Gift_ChangesLaterCandidatesWithoutRerollingToday_AndExpires()
        {
            var save = new MemorySave();
            var app = Create(save);
            var today = app.CurrentRecord.SpiritDay.PrimaryAction;
            int balance = app.EssenceWallet.Balance;
            Assert.That(app.GiveGift(1), Is.Not.Empty);
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance - CompanionItemCatalog.GiftPrice));
            Assert.That(app.CurrentRecord.SpiritDay.PrimaryAction, Is.EqualTo(today));
            Assert.Throws<InvalidOperationException>(() => app.GiveGift(0));
            app = Create(save);
            for (int i = 0; i < 3; i++)
            {
                app.AdvanceLifeDay();
                Assert.That(app.CurrentRecord.CandidateActions.Find(c => c.Action == SpiritActionId.Rest).Reason, Does.Contain("선물"));
            }
            app.AdvanceLifeDay();
            Assert.That(app.CurrentRecord.CandidateActions.Find(c => c.Action == SpiritActionId.Rest).Reason, Does.Not.Contain("선물"));
        }

        [TestCase(0)]
        [TestCase(1)]
        [TestCase(2)]
        public void Travel_SurvivesReload_RequiresFourHours_AndCannotBeClaimedTwice(int preparation)
        {
            var save = new MemorySave();
            var app = Create(save);
            app.AdvanceLifeDay(); app.AdvanceLifeDay();
            var now = new DateTime(2026, 1, 3, 12, 0, 0, DateTimeKind.Utc);
            int balance = app.EssenceWallet.Balance;
            app.StartTrip(preparation, now);
            string plannedDiscovery = app.PendingTrip.Discovery;
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance - 40));
            Assert.Throws<InvalidOperationException>(() => app.StartTrip(0, now));
            Assert.Throws<InvalidOperationException>(() => app.GiveGift(0));
            app = Create(save);
            Assert.That(app.CanClaimTrip(now.AddHours(3.99)), Is.False);
            Assert.Throws<InvalidOperationException>(() => app.ClaimTrip(now));
            var result = app.ClaimTrip(now.AddDays(7));
            Assert.That(result.Discovery, Is.EqualTo(plannedDiscovery));
            Assert.That(result.Discovery, Is.Not.Empty);
            Assert.That(result.EventText, Is.Not.Empty);
            Assert.That(app.PendingTrip, Is.Null);
            app = Create(save);
            Assert.That(app.Interventions.Trips[0].Claimed, Is.True);
            Assert.Throws<InvalidOperationException>(() => app.ClaimTrip(now.AddDays(7)));
        }

        [Test]
        public void Decoration_RequiresOwnership_PersistsAndRollsBackFailedPurchase()
        {
            var save = new MemorySave();
            var app = Create(save);
            Assert.Throws<InvalidOperationException>(() => app.EquipDecoration(0, true));
            Assert.Throws<InvalidOperationException>(() => app.BuyDecoration(0));
            app.AdvanceLifeDay();
            int balance = app.EssenceWallet.Balance;
            save.Fail = true;
            Assert.Throws<IOException>(() => app.BuyDecoration(0));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance));
            Assert.That(app.Interventions.OwnedDecorations, Is.Empty);
            save.Fail = false;
            app.BuyDecoration(0); app.EquipDecoration(0, true);
            app = Create(save);
            Assert.That(app.Interventions.EquippedDecorations, Does.Contain("0"));
            Assert.Throws<InvalidOperationException>(() => app.BuyDecoration(0));
            app.EquipDecoration(0, false);
            Assert.That(app.Interventions.EquippedDecorations, Is.Empty);
        }

        [Test]
        public void CompleteCycle_ReloadsNamedAdultAndPreservesArchiveAfterEgg()
        {
            var save = new MemorySave();
            var app = Create(save);
            for (int i = 0; i < 42 && app.CurrentLife.Stage != CompanionStage.Adult; i++)
            {
                app.ObserveLifeStage();
                app.AdvanceLifeDay();
            }
            Assert.That(app.CurrentLife.Stage, Is.EqualTo(CompanionStage.Adult));
            app.AdvanceLifeFarewell();
            app.AdvanceLifeFarewell();
            app.NameCurrentCompanion("잎새");
            string oldId = app.CurrentLife.SpiritId;
            int oldActivityDays = app.CurrentLife.ActivityDays;
            app = Create(save);
            Assert.That(app.CurrentLife.Name, Is.EqualTo("잎새"));
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(oldActivityDays));
            Assert.Throws<InvalidOperationException>(() => app.LetCompanionLeave());
            app.ObserveNamedCompanion();
            app.LetCompanionLeave();
            app.SelectNextEgg(2);
            app = Create(save);
            Assert.That(app.SaveData.Companions.Count, Is.EqualTo(2));
            Assert.That(app.CurrentLife.SpiritId, Is.Not.EqualTo(oldId));
            Assert.That(app.CurrentLife.ActivityDays, Is.Zero);
            Assert.That(app.CurrentSpiritState.Temperaments, Is.EqualTo(app.CurrentLife.Temperaments));
            Assert.That(app.SaveData.Records.Exists(record => record.SpiritId == oldId), Is.True);
            Assert.That(app.SaveData.Companions[0].Name, Is.EqualTo("잎새"));
            Assert.Throws<InvalidOperationException>(() => app.SelectNextEgg(1));
        }

        [Test]
        public void FailedSave_RollsBackDateRewardsAndLifeProgress()
        {
            var save = new MemorySave();
            var app = Create(save);
            int balance = app.EssenceWallet.Balance;
            string date = app.CurrentDateKey;
            save.Fail = true;
            Assert.Throws<IOException>(() => app.AdvanceLifeDay());
            Assert.That(app.CurrentDateKey, Is.EqualTo(date));
            Assert.That(app.CurrentLife.ActivityDays, Is.Zero);
            Assert.That(app.SaveData.Records.Count, Is.EqualTo(1));
            Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance));
            save.Fail = false;
            app.AdvanceLifeDay();
            Assert.That(app.CurrentLife.ActivityDays, Is.EqualTo(1));
        }

        [Test]
        public void LifeMode_DoesNotFabricatePriorHistoryOrAllowDemoOverwrite()
        {
            var app = Create(new MemorySave());
            app.AdvanceLifeDay();
            Assert.That(app.SaveData.Records.Count, Is.EqualTo(2));
            Assert.Throws<InvalidOperationException>(() => app.SelectDemoProfile(DemoTemperamentProfile.ProfileB));
            Assert.Throws<InvalidOperationException>(() => app.RegenerateCurrentDay());
            Assert.Throws<InvalidOperationException>(() => app.ResetDemoSave());
        }
    }
}
