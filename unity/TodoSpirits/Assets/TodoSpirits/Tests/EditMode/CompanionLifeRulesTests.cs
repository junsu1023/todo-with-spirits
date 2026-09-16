using System;
using NUnit.Framework;

namespace TodoSpirits.Core.Tests
{
    public sealed class CompanionLifeRulesTests
    {
        private static DailyCompanionRecord Day(int day, bool active = true, SpiritActionId action = SpiritActionId.ReadRecords)
        {
            var record = new DailyCompanionRecord {
                SpiritId = "life-1", Date = new DateTime(2026, 1, 1).AddDays(day).ToString("yyyy-MM-dd"),
                SpiritDay = new SpiritDayResult { PrimaryAction = action }
            };
            if (active) record.CompletedTasks.Add(new CompletedTask("task-" + day, "독서", UserTaskCategory.WorkStudy, record.Date));
            return record;
        }

        private static CompanionLife NewLife() => CompanionLifeRules.CreateFirst("life-1", "2026-01-01");

        [Test]
        public void TravelStories_AreStableAndPreparationsFavorTheirTheme()
        {
            string[][] discoveries = { new[] { "이웃의 감사 쪽지", "작은 마을 엽서" }, new[] { "반짝이는 씨앗", "물결무늬 조약돌" }, new[] { "숲길 관찰 지도", "나뭇잎 관찰장" } };
            var observed = new System.Collections.Generic.HashSet<string>();
            for(int preparation=0;preparation<3;preparation++)
            {
                int favored=0;
                for(int i=0;i<300;i++)
                {
                    var trip = new CompanionTrip { TripId="test-trip-"+i, Preparation=preparation };
                    CompanionTravelStories.Resolve(trip);
                    if(Array.IndexOf(discoveries[preparation],trip.Discovery)>=0) favored++;
                    observed.Add(trip.Discovery);
                    string story=trip.EventText;
                    trip.ReturnAtUtc="2099-01-01T00:00:00Z";
                    CompanionTravelStories.Resolve(trip);
                    Assert.That(trip.EventText,Is.EqualTo(story));
                }
                Assert.That(favored,Is.GreaterThan(170));
            }
            Assert.That(observed.Count,Is.EqualTo(6));
        }

        [Test]
        public void OngoingProject_UsesActualPatternAndOnlyMatchingActiveDaysAdvanceIt()
        {
            var life = NewLife();
            life.Stage = CompanionStage.OwnWay;
            for(int i=0;i<3;i++) CompanionLifeRules.FinalizeDay(life, Day(i,true,SpiritActionId.CraftRepair));
            Assert.That(life.ActiveProject.Action, Is.EqualTo(SpiritActionId.CraftRepair));
            Assert.That(life.ActiveProject.WorkDays, Is.EqualTo(1));
            CompanionLifeRules.FinalizeDay(life, Day(3,false));
            CompanionLifeRules.FinalizeDay(life, Day(4,true,SpiritActionId.WalkForest));
            Assert.That(life.ActiveProject.WorkDays, Is.EqualTo(1));
            CompanionLifeRules.FinalizeDay(life, Day(5,true,SpiritActionId.CraftRepair));
            var final = Day(6,true,SpiritActionId.CraftRepair);
            CompanionLifeRules.FinalizeDay(life, final);
            Assert.That(life.ActiveProject, Is.Null);
            Assert.That(life.Projects[0].IsComplete, Is.True);
            Assert.That(final.LifeActivityText, Does.Contain("마무리"));
            CompanionLifeRules.FinalizeDay(life, final);
            Assert.That(life.Projects[0].WorkDays, Is.EqualTo(3));
        }

        [Test]
        public void Finalization_IsIdempotent_AndEmptyDaysDoNotGrow()
        {
            var life = NewLife();
            Assert.That(CompanionLifeRules.FinalizeDay(life, Day(0)), Is.True);
            Assert.That(CompanionLifeRules.FinalizeDay(life, Day(0)), Is.False);
            CompanionLifeRules.FinalizeDay(life, Day(50, false));
            Assert.That(life.ActivityDays, Is.EqualTo(1));
            Assert.That(life.Stage, Is.EqualTo(CompanionStage.Meeting));
            Assert.That(life.Actions[0].Count, Is.EqualTo(1));
        }

        [Test]
        public void DiverseObservedLife_ReachesAdultIn28ActivityDays()
        {
            var life = NewLife();
            for (int i = 0; i < 28; i++)
            {
                CompanionLifeRules.ObserveStageEvent(life);
                CompanionLifeRules.FinalizeDay(life, Day(i, true, i % 4 == 0 ? SpiritActionId.WalkForest : SpiritActionId.ReadRecords));
            }
            Assert.That(life.Stage, Is.EqualTo(CompanionStage.Adult));
            Assert.That(life.Route, Is.EqualTo(AdultRoute.Recorder));
            Assert.That(life.RouteReasons.Count, Is.EqualTo(3));
        }

        [Test]
        public void SingleActivityWithoutEvents_AdvancesBy42ActivityDays()
        {
            var life = NewLife();
            for (int i = 0; i < 42; i++) CompanionLifeRules.FinalizeDay(life, Day(i, true, SpiritActionId.CraftRepair));
            Assert.That(life.Stage, Is.EqualTo(CompanionStage.Adult));
            Assert.That(life.Route, Is.EqualTo(AdultRoute.Artisan));
            Assert.That(life.Farewell, Is.EqualTo(FarewellStep.AdultReveal));
            var route = life.Route;
            CompanionLifeRules.FinalizeDay(life, Day(42, true, SpiritActionId.WalkForest));
            Assert.That(life.Route, Is.EqualTo(route));
        }

        [Test]
        public void Farewell_RequiresReflectionNameAndNamedMainVisit()
        {
            var life = NewLife();
            Assert.Throws<InvalidOperationException>(() => CompanionLifeRules.Name(life, "잎새"));
            for (int i = 0; i < 42; i++) CompanionLifeRules.FinalizeDay(life, Day(i));
            CompanionLifeRules.AdvanceFarewell(life);
            CompanionLifeRules.AdvanceFarewell(life);
            Assert.Throws<ArgumentException>(() => CompanionLifeRules.Name(life, "  "));
            Assert.Throws<ArgumentException>(() => CompanionLifeRules.Name(life, "<b>잎새</b>"));
            CompanionLifeRules.Name(life, " 잎새 ");
            Assert.That(life.DisplayName, Is.EqualTo("잎새"));
            Assert.Throws<InvalidOperationException>(() => CompanionLifeRules.Leave(life, "2026-03-01"));
            CompanionLifeRules.ObserveNamedCompanion(life);
            CompanionLifeRules.Leave(life, "2026-03-01");
            Assert.That(life.Farewell, Is.EqualTo(FarewellStep.Independent));
            Assert.That(life.Actions, Is.Not.Empty);
            Assert.That(CompanionLifeRules.FinalizeDay(life, Day(90)), Is.False);
        }

        [Test]
        public void EggSelection_HasStableUniqueTraits_AndAllEightAreReachable()
        {
            var seen = new System.Collections.Generic.HashSet<SpiritTemperament>();
            for (int i = 0; i < 60; i++)
            {
                var first = CompanionLifeRules.CreateFromEgg("egg-child-" + i, "2026-03-01", i % 3);
                var repeated = CompanionLifeRules.CreateFromEgg("egg-child-" + i, "2026-03-01", i % 3);
                CollectionAssert.AreEqual(first.Temperaments, repeated.Temperaments);
                Assert.That(first.Temperaments.Count, Is.InRange(2, 3));
                CollectionAssert.AllItemsAreUnique(first.Temperaments);
                foreach (var trait in first.Temperaments) seen.Add(trait);
            }
            Assert.That(seen.Count, Is.EqualTo(8));
        }

        [Test]
        public void RejectsWrongSpiritAndOutOfOrderRecordsWithoutMutation()
        {
            var life = NewLife();
            var wrong = Day(0); wrong.SpiritId = "other";
            Assert.Throws<InvalidOperationException>(() => CompanionLifeRules.FinalizeDay(life, wrong));
            CompanionLifeRules.FinalizeDay(life, Day(2));
            Assert.Throws<InvalidOperationException>(() => CompanionLifeRules.FinalizeDay(life, Day(1)));
            Assert.That(life.ActivityDays, Is.EqualTo(1));
        }
    }
}
