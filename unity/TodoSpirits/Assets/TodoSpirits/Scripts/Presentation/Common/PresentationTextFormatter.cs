using System.Collections.Generic;
using System.Globalization;
using System.Text;
using TodoSpirits.Core;

namespace TodoSpirits.Presentation.Common
{
    internal static class PresentationTextFormatter
    {
        public static string ActionDisplayName(SpiritActionId action)
        {
            return SpiritActionCatalog.Get(action).DisplayName;
        }

        public static List<string> CompletedTaskTitles(DailyCompanionRecord record)
        {
            var result = new List<string>();
            if (record?.CompletedTasks == null)
            {
                return result;
            }

            for (int i = 0; i < record.CompletedTasks.Count; i++)
            {
                CompletedTask task = record.CompletedTasks[i];
                if (task != null && !string.IsNullOrWhiteSpace(task.Title))
                {
                    result.Add(task.Title);
                }
            }

            return result;
        }

        public static List<string> TaskActionLinks(DailyCompanionRecord record)
        {
            var result = new List<string>();
            List<TaskActionLink> links = record?.SpiritDay?.TaskActionLinks;
            if (links != null)
            {
                for (int i = 0; i < links.Count; i++)
                {
                    TaskActionLink link = links[i];
                    if (link == null)
                    {
                        continue;
                    }

                    string taskTitle = string.IsNullOrWhiteSpace(link.TaskTitle) ? "오늘의 완료 기록" : link.TaskTitle;
                    string spiritRecord = string.IsNullOrWhiteSpace(link.SpiritRecordText)
                        ? SpiritActionCatalog.Get(link.Action).RecordText
                        : link.SpiritRecordText;
                    result.Add($"• {taskTitle}\n   ↳ {spiritRecord}");
                }
            }

            if (result.Count == 0 && record?.SpiritDay != null)
            {
                SpiritActionDefinition fallback = SpiritActionCatalog.Get(record.SpiritDay.PrimaryAction);
                result.Add($"• 완료 기록이 없는 날\n   ↳ {fallback.RecordText}");
            }

            return result;
        }

        public static string BuildDebugInfo(
            DailyCompanionRecord record,
            SpiritState spiritState,
            string runtimeDebugInfo)
        {
            var builder = new StringBuilder(2048);
            builder.AppendLine("[Current Date]");
            builder.AppendLine(record?.Date ?? "-");

            builder.AppendLine().AppendLine("[Mock Completed TODOs]");
            if (record?.CompletedTasks == null || record.CompletedTasks.Count == 0)
            {
                builder.AppendLine("(0 TODO)");
            }
            else
            {
                for (int i = 0; i < record.CompletedTasks.Count; i++)
                {
                    CompletedTask task = record.CompletedTasks[i];
                    if (task == null)
                    {
                        continue;
                    }

                    builder.Append("- ").Append(task.Title)
                        .Append(" / ").Append(task.UserCategory)
                        .Append(" / ").AppendLine(task.TaskId);
                }
            }

            builder.AppendLine().AppendLine("[Classification Result]");
            if (record?.Classifications == null || record.Classifications.Count == 0)
            {
                builder.AppendLine("(none)");
            }
            else
            {
                for (int i = 0; i < record.Classifications.Count; i++)
                {
                    TaskClassification classification = record.Classifications[i];
                    if (classification == null)
                    {
                        continue;
                    }

                    builder.Append("- ").Append(FindTaskTitle(record, classification.TaskId))
                        .Append(" => ").Append(classification.InferredSubcategory)
                        .Append(" / confidence ")
                        .Append(classification.ClassificationConfidence.ToString("0.00", CultureInfo.InvariantCulture))
                        .AppendLine();
                    builder.Append("  tags: ").AppendLine(JoinActions(classification.ActionTags));
                    builder.Append("  reason: ").AppendLine(classification.Reason);
                }
            }

            builder.AppendLine().AppendLine("[Spirit Temperaments]");
            builder.AppendLine(spiritState?.Temperaments == null || spiritState.Temperaments.Count == 0
                ? "(none)"
                : string.Join(", ", spiritState.Temperaments));

            builder.AppendLine().AppendLine("[Candidate Actions / Score / Reasons]");
            if (record?.CandidateActions == null || record.CandidateActions.Count == 0)
            {
                builder.AppendLine("(none)");
            }
            else
            {
                for (int i = 0; i < record.CandidateActions.Count; i++)
                {
                    ActionCandidateScore candidate = record.CandidateActions[i];
                    if (candidate == null)
                    {
                        continue;
                    }

                    builder.Append("- ").Append(candidate.Action)
                        .Append(" total=").Append(candidate.TotalScore.ToString("0.00", CultureInfo.InvariantCulture))
                        .Append(" [base ").Append(candidate.BaseScore.ToString("0.00", CultureInfo.InvariantCulture))
                        .Append(", task ").Append(candidate.TaskScore.ToString("0.00", CultureInfo.InvariantCulture))
                        .Append(", temperament ").Append(candidate.TemperamentScore.ToString("0.00", CultureInfo.InvariantCulture))
                        .Append(", favorite ").Append(candidate.FavoriteActionScore.ToString("0.00", CultureInfo.InvariantCulture))
                        .Append(", repeat ").Append(candidate.RepetitionPenalty.ToString("0.00", CultureInfo.InvariantCulture))
                        .Append(", jitter ").Append(candidate.DeterministicJitter.ToString("0.00", CultureInfo.InvariantCulture))
                        .AppendLine("]");
                    builder.Append("  reason: ").AppendLine(candidate.Reason);
                }
            }

            SpiritDayResult day = record?.SpiritDay;
            builder.AppendLine().AppendLine("[Selection]");
            builder.Append("Primary: ").AppendLine(day == null ? "-" : day.PrimaryAction.ToString());
            builder.Append("Secondaries: ").AppendLine(day == null ? "-" : JoinActions(day.SecondaryActions));
            builder.Append("Location: ").AppendLine(day?.Location ?? "-");
            builder.Append("Selection reason: ").AppendLine(record?.SelectionReason ?? "-");
            builder.Append("Seed: ").AppendLine(day == null ? "-" : day.Seed.ToString(CultureInfo.InvariantCulture));
            builder.Append("Essence reward: +").AppendLine((record?.EssenceReward ?? 0).ToString(CultureInfo.InvariantCulture));
            builder.Append("Granted delta: +").AppendLine((record?.GrantedEssenceDelta ?? 0).ToString(CultureInfo.InvariantCulture));
            builder.Append("Simulation version: ").AppendLine(record?.SimulationVersion ?? "-");

            if (!string.IsNullOrWhiteSpace(runtimeDebugInfo))
            {
                builder.AppendLine().AppendLine("[Runtime Snapshot]");
                builder.AppendLine(runtimeDebugInfo);
            }

            return builder.ToString();
        }

        private static string FindTaskTitle(DailyCompanionRecord record, string taskId)
        {
            if (record?.CompletedTasks != null)
            {
                for (int i = 0; i < record.CompletedTasks.Count; i++)
                {
                    CompletedTask task = record.CompletedTasks[i];
                    if (task != null && task.TaskId == taskId)
                    {
                        return task.Title;
                    }
                }
            }

            return taskId ?? "-";
        }

        private static string JoinActions(IReadOnlyList<SpiritActionId> actions)
        {
            if (actions == null || actions.Count == 0)
            {
                return "(none)";
            }

            var builder = new StringBuilder();
            for (int i = 0; i < actions.Count; i++)
            {
                if (i > 0)
                {
                    builder.Append(", ");
                }

                builder.Append(actions[i]);
            }

            return builder.ToString();
        }
    }
}
