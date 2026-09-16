using System;
using NUnit.Framework;

namespace TodoSpirits.Core.Tests
{
    public sealed class PrototypeCoverageTests
    {
        [Test]
        public void GrowthExpandsPlacesWithoutChangingActionAndHintsNeedActualPattern()
        {
            var first = new System.Collections.Generic.HashSet<string>();
            var later = new System.Collections.Generic.HashSet<string>();
            foreach (SpiritActionId action in Enum.GetValues(typeof(SpiritActionId)))
            {
                first.Add(CompanionStagePresentation.PlaceName(CompanionStage.Meeting, action));
                later.Add(CompanionStagePresentation.PlaceName(CompanionStage.Adapting, action));
            }
            Assert.That(first.Count, Is.EqualTo(3));
            Assert.That(later.Count, Is.EqualTo(5));
            var life = new CompanionLife { Stage = CompanionStage.Interests };
            Assert.That(CompanionStagePresentation.Hint(life), Does.Contain("찾고 있어요"));
            life.Actions.Add(new LifeActionCount { Action = SpiritActionId.WalkForest, Count = 3 });
            Assert.That(CompanionStagePresentation.Hint(life), Does.Contain(SpiritActionCatalog.Get(SpiritActionId.WalkForest).Location));
            life.Stage = CompanionStage.Preparing;
            string reasons = string.Join("|", life.RouteReasons);
            var originalRoute = life.Route;
            Assert.That(CompanionLifeRules.PreviewRoute(life), Is.EqualTo(AdultRoute.Explorer));
            Assert.That(CompanionStagePresentation.Hint(life), Does.Contain("가방"));
            Assert.That(life.Route, Is.EqualTo(originalRoute));
            Assert.That(string.Join("|", life.RouteReasons), Is.EqualTo(reasons));
        }
        [Test]
        public void RememberedCorrectionWinsOverRoutineAndKeywords()
        {
            var memory = new TaskClassificationMemory();
            var classifier = new TaskClassifier();
            var task = new CompletedTask("first", "독서", UserTaskCategory.WorkStudy, "") { RoutineId = "routine" };
            classifier.Classify(new[] { task }, memory);
            task = new CompletedTask("next", "프로젝트 제작", UserTaskCategory.WorkStudy, "") { RoutineId = "routine" };
            Assert.That(classifier.Classify(task, memory).ActionTags[0], Is.EqualTo(SpiritActionId.ReadRecords));
            memory.Correct(task, SpiritActionId.SocialTea);
            var corrected = classifier.Classify(task, memory);
            Assert.That(corrected.ActionTags[0], Is.EqualTo(SpiritActionId.SocialTea));
            Assert.That(corrected.Reason, Does.Contain("수정"));
            var renamedRoutine = new CompletedTask("fourth", "다른 제목", UserTaskCategory.WorkStudy, "") { RoutineId = "routine" };
            Assert.That(classifier.Classify(renamedRoutine, memory).ActionTags[0], Is.EqualTo(SpiritActionId.SocialTea));
            var similar = new CompletedTask("third", " 프로젝트  제작 ", UserTaskCategory.WorkStudy, "");
            Assert.That(classifier.Classify(similar, memory).ActionTags[0], Is.EqualTo(SpiritActionId.SocialTea));
            similar.UserCategory = UserTaskCategory.Hobby;
            Assert.That(classifier.Classify(similar, memory).ActionTags[0], Is.EqualTo(SpiritActionId.CraftRepair));
        }

        [Test]
        public void InvalidMemoryFallsBackAndExactIdentityWinsOverTitle()
        {
            var memory = new TaskClassificationMemory();
            var task = new CompletedTask("a", "독서", UserTaskCategory.WorkStudy, "");
            memory.Correct(task, SpiritActionId.Rest);
            task.Title = "산책";
            memory.Correct(new CompletedTask("b", "산책", UserTaskCategory.WorkStudy, ""), SpiritActionId.WalkForest);
            Assert.That(new TaskClassifier().Classify(task, memory).ActionTags[0], Is.EqualTo(SpiritActionId.Rest));
            memory.Corrections[0].Action = (SpiritActionId)999;
            task.Title = "독서";
            Assert.That(new TaskClassifier().Classify(task, memory).ActionTags[0], Is.EqualTo(SpiritActionId.ReadRecords));
            Assert.Throws<ArgumentOutOfRangeException>(() => memory.Correct(task, (SpiritActionId)999));
        }
        [TestCase(UserTaskCategory.WorkStudy, SpiritActionId.CraftRepair)]
        [TestCase(UserTaskCategory.Health, SpiritActionId.WalkForest)]
        [TestCase(UserTaskCategory.Living, SpiritActionId.CraftRepair)]
        [TestCase(UserTaskCategory.Relationships, SpiritActionId.SocialTea)]
        [TestCase(UserTaskCategory.SelfDevelopment, SpiritActionId.ReadRecords)]
        [TestCase(UserTaskCategory.Hobby, SpiritActionId.CraftRepair)]
        [TestCase(UserTaskCategory.RestMind, SpiritActionId.Rest)]
        [TestCase(UserTaskCategory.Assets, SpiritActionId.ReadRecords)]
        [TestCase(UserTaskCategory.Economy, SpiritActionId.ReadRecords)]
        public void AllCategoriesHaveDefaults(UserTaskCategory category, SpiritActionId expected)
        {
            var result=new TaskClassifier().Classify(new CompletedTask("id","키워드 없는 제목",category,""));
            Assert.That(result.ActionTags,Does.Contain(expected));
            Assert.That(result.ClassificationConfidence,Is.GreaterThan(.5f));
        }
        [Test]
        public void DailyPresentationVariesAcrossDaysWithoutChangingSelectedAction()
        {
            foreach(SpiritActionId action in Enum.GetValues(typeof(SpiritActionId)))
            {
                var lines=new System.Collections.Generic.HashSet<string>();
                var poses=new System.Collections.Generic.HashSet<int>();
                for(int i=0;i<3;i++)
                {
                    var day=new SpiritDayResult { Date=new DateTime(2026,9,9).AddDays(i).ToString("yyyy-MM-dd"),PrimaryAction=action,Seed=42 };
                    SpiritDayPresentation.Apply(day,"same-spirit");
                    lines.Add(day.Dialogue); poses.Add(day.PresentationVariant);
                    string original=day.Dialogue;
                    SpiritDayPresentation.Apply(day,"same-spirit");
                    Assert.That(day.Dialogue,Is.EqualTo(original));
                    Assert.That(day.PrimaryAction,Is.EqualTo(action));
                    Assert.That(day.Seed,Is.EqualTo(42));
                }
                Assert.That(lines.Count,Is.EqualTo(3));
                Assert.That(poses.Count,Is.EqualTo(3));
            }
        }

        [Test]
        public void AllEightTemperamentsAffectScoresAndHaveDistinctReadableDescriptions()
        {
            var descriptions=new System.Collections.Generic.HashSet<string>();
            var reactions=new System.Collections.Generic.HashSet<string>();
            foreach(SpiritTemperament trait in Enum.GetValues(typeof(SpiritTemperament)))
            {
                if(trait==SpiritTemperament.Unspecified) continue;
                var traits=new[]{trait};
                var spirit=new SpiritState("id",traits,Array.Empty<SpiritActionId>(),SpiritActionId.Rest);
                var result=new SpiritDayGenerator().Generate("2026-09-09",new[]{new CompletedTask("a","독서",UserTaskCategory.WorkStudy,"")},spirit);
                Assert.That(result.CandidateActions.Exists(c=>c.TemperamentScore>0),Is.True,trait.ToString());
                descriptions.Add(CompanionTemperamentText.Describe(trait));
                reactions.Add(CompanionTemperamentText.GiftReaction(traits,"key"));
            }
            Assert.That(descriptions.Count,Is.EqualTo(8));
            Assert.That(reactions.Count,Is.EqualTo(8));
        }
        [Test]
        public void ThirdTemperamentIsIncludedInScoring()
        {
            var spirit=new SpiritState("id",new[]{SpiritTemperament.Curious,SpiritTemperament.Meticulous,SpiritTemperament.Active},Array.Empty<SpiritActionId>(),SpiritActionId.Rest);
            var result=new SpiritDayGenerator().Generate("2026-09-09",new[]{new CompletedTask("a","산책",UserTaskCategory.Health,"")},spirit);
            Assert.That(result.CandidateActions.Find(c=>c.Action==SpiritActionId.WalkForest).TemperamentScore,Is.EqualTo(11));
        }
    }
}
