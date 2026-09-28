using System;
using System.Linq;
using NUnit.Framework;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class ForestInterventionExperienceTests
    {
        private sealed class MemorySave : ISpiritSaveRepository
        {
            private string _json;
            public string SavePath => "forest-intervention-tests";
            public PrototypeSaveData Load() => _json == null ? new PrototypeSaveData() : JsonUtility.FromJson<PrototypeSaveData>(_json);
            public void Save(PrototypeSaveData value) => _json = JsonUtility.ToJson(value);
        }

        private static PrototypeApplicationService Open(MemorySave save)
        {
            var app = new PrototypeApplicationService(new EmptyTodoCompletionSource(), save, new TaskClassifier(),
                new SpiritDayGenerator(), new DateTime(2026, 1, 25), true);
            app.Initialize();
            if (!app.SaveData.Forest.Enabled) app.EnableForest();
            return app;
        }

        private static void CompleteDay(PrototypeApplicationService app)
        {
            if (app.TodayTodos.Count == 0)
            {
                app.AddForestTodo("책 읽기", UserTaskCategory.WorkStudy, true);
                app.AddForestTodo("산책", UserTaskCategory.Health, true);
            }
            foreach (var todo in app.TodayTodos.ToArray())
                if (!todo.CompletedDates.Contains(app.CurrentDateKey)) app.ToggleForestTodo(todo.Id);
        }

        [TestCase(0, "read")]
        [TestCase(1, "rest")]
        [TestCase(2, "craft")]
        [TestCase(3, "craft")]
        [TestCase(4, "tea")]
        [TestCase(5, "walk")]
        public void GiftAppearsAsAutonomousLifeForNextThreeDaysAndSurvivesReload(int giftId, string activity)
        {
            var save = new MemorySave();
            var app = Open(save);
            CompleteDay(app);
            string giftDay = app.CurrentDateKey;
            app.GiveGift(giftId);
            Assert.That(app.GetForestTodaySummary().Activities.Any(a => !string.IsNullOrEmpty(a.GiftSource)), Is.False);
            int balance = app.EssenceWallet.Balance;
            int activityDays = app.ForestActiveDays;
            app.AdvanceLifeDay();
            string rememberedDay = app.CurrentDateKey;
            for (int day = 1; day <= 3; day++)
            {
                var summary = app.GetForestTodaySummary();
                var inspired = summary.Activities.First();
                Assert.That(inspired.ActivityKey, Is.EqualTo(activity));
                Assert.That(inspired.GiftSource, Is.EqualTo(CompanionItemCatalog.Gifts[giftId]));
                Assert.That(inspired.GiftGivenOn, Is.EqualTo(giftDay));
                Assert.That(inspired.IsAutonomous, Is.True);
                Assert.That(inspired.IsCompleted, Is.False);
                Assert.That(inspired.SourceTodoIds, Is.Empty);
                Assert.That(summary.Todos.Count, Is.EqualTo(2));
                Assert.That(summary.Todos.Any(t => t.IsCompleted), Is.False);
                Assert.That(summary.TotalEssence, Is.Zero);
                Assert.That(app.ForestActiveDays, Is.EqualTo(activityDays));
                Assert.That(app.EssenceWallet.Balance, Is.EqualTo(balance));
                app.AcknowledgeForestToday(summary);
                app = Open(save);
                Assert.That(app.GetForestTodaySummary().Activities.First().GiftSource, Is.EqualTo(CompanionItemCatalog.Gifts[giftId]));
                app.AdvanceLifeDay();
            }
            Assert.That(app.GetForestTodaySummary().Activities.All(a => string.IsNullOrEmpty(a.GiftSource)), Is.True);
            var memory = app.SaveData.Forest.Days.Single(d => d.Date == rememberedDay);
            Assert.That(memory.ObservedActivities.First().GiftSource, Is.EqualTo(CompanionItemCatalog.Gifts[giftId]));
            Assert.That(memory.PrimaryActivity, Is.EqualTo(activity));
        }

        [Test]
        public void MostRecentEligibleGiftIsShownAndAnotherCompanionsGiftIsIgnored()
        {
            var app = Open(new MemorySave());
            CompleteDay(app);
            app.GiveGift(1);
            app.AdvanceLifeDay();
            CompleteDay(app);
            app.GiveGift(4);
            Assert.That(app.GetForestTodaySummary().Activities.First().GiftSource, Is.EqualTo("따뜻한 담요"));
            app.AdvanceLifeDay();
            var summary = app.GetForestTodaySummary();
            Assert.That(summary.Activities.Count(a => !string.IsNullOrEmpty(a.GiftSource)), Is.EqualTo(1));
            Assert.That(summary.Activities.First().GiftSource, Is.EqualTo("나무 열매 차"));
            foreach (var gift in app.Interventions.Gifts) gift.SpiritId = "previous-companion-fixture";
            Assert.That(app.GetForestTodaySummary().Activities.All(a => string.IsNullOrEmpty(a.GiftSource)), Is.True);
        }

        [Test]
        public void TripDialogueUsesTravelStateWhileOfflineAndAfterReturnTime()
        {
            var app = Open(new MemorySave());
            for (int day = 0; day < 3; day++) { CompleteDay(app); app.AdvanceLifeDay(); }
            app.StartTrip(2, DateTime.UtcNow);
            app.SetForestSyncState(ForestSyncState.Offline);
            for (int i = 0; i < 6; i++)
            {
                string line = app.NextForestDialogue();
                Assert.That(line, Does.Contain("숲길"));
                Assert.That(line, Does.Not.Contain("책상"));
            }
            // Only this in-memory test trip advances; the device clock stays unchanged.
            app.PendingTrip.ReturnAtUtc = DateTime.UtcNow.AddMinutes(-1).ToString("O");
            Assert.That(app.NextForestDialogue(), Does.Contain("돌아왔어"));
        }
    }
}
