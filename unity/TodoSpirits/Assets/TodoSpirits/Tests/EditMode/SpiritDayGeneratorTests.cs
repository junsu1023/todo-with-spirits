using System.Collections.Generic;
using NUnit.Framework;

namespace TodoSpirits.Core.Tests
{
    public sealed class SpiritDayGeneratorTests
    {
        [Test]
        public void Generate_SameInputsProduceSameDay()
        {
            var tasks = CreateTasks();
            var spirit = CreateSpirit();
            var generator = new SpiritDayGenerator();

            var first = generator.Generate("2026-08-31", tasks, spirit);
            var second = generator.Generate("2026-08-31", tasks, spirit);

            Assert.That(second.SpiritDay.Seed, Is.EqualTo(first.SpiritDay.Seed));
            Assert.That(second.SpiritDay.PrimaryAction, Is.EqualTo(first.SpiritDay.PrimaryAction));
            CollectionAssert.AreEqual(
                first.SpiritDay.SecondaryActions,
                second.SpiritDay.SecondaryActions);
            Assert.That(second.SpiritDay.Location, Is.EqualTo(first.SpiritDay.Location));
            Assert.That(second.SpiritDay.Dialogue, Is.EqualTo(first.SpiritDay.Dialogue));
            Assert.That(second.SelectionReason, Is.EqualTo(first.SelectionReason));
            Assert.That(second.CandidateActions.Count, Is.EqualTo(5));

            for (var i = 0; i < first.CandidateActions.Count; i++)
            {
                Assert.That(
                    second.CandidateActions[i].TotalScore,
                    Is.EqualTo(first.CandidateActions[i].TotalScore));
                Assert.That(
                    second.CandidateActions[i].Reason,
                    Is.EqualTo(first.CandidateActions[i].Reason));
            }
        }

        [Test]
        public void Generate_TaskOrderingDoesNotChangeStableSelection()
        {
            var tasks = CreateTasks();
            var reversedTasks = new List<CompletedTask>(tasks);
            reversedTasks.Reverse();
            var generator = new SpiritDayGenerator();

            var first = generator.Generate("2026-08-31", tasks, CreateSpirit());
            var reordered = generator.Generate("2026-08-31", reversedTasks, CreateSpirit());

            Assert.That(reordered.SpiritDay.Seed, Is.EqualTo(first.SpiritDay.Seed));
            Assert.That(reordered.SpiritDay.PrimaryAction, Is.EqualTo(first.SpiritDay.PrimaryAction));
            CollectionAssert.AreEqual(
                first.SpiritDay.SecondaryActions,
                reordered.SpiritDay.SecondaryActions);
        }

        [Test]
        public void DayGeneration_IsIndependentOfDebugVisitOrder()
        {
            var baseState = new SpiritState(
                "prototype-spirit",
                new[] { SpiritTemperament.Curious, SpiritTemperament.Meticulous },
                new[] { SpiritActionId.Rest },
                SpiritActionId.ReadRecords);
            var oldest = CreateRecord(
                "prototype-spirit",
                "2026-08-28",
                SpiritActionId.ReadRecords,
                10);
            var middle = CreateRecord(
                "prototype-spirit",
                "2026-08-29",
                SpiritActionId.CraftRepair,
                20);
            var newest = CreateRecord(
                "prototype-spirit",
                "2026-08-30",
                SpiritActionId.WalkForest,
                30);
            var otherSpirit = CreateRecord(
                "other-spirit",
                "2026-08-30",
                SpiritActionId.SocialTea,
                40);
            var targetDateRecord = CreateRecord(
                "prototype-spirit",
                "2026-08-31",
                SpiritActionId.Rest,
                50);
            var firstVisitOrder = new List<DailyCompanionRecord>
            {
                newest,
                otherSpirit,
                oldest,
                targetDateRecord,
                middle
            };
            var secondVisitOrder = new List<DailyCompanionRecord>
            {
                middle,
                targetDateRecord,
                oldest,
                otherSpirit,
                newest
            };

            var firstSnapshot = SpiritHistorySnapshot.Build(
                baseState,
                firstVisitOrder,
                "2026-08-31");
            var secondSnapshot = SpiritHistorySnapshot.Build(
                baseState,
                secondVisitOrder,
                "2026-08-31");

            CollectionAssert.AreEqual(
                new[]
                {
                    SpiritActionId.ReadRecords,
                    SpiritActionId.CraftRepair,
                    SpiritActionId.WalkForest
                },
                firstSnapshot.RecentPrimaryActions);
            CollectionAssert.AreEqual(
                firstSnapshot.RecentPrimaryActions,
                secondSnapshot.RecentPrimaryActions);
            CollectionAssert.AreEqual(
                new[] { SpiritActionId.Rest },
                baseState.RecentPrimaryActions);
            Assert.That(firstVisitOrder[0], Is.SameAs(newest));
            Assert.That(secondVisitOrder[0], Is.SameAs(middle));

            var generator = new SpiritDayGenerator();
            var firstReport = generator.Generate(
                "2026-08-31",
                CreateTasks(),
                firstSnapshot);
            var secondReport = generator.Generate(
                "2026-08-31",
                CreateTasks(),
                secondSnapshot);

            Assert.That(secondReport.SpiritDay.Seed, Is.EqualTo(firstReport.SpiritDay.Seed));
            Assert.That(
                secondReport.SpiritDay.PrimaryAction,
                Is.EqualTo(firstReport.SpiritDay.PrimaryAction));
            CollectionAssert.AreEqual(
                firstReport.SpiritDay.SecondaryActions,
                secondReport.SpiritDay.SecondaryActions);

            for (var i = 0; i < firstReport.CandidateActions.Count; i++)
            {
                Assert.That(
                    secondReport.CandidateActions[i].SelectionWeight,
                    Is.EqualTo(firstReport.CandidateActions[i].SelectionWeight));
                Assert.That(
                    secondReport.CandidateActions[i].Reason,
                    Is.EqualTo(firstReport.CandidateActions[i].Reason));
            }
        }

        [Test]
        public void Generate_SameInputDifferentProfileChangesCandidateWeightAndReason()
        {
            var profileA = new SpiritState(
                "prototype-spirit",
                new[] { SpiritTemperament.Curious, SpiritTemperament.Meticulous },
                new[] { SpiritActionId.Rest },
                SpiritActionId.ReadRecords);
            var profileB = new SpiritState(
                "prototype-spirit",
                new[] { SpiritTemperament.Active, SpiritTemperament.Sociable },
                new[] { SpiritActionId.Rest },
                SpiritActionId.ReadRecords);
            var generator = new SpiritDayGenerator();

            var profileAReport = generator.Generate("2026-08-31", CreateTasks(), profileA);
            var profileBReport = generator.Generate("2026-08-31", CreateTasks(), profileB);
            var profileASocialTea = FindCandidate(
                profileAReport,
                SpiritActionId.SocialTea);
            var profileBSocialTea = FindCandidate(
                profileBReport,
                SpiritActionId.SocialTea);

            Assert.That(profileASocialTea.TemperamentScore, Is.Zero);
            Assert.That(profileBSocialTea.TemperamentScore, Is.EqualTo(8f));
            Assert.That(
                profileBSocialTea.SelectionWeight,
                Is.Not.EqualTo(profileASocialTea.SelectionWeight));
            Assert.That(profileBSocialTea.Reason, Is.Not.EqualTo(profileASocialTea.Reason));
            Assert.That(profileBSocialTea.Reason, Does.Contain("기질 8"));
        }

        [Test]
        public void Generate_WithNoTasksFallsBackToRest()
        {
            var spirit = new SpiritState(
                "prototype-spirit",
                new[] { SpiritTemperament.Curious, SpiritTemperament.Meticulous },
                new[] { SpiritActionId.Rest, SpiritActionId.Rest },
                SpiritActionId.ReadRecords);

            var report = new SpiritDayGenerator().Generate(
                "2026-08-31",
                new List<CompletedTask>(),
                spirit);

            Assert.That(report.SpiritDay.PrimaryAction, Is.EqualTo(SpiritActionId.Rest));
            Assert.That(report.SpiritDay.SecondaryActions, Is.Empty);
            Assert.That(report.SpiritDay.TaskActionLinks, Is.Empty);
            Assert.That(report.SpiritDay.Location, Is.Not.Empty);
            Assert.That(
                report.SpiritDay.Dialogue,
                Is.EqualTo(SpiritActionCatalog.ResolveDialogue(
                    SpiritActionId.Rest,
                    spirit.Temperaments)));
            Assert.That(report.SpiritDay.Dialogue, Does.Contain("내일의 빛"));
            Assert.That(report.CandidateActions.Count, Is.EqualTo(5));
        }

        [Test]
        public void Generate_ActiveProfileUsesResolvedDialogue()
        {
            var tasks = new List<CompletedTask>
            {
                new CompletedTask(
                    "task-walk",
                    "30분 산책",
                    UserTaskCategory.Health,
                    "2026-08-31T18:00:00+09:00")
            };
            var spirit = new SpiritState(
                "prototype-spirit",
                new[] { SpiritTemperament.Active, SpiritTemperament.Sociable },
                new List<SpiritActionId>(),
                SpiritActionId.ReadRecords);

            var report = new SpiritDayGenerator().Generate("2026-08-31", tasks, spirit);

            Assert.That(report.SpiritDay.PrimaryAction, Is.EqualTo(SpiritActionId.WalkForest));
            Assert.That(
                report.SpiritDay.Dialogue,
                Is.EqualTo(SpiritActionCatalog.ResolveDialogue(
                    SpiritActionId.WalkForest,
                    spirit.Temperaments)));
            Assert.That(report.SpiritDay.Dialogue, Does.Contain("힘차게"));
        }

        private static List<CompletedTask> CreateTasks()
        {
            return new List<CompletedTask>
            {
                new CompletedTask(
                    "task-plan",
                    "기획서 수정",
                    UserTaskCategory.WorkStudy,
                    "2026-08-31T09:00:00+09:00"),
                new CompletedTask(
                    "task-walk",
                    "30분 산책",
                    UserTaskCategory.Health,
                    "2026-08-31T18:00:00+09:00"),
                new CompletedTask(
                    "task-dinner",
                    "친구와 저녁",
                    UserTaskCategory.Relationships,
                    "2026-08-31T19:30:00+09:00")
            };
        }

        private static SpiritState CreateSpirit()
        {
            return new SpiritState(
                "prototype-spirit",
                new[] { SpiritTemperament.Curious, SpiritTemperament.Meticulous },
                new[] { SpiritActionId.WalkForest },
                SpiritActionId.ReadRecords);
        }

        private static DailyCompanionRecord CreateRecord(
            string spiritId,
            string date,
            SpiritActionId primaryAction,
            int seed)
        {
            return new DailyCompanionRecord
            {
                SpiritId = spiritId,
                Date = date,
                SpiritDay = new SpiritDayResult
                {
                    Date = date,
                    PrimaryAction = primaryAction,
                    Seed = seed
                }
            };
        }

        private static ActionCandidateScore FindCandidate(
            DayGenerationReport report,
            SpiritActionId action)
        {
            for (var i = 0; i < report.CandidateActions.Count; i++)
            {
                if (report.CandidateActions[i].Action == action)
                {
                    return report.CandidateActions[i];
                }
            }

            Assert.Fail("Candidate action was not generated: " + action);
            return null;
        }
    }
}
