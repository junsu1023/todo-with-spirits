using System;
using System.Collections.Generic;
using System.Linq;
using NUnit.Framework;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class ForestStoryBranchPersistenceTests
    {
        private const string Account = "story-branch-reader";
        private sealed class MemorySave : ISpiritSaveRepository
        {
            private string _json;
            public string SavePath => "story-branch-tests";
            public PrototypeSaveData Load() => _json == null ? new PrototypeSaveData() : JsonUtility.FromJson<PrototypeSaveData>(_json);
            public void Save(PrototypeSaveData data) => _json = JsonUtility.ToJson(data);
        }

        [Serializable]
        private sealed class ObservedScene
        {
            public string Date, SpiritId, ObservedAtUtc, PrimaryActivity, VisiblePropName;
            public long ObservedRevision;
            public CompanionStage AppearancePhase;
            public ForestStoryFamily StoryFamily;
            public int ChoiceCount, LastChoiceOption;
            public List<ForestDailyActivity> ObservedActivities;
            public List<string> Acquisitions;
        }

        private sealed class BranchRun
        {
            public MemorySave Save = new MemorySave();
            public PrototypeApplicationService App;
            public Dictionary<string, string> Scenes = new Dictionary<string, string>();
            public List<string> Captions = new List<string>();
        }

        private static IEnumerable<TestCaseData> BranchCases()
        {
            foreach (var family in new[] { ForestStoryFamily.Focus, ForestStoryFamily.Movement, ForestStoryFamily.Everyday })
                for (int first = 0; first < 2; first++)
                    for (int second = 0; second < 2; second++)
                        for (int ending = 0; ending < 3; ending++)
                            yield return new TestCaseData(family, first, second, ending)
                                .SetName("StoryBranch_" + family + "_" + first + second + ending + "_RestoresEachChoiceAndNextVisit");
        }

        [TestCaseSource(nameof(BranchCases))]
        public void EveryBranchRestoresImmediateResultPropFollowupAndNextVisit(ForestStoryFamily family, int first, int second, int ending)
        {
            var run = PlayThroughNextVisit(family, first, second, ending);
            Assert.That(run.App.CurrentLife.Story.Choices.Select(c => c.Option), Is.EqualTo(new[] { first, second, ending }));
            Assert.That(run.App.CurrentLife.Story.Choices.Select(c => c.Caption), Is.EqualTo(run.Captions));
            Assert.That(run.App.CurrentLife.Route, Is.EqualTo((AdultRoute)ending));
            Assert.That(run.App.HasForestChoice, Is.False, "The final choice completes the three-scene story; Question is not a fourth event.");
            Assert.That(run.App.EssenceWallet.Balance, Is.EqualTo(80), "Only eight actual completed days grant essence.");
            AssertFrozenScenes(run);
        }

        [TestCase(ForestStoryFamily.Focus)]
        [TestCase(ForestStoryFamily.Movement)]
        [TestCase(ForestStoryFamily.Everyday)]
        public void EarlierSceneKeepsItsChoicePropAndChildAppearanceAfterAdultGrowthAndHistoricalCorrection(ForestStoryFamily family)
        {
            var run = PlayThroughNextVisit(family, 0, 1, 2);
            string spirit = run.App.CurrentLife.SpiritId;
            for (int day = 9; day <= 21; day++)
            {
                run.App.AdvanceLifeDay();
                run.App.ApplyTodoSnapshot(Snapshot(run.App.CurrentDateKey, day, family));
                run.App.AcknowledgeForestToday();
            }
            run.App.AdvanceLifeDay();
            run.App = Open(run.Save);
            Assert.That(run.App.CurrentLife.Stage, Is.EqualTo(CompanionStage.Adult));
            Assert.That(ForestStoryRules.VisiblePropName(run.App.CurrentLife), Is.EqualTo("손으로 만든 기념물"));
            AssertFrozenScenes(run);

            var corrected = Snapshot("2026-01-25", 100, family);
            corrected.items[0].title = "나중에 정정한 원본 제목";
            corrected.items[0].category = "REST";
            corrected.items[0].isCompleted = false;
            int balance = run.App.EssenceWallet.Balance;
            Assert.That(run.App.CorrectForestDayFacts(corrected), Is.True);
            run.App = Open(run.Save);

            var firstDay = run.App.SaveData.Forest.Days.Single(d => d.SpiritId == spirit && d.Date == "2026-01-25");
            Assert.That(firstDay.Todos[0].Title, Is.EqualTo("나중에 정정한 원본 제목"));
            Assert.That(firstDay.Todos[0].IsCompleted, Is.False);
            Assert.That(firstDay.Revision, Is.EqualTo(100));
            Assert.That(firstDay.ObservedRevision, Is.EqualTo(1));
            Assert.That(firstDay.AppearancePhase, Is.EqualTo(CompanionStage.Meeting));
            Assert.That(firstDay.ChoiceCount, Is.EqualTo(1));
            Assert.That(firstDay.LastChoiceOption, Is.Zero);
            Assert.That(firstDay.VisiblePropName, Is.EqualTo(FirstProp(family, 0)));
            Assert.That(run.App.CurrentLife.Story.Choices.Select(c => c.Caption), Is.EqualTo(run.Captions));
            Assert.That(run.App.EssenceWallet.Balance, Is.EqualTo(balance));
            AssertFrozenScenes(run);
        }

        private static BranchRun PlayThroughNextVisit(ForestStoryFamily family, int first, int second, int ending)
        {
            var run = new BranchRun();
            run.App = Open(run.Save);
            int[] choices = { first, second, ending };
            for (int day = 1; day <= 8; day++)
            {
                var snapshot = Snapshot(run.App.CurrentDateKey, day, family);
                run.App.ApplyTodoSnapshot(snapshot);
                int episode = day == 1 ? 0 : day == 4 ? 1 : day == 7 ? 2 : -1;
                if (episode >= 0)
                {
                    Assert.That(run.App.HasForestChoice, Is.True);
                    Assert.That(run.App.CurrentLife.Story.Family, Is.EqualTo(family));
                    Assert.That(ForestStoryRules.Options(run.App.CurrentLife), Has.Length.EqualTo(episode == 2 ? 3 : 2));
                    string originalFacts = JsonUtility.ToJson(run.App.SaveData.Forest.Todos[0]);
                    int balance = run.App.EssenceWallet.Balance;
                    string caption = run.App.ChooseForestStory(choices[episode]);
                    Assert.That(caption, Is.EqualTo(ExpectedCaption(family, episode, choices[episode])));
                    Assert.That(JsonUtility.ToJson(run.App.SaveData.Forest.Todos[0]), Is.EqualTo(originalFacts));
                    Assert.That(run.App.EssenceWallet.Balance, Is.EqualTo(balance));
                    run.Captions.Add(caption);
                    run.App.AcknowledgeForestToday();
                    var memory = run.App.SaveData.Forest.Days.Single(d => d.Date == snapshot.date && d.SpiritId == run.App.CurrentLife.SpiritId);
                    Assert.That(memory.Acquisitions, Does.Contain(caption));
                    Assert.That(memory.ChoiceCount, Is.EqualTo(episode + 1));
                    Assert.That(memory.LastChoiceOption, Is.EqualTo(choices[episode]));
                    Assert.That(memory.StoryFamily, Is.EqualTo(family));
                    Assert.That(memory.VisiblePropName, Is.EqualTo(ExpectedProp(family, episode, choices[episode])));
                    Assert.That(memory.AppearancePhase, Is.EqualTo(new[] { CompanionStage.Meeting, CompanionStage.Adapting, CompanionStage.Interests }[episode]));
                    Assert.That(memory.ObservedAtUtc, Is.Not.Empty);
                    Assert.That(memory.ObservedActivities.Any(a => a.IsCompleted && a.SourceTodoIds.Contains("actual-habit")), Is.True);
                    run.Scenes.Add(snapshot.date, Projection(memory));

                    // The snapshot sender may resend the same revision after restarting the process.
                    run.App = Open(run.Save);
                    Assert.That(run.App.ApplyTodoSnapshot(snapshot), Is.False);
                    AssertBranchState(run.App, family, episode, choices[episode]);
                    Assert.That(run.App.CurrentLife.Story.Choices[episode].Caption, Is.EqualTo(caption));
                    Assert.That(run.App.CurrentLife.Story.Choices[episode].Date, Is.EqualTo(snapshot.date));
                    Assert.That(run.App.CurrentLife.Story.Choices[episode].Episode, Is.EqualTo(episode));
                    Assert.That(run.App.GetForestTodaySummary().NewEssence, Is.Zero);
                    Assert.That(run.App.GetForestTodaySummary().Acquisitions, Is.Empty);
                    AssertFollowupAppearsInTouchDialogue(run.App, ExpectedFollowup(family, episode, choices[episode]));
                }
                else
                {
                    run.App.AcknowledgeForestToday();
                    if (day == 2 || day == 5 || day == 8)
                    {
                        int previousEpisode = day == 2 ? 0 : day == 5 ? 1 : 2;
                        run.App = Open(run.Save);
                        Assert.That(run.App.ApplyTodoSnapshot(snapshot), Is.False);
                        AssertBranchState(run.App, family, previousEpisode, choices[previousEpisode]);
                        AssertFollowupAppearsInTouchDialogue(run.App, ExpectedFollowup(family, previousEpisode, choices[previousEpisode]));
                    }
                }
                AssertFrozenScenes(run);
                if (day < 8) run.App.AdvanceLifeDay();
            }
            return run;
        }

        private static void AssertBranchState(PrototypeApplicationService app, ForestStoryFamily family, int episode, int option)
        {
            Assert.That(app.CurrentLife.Story.Family, Is.EqualTo(family));
            Assert.That(ForestStoryRules.VisiblePropName(app.CurrentLife), Is.EqualTo(ExpectedProp(family, episode, option)));
            Assert.That(ForestStoryRules.ContinuingDialogue(app.CurrentLife), Is.EqualTo(ExpectedFollowup(family, episode, option)));
            Assert.That(app.HasForestChoice, Is.False, "A selected event must not reopen on the same day or following visit.");
            if (episode < 2)
                Assert.That(ForestStoryRules.Question(app.CurrentLife), Does.Contain(NextQuestionFragment(family, episode, option)));
            else Assert.That(app.CurrentLife.Route, Is.EqualTo((AdultRoute)option));
        }

        private static void AssertFollowupAppearsInTouchDialogue(PrototypeApplicationService app, string expected)
        {
            var lines = Enumerable.Range(0, 3).Select(_ => app.NextForestDialogue()).ToList();
            Assert.That(lines, Does.Contain(expected), "The restored result must reach the public touch dialogue, not only a story helper.");
        }

        private static void AssertFrozenScenes(BranchRun run)
        {
            foreach (var saved in run.Scenes)
            {
                var memory = run.App.SaveData.Forest.Days.Single(d => d.Date == saved.Key && d.SpiritId == run.App.CurrentLife.SpiritId);
                Assert.That(Projection(memory), Is.EqualTo(saved.Value), "Later choices or appearance must not rewrite " + saved.Key);
            }
        }

        private static string Projection(ForestDailyMemory memory) => JsonUtility.ToJson(new ObservedScene {
            Date = memory.Date, SpiritId = memory.SpiritId, ObservedAtUtc = memory.ObservedAtUtc,
            ObservedRevision = memory.ObservedRevision, AppearancePhase = memory.AppearancePhase,
            PrimaryActivity = memory.PrimaryActivity, StoryFamily = memory.StoryFamily,
            ChoiceCount = memory.ChoiceCount, LastChoiceOption = memory.LastChoiceOption,
            VisiblePropName = memory.VisiblePropName, ObservedActivities = memory.ObservedActivities,
            Acquisitions = memory.Acquisitions
        });

        private static PrototypeApplicationService Open(MemorySave save)
        {
            var app = new PrototypeApplicationService(new EmptyTodoCompletionSource(), save, new TaskClassifier(),
                new SpiritDayGenerator(), new DateTime(2026, 1, 25), true);
            app.Initialize();
            if (!app.SaveData.Forest.Enabled) app.EnableForest();
            return app;
        }

        private static ForestTodoSnapshot Snapshot(string date, long revision, ForestStoryFamily family) => new ForestTodoSnapshot {
            schemaVersion = 1, accountId = Account, date = date, revision = revision,
            items = new List<ForestTodoSnapshotItem> {
                new ForestTodoSnapshotItem { id = "actual-habit", title = "내가 마친 원본 약속", isHabit = true, isCompleted = true,
                    category = family == ForestStoryFamily.Focus ? "WORK_STUDY" : family == ForestStoryFamily.Movement ? "HEALTH" : "NONE" },
                new ForestTodoSnapshotItem { id = "planned", title = "아직 하지 않은 원본 약속", isHabit = true, isCompleted = false, category = "NONE" }
            }
        };

        private static string FirstProp(ForestStoryFamily family, int option)
        {
            if (family == ForestStoryFamily.Focus) return option == 0 ? "관찰 수첩" : "관찰 가방";
            if (family == ForestStoryFamily.Movement) return option == 0 ? "공터 산책 지도" : "작은 길 표식";
            return option == 0 ? "따뜻한 등불" : "바람 장식";
        }

        private static string ExpectedProp(ForestStoryFamily family, int episode, int option) =>
            episode == 0 ? FirstProp(family, option) : episode == 1
                ? option == 0 ? "이웃에게 보낸 초대 편지" : "우리만의 기억 표식"
                : new[] { "작은 이야기 책", "우리 숲의 지도", "손으로 만든 기념물" }[option];

        private static string ExpectedCaption(ForestStoryFamily family, int episode, int option)
        {
            if (episode == 0)
            {
                if (family == ForestStoryFamily.Focus) return option == 0
                    ? "관찰 수첩을 책상에 펼쳤어요. 다음 발견을 이어 그릴 거예요."
                    : "관찰 가방을 챙겼어요. 다음 숲길에서 닮은 잎을 찾아볼 거예요.";
                if (family == ForestStoryFamily.Movement) return option == 0
                    ? "공터까지의 지도를 그렸어요. 다음 산책도 이 지도를 펼쳐 볼 거예요."
                    : "숲길에 작은 표식을 세웠어요. 다음에는 표식 옆의 풍경을 살펴볼 거예요.";
                return option == 0 ? "우리 자리에 작은 등불을 놓았어요. 다음 방문에도 따뜻한 빛이 반겨 줄 거예요."
                    : "우리 자리에 바람 장식을 매달았어요. 다음 방문에도 바람과 함께 움직일 거예요.";
            }
            if (episode == 1) return option == 0
                ? "이웃에게 초대 편지를 보냈어요. 편지가 티테이블에 남고, 다음 이야기는 답장을 기다려요."
                : "우리만의 기억 표식을 남겼어요. 다음 이야기에서 이 약속을 다시 꺼내 볼 거예요.";
            return new[] { "작은 이야기 책", "우리 숲의 지도", "손으로 만든 기념물" }[option]
                + "이 완성됐어요. 함께 고른 방법과 오늘의 기록이 앨범에 남아요.";
        }

        private static string ExpectedFollowup(ForestStoryFamily family, int episode, int option)
        {
            if (episode == 0) return "우리가 고른 " + FirstProp(family, option) + "을 오늘도 챙겼어. 다음 발견에 함께 써 보자.";
            if (episode == 1) return option == 0 ? "함께 보낸 초대 편지에 어떤 답장이 올까? 티테이블에 자리를 남겨 뒀어."
                : "우리만의 기억 표식을 오늘도 살펴봤어. 다음 이야기도 여기에 이어 두자.";
            return "함께 만든 " + new[] { "작은 이야기 책", "우리 숲의 지도", "손으로 만든 기념물" }[option]
                + ", 오늘도 곁에 두었어. 다음 기억도 함께 만들자.";
        }

        private static string NextQuestionFragment(ForestStoryFamily family, int episode, int option)
        {
            if (episode == 0)
            {
                if (family == ForestStoryFamily.Focus) return option == 0 ? "수첩에 그린 무늬" : "탐험 가방에 담아 온 잎";
                if (family == ForestStoryFamily.Movement) return option == 0 ? "우리가 그린 지도" : "우리가 세운 길 표식";
                return option == 0 ? "함께 만든 작은 등불" : "함께 매단 바람 장식";
            }
            if (family == ForestStoryFamily.Focus) return option == 0 ? "관찰 편지에 답장" : "우리만의 표식 옆";
            if (family == ForestStoryFamily.Movement) return option == 0 ? "이웃도 우리의 산책길" : "우리만 아는 공터";
            return option == 0 ? "이웃과 나눈 작은 풍경" : "우리만의 자리에 추억";
        }
    }
}
