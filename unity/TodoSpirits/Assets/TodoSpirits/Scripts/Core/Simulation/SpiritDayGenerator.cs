using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;

namespace TodoSpirits.Core
{
    public sealed class SpiritDayGenerator
    {
        public const string DefaultSimulationVersion = "internal-demo-v0.1";

        private readonly TaskClassifier _taskClassifier;

        public SpiritDayGenerator()
            : this(new TaskClassifier())
        {
        }

        public SpiritDayGenerator(TaskClassifier taskClassifier)
        {
            _taskClassifier = taskClassifier ?? throw new ArgumentNullException(nameof(taskClassifier));
        }

        public DayGenerationReport Generate(
            string date,
            IEnumerable<CompletedTask> completedTasks,
            SpiritState spiritState,
            string simulationVersion = DefaultSimulationVersion)
        {
            var taskList = CopyTasks(completedTasks);
            var classifications = _taskClassifier.Classify(taskList);
            return Generate(date, taskList, classifications, spiritState, simulationVersion);
        }

        public DayGenerationReport Generate(
            string date,
            IEnumerable<CompletedTask> completedTasks,
            IEnumerable<TaskClassification> classifications,
            SpiritState spiritState,
            string simulationVersion = DefaultSimulationVersion)
        {
            if (string.IsNullOrWhiteSpace(date))
            {
                throw new ArgumentException("날짜는 비어 있을 수 없습니다.", nameof(date));
            }

            if (spiritState == null)
            {
                throw new ArgumentNullException(nameof(spiritState));
            }

            var taskList = CopyTasks(completedTasks);
            var classificationList = CopyClassifications(classifications);
            if (classificationList.Count == 0 && taskList.Count > 0)
            {
                classificationList = _taskClassifier.Classify(taskList);
            }

            var version = string.IsNullOrWhiteSpace(simulationVersion)
                ? DefaultSimulationVersion
                : simulationVersion;
            var seed = StableHash.ToSignedSeed(
                BuildCanonicalSeedInput(date, taskList, classificationList, spiritState, version));
            var candidates = ScoreAllCandidates(seed, taskList.Count, classificationList, spiritState);
            var rankedCandidates = new List<ActionCandidateScore>(candidates);
            rankedCandidates.Sort(CompareCandidateScores);

            int selectionRoll;
            int totalSelectionWeight;
            var primaryAction = SelectPrimaryAction(
                seed,
                taskList.Count,
                candidates,
                out selectionRoll,
                out totalSelectionWeight);
            var secondaryActions = SelectSecondaryActions(
                primaryAction,
                taskList.Count,
                rankedCandidates);
            var links = BuildTaskActionLinks(taskList, classificationList);
            var primaryDefinition = SpiritActionCatalog.Get(primaryAction);
            var selectionReason = BuildSelectionReason(
                taskList.Count,
                primaryAction,
                secondaryActions,
                rankedCandidates,
                selectionRoll,
                totalSelectionWeight);

            var day = new SpiritDayResult(
                date,
                primaryAction,
                secondaryActions,
                primaryDefinition.Location,
                SpiritActionCatalog.ResolveDialogue(primaryAction, spiritState.Temperaments),
                seed,
                links);

            return new DayGenerationReport
            {
                Date = date,
                SimulationVersion = version,
                SpiritDay = day,
                Classifications = classificationList,
                CandidateActions = candidates,
                SelectionReason = selectionReason
            };
        }

        public SpiritDayResult GenerateDay(
            string date,
            IEnumerable<CompletedTask> completedTasks,
            SpiritState spiritState,
            string simulationVersion = DefaultSimulationVersion)
        {
            return Generate(date, completedTasks, spiritState, simulationVersion).SpiritDay;
        }

        private static List<ActionCandidateScore> ScoreAllCandidates(
            int seed,
            int completedTaskCount,
            List<TaskClassification> classifications,
            SpiritState spiritState)
        {
            var candidates = new List<ActionCandidateScore>();
            var definitions = SpiritActionCatalog.All;

            for (var i = 0; i < definitions.Count; i++)
            {
                var action = definitions[i].Id;
                var baseScore = action == SpiritActionId.Rest ? 6f : 10f;
                var taskScore = CountActionTags(classifications, action) * 24f;
                if (completedTaskCount == 0 && action == SpiritActionId.Rest)
                {
                    taskScore += 40f;
                }

                var temperamentScore = GetTemperamentScore(spiritState.Temperaments, action);
                var favoriteScore = spiritState.FavoriteAction == action ? 3f : 0f;
                var repetitionPenalty = GetRepetitionPenalty(
                    spiritState.RecentPrimaryActions,
                    action);
                var jitter = GetDeterministicJitter(seed, action);
                var total = baseScore + taskScore + temperamentScore +
                            favoriteScore + repetitionPenalty + jitter;
                var eligibleForPrimary = completedTaskCount > 0 && taskScore > 0f;
                var selectionWeight = eligibleForPrimary
                    ? Math.Max(
                        1,
                        (int)Math.Round(total * 100f, MidpointRounding.AwayFromZero))
                    : 0;

                candidates.Add(new ActionCandidateScore
                {
                    Action = action,
                    BaseScore = baseScore,
                    TaskScore = taskScore,
                    TemperamentScore = temperamentScore,
                    FavoriteActionScore = favoriteScore,
                    RepetitionPenalty = repetitionPenalty,
                    DeterministicJitter = jitter,
                    TotalScore = total,
                    EligibleForPrimary = eligibleForPrimary,
                    SelectionWeight = selectionWeight,
                    Reason = BuildCandidateReason(
                        action,
                        baseScore,
                        taskScore,
                        temperamentScore,
                        favoriteScore,
                        repetitionPenalty,
                        jitter,
                        total,
                        eligibleForPrimary,
                        selectionWeight)
                });
            }

            return candidates;
        }

        private static SpiritActionId SelectPrimaryAction(
            int seed,
            int completedTaskCount,
            List<ActionCandidateScore> candidates,
            out int selectionRoll,
            out int totalSelectionWeight)
        {
            selectionRoll = 0;
            totalSelectionWeight = 0;

            if (completedTaskCount == 0)
            {
                return SpiritActionId.Rest;
            }

            for (var i = 0; i < candidates.Count; i++)
            {
                totalSelectionWeight += candidates[i].SelectionWeight;
            }

            if (totalSelectionWeight <= 0)
            {
                return SpiritActionId.Rest;
            }

            var rollHash = StableHash.Fnv1A(
                "primary|" + seed.ToString(CultureInfo.InvariantCulture));
            selectionRoll = (int)(rollHash % (uint)totalSelectionWeight);
            var cumulativeWeight = 0;

            for (var i = 0; i < candidates.Count; i++)
            {
                cumulativeWeight += candidates[i].SelectionWeight;
                if (selectionRoll < cumulativeWeight)
                {
                    return candidates[i].Action;
                }
            }

            return SpiritActionId.Rest;
        }

        private static List<SpiritActionId> SelectSecondaryActions(
            SpiritActionId primaryAction,
            int completedTaskCount,
            List<ActionCandidateScore> rankedCandidates)
        {
            var results = new List<SpiritActionId>();
            var targetCount = Math.Min(2, Math.Max(0, completedTaskCount - 1));

            for (var i = 0; i < rankedCandidates.Count && results.Count < targetCount; i++)
            {
                var candidate = rankedCandidates[i];
                if (candidate.Action == primaryAction || candidate.TaskScore <= 0f)
                {
                    continue;
                }

                results.Add(candidate.Action);
            }

            return results;
        }

        private List<TaskActionLink> BuildTaskActionLinks(
            List<CompletedTask> tasks,
            List<TaskClassification> classifications)
        {
            var links = new List<TaskActionLink>();

            for (var i = 0; i < tasks.Count; i++)
            {
                var task = tasks[i];
                var classification = FindClassification(task, classifications);
                if (classification == null)
                {
                    classification = _taskClassifier.Classify(task);
                }

                var action = classification.ActionTags != null && classification.ActionTags.Count > 0
                    ? classification.ActionTags[0]
                    : SpiritActionId.Rest;
                var definition = SpiritActionCatalog.Get(action);
                links.Add(new TaskActionLink(
                    task.TaskId,
                    task.Title,
                    action,
                    definition.RecordText,
                    classification.Reason));
            }

            return links;
        }

        private static TaskClassification FindClassification(
            CompletedTask task,
            List<TaskClassification> classifications)
        {
            for (var i = 0; i < classifications.Count; i++)
            {
                var classification = classifications[i];
                if (string.Equals(
                        classification.TaskId,
                        task.TaskId,
                        StringComparison.Ordinal))
                {
                    return classification;
                }
            }

            return null;
        }

        private static int CompareCandidateScores(
            ActionCandidateScore left,
            ActionCandidateScore right)
        {
            var scoreComparison = right.TotalScore.CompareTo(left.TotalScore);
            return scoreComparison != 0
                ? scoreComparison
                : left.Action.CompareTo(right.Action);
        }

        private static int CountActionTags(
            List<TaskClassification> classifications,
            SpiritActionId action)
        {
            var count = 0;
            for (var i = 0; i < classifications.Count; i++)
            {
                var tags = classifications[i].ActionTags;
                if (tags == null)
                {
                    continue;
                }

                for (var j = 0; j < tags.Count; j++)
                {
                    if (tags[j] == action)
                    {
                        count++;
                        break;
                    }
                }
            }

            return count;
        }

        private static float GetTemperamentScore(
            List<SpiritTemperament> temperaments,
            SpiritActionId action)
        {
            if (temperaments == null || temperaments.Count == 0)
            {
                return 0f;
            }

            var score = 0f;
            var usedTemperaments = new HashSet<SpiritTemperament>();
            for (var i = 0; i < temperaments.Count && usedTemperaments.Count < 2; i++)
            {
                var temperament = temperaments[i];
                if (temperament == SpiritTemperament.Unspecified ||
                    !usedTemperaments.Add(temperament))
                {
                    continue;
                }

                score += GetTemperamentActionWeight(temperament, action);
            }

            return score;
        }

        private static float GetTemperamentActionWeight(
            SpiritTemperament temperament,
            SpiritActionId action)
        {
            switch (temperament)
            {
                case SpiritTemperament.Curious:
                    return action == SpiritActionId.ReadRecords ? 8f :
                        action == SpiritActionId.WalkForest ? 3f : 0f;
                case SpiritTemperament.Meticulous:
                    return action == SpiritActionId.CraftRepair ? 8f :
                        action == SpiritActionId.ReadRecords ? 5f : 0f;
                case SpiritTemperament.Active:
                    return action == SpiritActionId.WalkForest ? 8f : 0f;
                case SpiritTemperament.Sociable:
                    return action == SpiritActionId.SocialTea ? 8f : 0f;
                default:
                    return 0f;
            }
        }

        private static float GetRepetitionPenalty(
            List<SpiritActionId> recentPrimaryActions,
            SpiritActionId action)
        {
            if (recentPrimaryActions == null || recentPrimaryActions.Count == 0)
            {
                return 0f;
            }

            var occurrences = 0;
            for (var i = 0; i < recentPrimaryActions.Count; i++)
            {
                if (recentPrimaryActions[i] == action)
                {
                    occurrences++;
                }
            }

            var penalty = occurrences * -6f;
            if (recentPrimaryActions[recentPrimaryActions.Count - 1] == action)
            {
                penalty -= 4f;
            }

            return penalty;
        }

        private static float GetDeterministicJitter(int seed, SpiritActionId action)
        {
            var key = seed.ToString(CultureInfo.InvariantCulture) + "|" +
                      ((int)action).ToString(CultureInfo.InvariantCulture);
            return StableHash.Fnv1A(key) % 500u / 100f;
        }

        private static string BuildCanonicalSeedInput(
            string date,
            List<CompletedTask> tasks,
            List<TaskClassification> classifications,
            SpiritState spiritState,
            string simulationVersion)
        {
            var builder = new StringBuilder();
            AppendToken(builder, date);
            AppendToken(builder, simulationVersion);
            AppendToken(builder, spiritState.SpiritId);

            var sortedTasks = new List<CompletedTask>(tasks);
            sortedTasks.Sort(CompareTasks);
            for (var i = 0; i < sortedTasks.Count; i++)
            {
                var task = sortedTasks[i];
                AppendToken(builder, task.TaskId);
                AppendToken(builder, task.Title);
                AppendToken(builder, ((int)task.UserCategory).ToString(CultureInfo.InvariantCulture));
                AppendToken(builder, task.CompletedAt);
            }

            var sortedClassifications = new List<TaskClassification>(classifications);
            sortedClassifications.Sort(CompareClassifications);
            for (var i = 0; i < sortedClassifications.Count; i++)
            {
                var classification = sortedClassifications[i];
                AppendToken(builder, classification.TaskId);
                AppendToken(builder, ((int)classification.UserCategory).ToString(CultureInfo.InvariantCulture));
                AppendToken(builder, classification.InferredSubcategory);

                var tags = classification.ActionTags == null
                    ? new List<SpiritActionId>()
                    : new List<SpiritActionId>(classification.ActionTags);
                tags.Sort();
                for (var j = 0; j < tags.Count; j++)
                {
                    AppendToken(builder, ((int)tags[j]).ToString(CultureInfo.InvariantCulture));
                }
            }

            var temperaments = GetPrototypeTemperaments(spiritState.Temperaments);
            temperaments.Sort();
            for (var i = 0; i < temperaments.Count; i++)
            {
                AppendToken(builder, ((int)temperaments[i]).ToString(CultureInfo.InvariantCulture));
            }

            if (spiritState.RecentPrimaryActions != null)
            {
                for (var i = 0; i < spiritState.RecentPrimaryActions.Count; i++)
                {
                    AppendToken(
                        builder,
                        ((int)spiritState.RecentPrimaryActions[i]).ToString(CultureInfo.InvariantCulture));
                }
            }

            AppendToken(
                builder,
                ((int)spiritState.FavoriteAction).ToString(CultureInfo.InvariantCulture));
            return builder.ToString();
        }

        private static List<SpiritTemperament> GetPrototypeTemperaments(
            List<SpiritTemperament> temperaments)
        {
            var results = new List<SpiritTemperament>();
            if (temperaments == null)
            {
                return results;
            }

            for (var i = 0; i < temperaments.Count && results.Count < 2; i++)
            {
                var temperament = temperaments[i];
                if (temperament != SpiritTemperament.Unspecified && !results.Contains(temperament))
                {
                    results.Add(temperament);
                }
            }

            return results;
        }

        private static int CompareTasks(CompletedTask left, CompletedTask right)
        {
            var idComparison = string.Compare(left.TaskId, right.TaskId, StringComparison.Ordinal);
            if (idComparison != 0)
            {
                return idComparison;
            }

            var titleComparison = string.Compare(left.Title, right.Title, StringComparison.Ordinal);
            if (titleComparison != 0)
            {
                return titleComparison;
            }

            return string.Compare(left.CompletedAt, right.CompletedAt, StringComparison.Ordinal);
        }

        private static int CompareClassifications(
            TaskClassification left,
            TaskClassification right)
        {
            var idComparison = string.Compare(left.TaskId, right.TaskId, StringComparison.Ordinal);
            if (idComparison != 0)
            {
                return idComparison;
            }

            return string.Compare(
                left.InferredSubcategory,
                right.InferredSubcategory,
                StringComparison.Ordinal);
        }

        private static void AppendToken(StringBuilder builder, string value)
        {
            var safeValue = value ?? string.Empty;
            builder.Append(safeValue.Length.ToString(CultureInfo.InvariantCulture));
            builder.Append(':');
            builder.Append(safeValue);
            builder.Append('|');
        }

        private static string BuildCandidateReason(
            SpiritActionId action,
            float baseScore,
            float taskScore,
            float temperamentScore,
            float favoriteScore,
            float repetitionPenalty,
            float jitter,
            float total,
            bool eligibleForPrimary,
            int selectionWeight)
        {
            var reason = SpiritActionCatalog.Get(action).DisplayName + ": 기본 " + FormatScore(baseScore) +
                   " + TODO " + FormatScore(taskScore) +
                   " + 기질 " + FormatScore(temperamentScore) +
                   " + 선호 " + FormatScore(favoriteScore) +
                   " + 최근 반복 " + FormatScore(repetitionPenalty) +
                   " + 고정 시드 변주 " + FormatScore(jitter) +
                   " = " + FormatScore(total);

            return eligibleForPrimary
                ? reason + ", 대표 행동 추첨 가중치 " + selectionWeight
                : reason + ", TODO 연결이 없어 대표 행동 추첨 대상 제외";
        }

        private static string BuildSelectionReason(
            int completedTaskCount,
            SpiritActionId primaryAction,
            List<SpiritActionId> secondaryActions,
            List<ActionCandidateScore> rankedCandidates,
            int selectionRoll,
            int totalSelectionWeight)
        {
            if (completedTaskCount == 0)
            {
                return "완료한 TODO가 없어 정령이 이끼 침상에서 쉬는 기본 하루를 선택했습니다.";
            }

            ActionCandidateScore primaryScore = null;
            for (var i = 0; i < rankedCandidates.Count; i++)
            {
                if (rankedCandidates[i].Action == primaryAction)
                {
                    primaryScore = rankedCandidates[i];
                    break;
                }
            }

            var reason = "TODO 연결 후보의 점수를 양의 가중치로 바꾼 뒤, 임시 기질 최대 2개와 " +
                         "최근 반복 감점 및 고정 시드를 반영한 추첨으로 '" +
                         SpiritActionCatalog.Get(primaryAction).DisplayName + "'를 대표 행동으로 선택했습니다." +
                         " 고정 추첨 위치 " + (selectionRoll + 1) + "/" + totalSelectionWeight + ".";
            if (primaryScore != null)
            {
                reason += " 대표 점수 " + FormatScore(primaryScore.TotalScore) + ".";
            }

            if (secondaryActions.Count > 0)
            {
                reason += " 서로 다른 TODO 행동 태그 중 다음 순위 " +
                          JoinActionNames(secondaryActions) + "를 보조 행동으로 남겼습니다.";
            }

            return reason;
        }

        private static string JoinActionNames(List<SpiritActionId> actions)
        {
            var builder = new StringBuilder();
            for (var i = 0; i < actions.Count; i++)
            {
                if (i > 0)
                {
                    builder.Append(", ");
                }

                builder.Append(SpiritActionCatalog.Get(actions[i]).DisplayName);
            }

            return builder.ToString();
        }

        private static string FormatScore(float score)
        {
            return score.ToString("0.##", CultureInfo.InvariantCulture);
        }

        private static List<CompletedTask> CopyTasks(IEnumerable<CompletedTask> tasks)
        {
            var results = new List<CompletedTask>();
            if (tasks == null)
            {
                return results;
            }

            foreach (var task in tasks)
            {
                if (task != null)
                {
                    results.Add(task);
                }
            }

            return results;
        }

        private static List<TaskClassification> CopyClassifications(
            IEnumerable<TaskClassification> classifications)
        {
            var results = new List<TaskClassification>();
            if (classifications == null)
            {
                return results;
            }

            foreach (var classification in classifications)
            {
                if (classification != null)
                {
                    results.Add(classification);
                }
            }

            return results;
        }
    }
}
