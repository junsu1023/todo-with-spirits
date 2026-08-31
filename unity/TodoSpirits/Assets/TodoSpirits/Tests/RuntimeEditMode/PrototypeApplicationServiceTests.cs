using System;
using System.Collections.Generic;
using NUnit.Framework;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class PrototypeApplicationServiceTests
    {
        private static readonly DateTime DemoStartDate = new DateTime(2026, 8, 31);

        [Test]
        public void SelectDay3_MatchesSequentialVisit_WhenPriorDemoRecordsAreMissing()
        {
            var day3FirstRepository = new InMemorySpiritSaveRepository();
            var day3FirstService = CreateService(day3FirstRepository);
            day3FirstService.Initialize();
            day3FirstService.SaveData.Records.Clear();
            day3FirstService.SaveData.EssenceWallet = new EssenceWallet();

            var day3First = day3FirstService.SelectDemoDay(DemoDayId.Day3Rest);

            var sequentialRepository = new InMemorySpiritSaveRepository();
            var sequentialService = CreateService(sequentialRepository);
            sequentialService.Initialize();
            sequentialService.SelectDemoDay(DemoDayId.Day2HealthRelationship);
            var sequentialDay3 = sequentialService.SelectDemoDay(DemoDayId.Day3Rest);

            Assert.That(day3First.SpiritDay.Seed, Is.EqualTo(sequentialDay3.SpiritDay.Seed));
            Assert.That(
                day3First.SpiritDay.PrimaryAction,
                Is.EqualTo(sequentialDay3.SpiritDay.PrimaryAction));
            CollectionAssert.AreEqual(
                day3First.SpiritDay.SecondaryActions,
                sequentialDay3.SpiritDay.SecondaryActions);
            Assert.That(day3First.SelectionReason, Is.EqualTo(sequentialDay3.SelectionReason));
            AssertCandidateScoresEqual(day3First, sequentialDay3);
        }

        [Test]
        public void SelectDay3_TransientPriorSimulation_DoesNotPersistOrGrantRewards()
        {
            var repository = new InMemorySpiritSaveRepository();
            var service = CreateService(repository);
            service.Initialize();
            service.SaveData.Records.Clear();
            service.SaveData.EssenceWallet = new EssenceWallet();
            repository.ResetTracking();

            var day3 = service.SelectDemoDay(DemoDayId.Day3Rest);

            Assert.That(service.SaveData.Records.Count, Is.EqualTo(1));
            Assert.That(service.SaveData.Records[0], Is.SameAs(day3));
            Assert.That(day3.Date, Is.EqualTo("2026-09-02"));
            Assert.That(day3.CompletedTasks, Is.Empty);
            Assert.That(service.EssenceWallet.Balance, Is.Zero);
            Assert.That(service.EssenceWallet.HighestRewardBandByDate.Count, Is.EqualTo(1));
            Assert.That(
                service.EssenceWallet.HighestRewardBandByDate[0].Date,
                Is.EqualTo(day3.Date));
            Assert.That(repository.SaveCallCount, Is.EqualTo(1));
        }

        [Test]
        public void ResetDemoSave_RestoresProfileADay1AndFreshRewardState()
        {
            var repository = new InMemorySpiritSaveRepository();
            var service = CreateService(repository);
            service.Initialize();
            service.SelectDemoDay(DemoDayId.Day2HealthRelationship);
            service.SelectDemoProfile(DemoTemperamentProfile.ProfileB);
            service.AcknowledgeReward();
            repository.ResetTracking();

            var succeeded = service.ResetDemoSave();

            Assert.That(succeeded, Is.True);
            Assert.That(service.CurrentDemoDay, Is.EqualTo(DemoDayId.Day1WorkFocus));
            Assert.That(
                service.CurrentDemoProfile,
                Is.EqualTo(DemoTemperamentProfile.ProfileA));
            Assert.That(service.CurrentDate, Is.EqualTo(DemoStartDate));
            Assert.That(service.CurrentProfileName, Is.EqualTo("Profile A"));
            Assert.That(service.CurrentSpiritState.SpiritId, Is.EqualTo("prototype-spirit-001"));
            CollectionAssert.AreEqual(
                new[]
                {
                    SpiritTemperament.Curious,
                    SpiritTemperament.Meticulous
                },
                service.CurrentSpiritState.Temperaments);
            Assert.That(
                service.CurrentSpiritState.FavoriteAction,
                Is.EqualTo(SpiritActionId.ReadRecords));
            Assert.That(service.SaveData.Records.Count, Is.EqualTo(1));
            Assert.That(service.CurrentRecord, Is.SameAs(service.SaveData.Records[0]));
            Assert.That(service.CurrentRecord.SpiritId, Is.EqualTo("prototype-spirit-001"));
            Assert.That(service.CurrentRecord.CompletedTasks.Count, Is.EqualTo(3));
            Assert.That(service.CurrentRecord.EssenceReward, Is.EqualTo(18));
            Assert.That(service.CurrentRecord.GrantedEssenceDelta, Is.EqualTo(18));
            Assert.That(service.EssenceWallet.Balance, Is.EqualTo(18));
            Assert.That(repository.SaveCallCount, Is.EqualTo(1));
            Assert.That(repository.StoredData, Is.SameAs(service.SaveData));
        }

        [Test]
        public void RegenerateCurrentDay_DoesNotGrantDuplicateReward()
        {
            var repository = new InMemorySpiritSaveRepository();
            var service = CreateService(repository);
            var firstRecord = service.Initialize();
            var firstBalance = service.EssenceWallet.Balance;
            var firstSeed = firstRecord.SpiritDay.Seed;
            var firstPrimaryAction = firstRecord.SpiritDay.PrimaryAction;

            var regenerated = service.RegenerateCurrentDay();

            Assert.That(firstBalance, Is.EqualTo(18));
            Assert.That(service.EssenceWallet.Balance, Is.EqualTo(firstBalance));
            Assert.That(
                service.EssenceWallet.GetHighestReward(service.CurrentDateKey),
                Is.EqualTo(18));
            Assert.That(regenerated.GrantedEssenceDelta, Is.Zero);
            Assert.That(regenerated.RewardAcknowledged, Is.True);
            Assert.That(regenerated.SpiritDay.Seed, Is.EqualTo(firstSeed));
            Assert.That(regenerated.SpiritDay.PrimaryAction, Is.EqualTo(firstPrimaryAction));
            Assert.That(service.SaveData.Records.Count, Is.EqualTo(1));
            Assert.That(service.SaveData.Records[0], Is.SameAs(regenerated));
        }

        private static PrototypeApplicationService CreateService(
            InMemorySpiritSaveRepository repository)
        {
            var todoSource = new MockTodoCompletionSource(MockTodoPreset.DemoDay1WorkFocus);
            var classifier = new TaskClassifier();
            return new PrototypeApplicationService(
                todoSource,
                repository,
                classifier,
                new SpiritDayGenerator(classifier),
                DemoStartDate);
        }

        private static void AssertCandidateScoresEqual(
            DailyCompanionRecord first,
            DailyCompanionRecord second)
        {
            Assert.That(second.CandidateActions.Count, Is.EqualTo(first.CandidateActions.Count));
            for (var i = 0; i < first.CandidateActions.Count; i++)
            {
                Assert.That(
                    second.CandidateActions[i].Action,
                    Is.EqualTo(first.CandidateActions[i].Action));
                Assert.That(
                    second.CandidateActions[i].TotalScore,
                    Is.EqualTo(first.CandidateActions[i].TotalScore));
                Assert.That(
                    second.CandidateActions[i].SelectionWeight,
                    Is.EqualTo(first.CandidateActions[i].SelectionWeight));
            }
        }

        private sealed class InMemorySpiritSaveRepository : ISpiritSaveRepository
        {
            public string SavePath => "memory://prototype-save.json";

            public int SaveCallCount { get; private set; }

            public PrototypeSaveData StoredData { get; private set; } = new PrototypeSaveData();

            public PrototypeSaveData Load()
            {
                return StoredData;
            }

            public void Save(PrototypeSaveData saveData)
            {
                StoredData = saveData ?? throw new ArgumentNullException(nameof(saveData));
                SaveCallCount++;
            }

            public void ResetTracking()
            {
                SaveCallCount = 0;
            }
        }
    }
}
