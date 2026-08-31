using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    // Values are append-only because Unity serializes enums as integers.
    public enum UserTaskCategory
    {
        Unspecified = 0,
        WorkStudy = 1,
        Health = 2,
        Living = 3,
        Relationships = 4,
        SelfDevelopment = 5,
        Hobby = 6,
        RestMind = 7,
        Assets = 8,
        Economy = 9
    }

    public enum SpiritTemperament
    {
        Unspecified = 0,
        Curious = 1,
        Relaxed = 2,
        Active = 3,
        Sociable = 4,
        Cautious = 5,
        Meticulous = 6,
        Playful = 7,
        Independent = 8
    }

    public enum SpiritActionId
    {
        ReadRecords = 0,
        CraftRepair = 1,
        WalkForest = 2,
        SocialTea = 3,
        Rest = 4
    }

    public enum EssenceRewardBand
    {
        None = 0,
        OneTask = 1,
        TwoToThreeTasks = 2,
        FourOrMoreTasks = 3
    }

    [Serializable]
    public sealed class CompletedTask
    {
        public string TaskId;
        public string Title;
        public UserTaskCategory UserCategory;
        public string CompletedAt;

        public CompletedTask()
        {
            TaskId = string.Empty;
            Title = string.Empty;
            CompletedAt = string.Empty;
        }

        public CompletedTask(
            string taskId,
            string title,
            UserTaskCategory userCategory,
            string completedAt)
        {
            TaskId = taskId ?? string.Empty;
            Title = title ?? string.Empty;
            UserCategory = userCategory;
            CompletedAt = completedAt ?? string.Empty;
        }
    }

    [Serializable]
    public sealed class TaskClassification
    {
        public string TaskId;
        public UserTaskCategory UserCategory;
        public string InferredSubcategory;
        public float ClassificationConfidence;
        public List<SpiritActionId> ActionTags;
        public string Reason;

        public TaskClassification()
        {
            TaskId = string.Empty;
            InferredSubcategory = string.Empty;
            ActionTags = new List<SpiritActionId>();
            Reason = string.Empty;
        }

        public TaskClassification(
            string taskId,
            UserTaskCategory userCategory,
            string inferredSubcategory,
            float classificationConfidence,
            IEnumerable<SpiritActionId> actionTags,
            string reason)
        {
            TaskId = taskId ?? string.Empty;
            UserCategory = userCategory;
            InferredSubcategory = inferredSubcategory ?? string.Empty;
            ClassificationConfidence = classificationConfidence;
            ActionTags = actionTags == null
                ? new List<SpiritActionId>()
                : new List<SpiritActionId>(actionTags);
            Reason = reason ?? string.Empty;
        }
    }

    [Serializable]
    public sealed class SpiritState
    {
        public string SpiritId;
        public List<SpiritTemperament> Temperaments;
        public List<SpiritActionId> RecentPrimaryActions;
        public SpiritActionId FavoriteAction;

        public SpiritState()
        {
            SpiritId = string.Empty;
            Temperaments = new List<SpiritTemperament>();
            RecentPrimaryActions = new List<SpiritActionId>();
            FavoriteAction = SpiritActionId.Rest;
        }

        public SpiritState(
            string spiritId,
            IEnumerable<SpiritTemperament> temperaments,
            IEnumerable<SpiritActionId> recentPrimaryActions,
            SpiritActionId favoriteAction)
        {
            SpiritId = spiritId ?? string.Empty;
            Temperaments = temperaments == null
                ? new List<SpiritTemperament>()
                : new List<SpiritTemperament>(temperaments);
            RecentPrimaryActions = recentPrimaryActions == null
                ? new List<SpiritActionId>()
                : new List<SpiritActionId>(recentPrimaryActions);
            FavoriteAction = favoriteAction;
        }
    }

    [Serializable]
    public sealed class TaskActionLink
    {
        public string TaskId;
        public string TaskTitle;
        public SpiritActionId Action;
        public string SpiritRecordText;
        public string Reason;

        public TaskActionLink()
        {
            TaskId = string.Empty;
            TaskTitle = string.Empty;
            SpiritRecordText = string.Empty;
            Reason = string.Empty;
        }

        public TaskActionLink(
            string taskId,
            string taskTitle,
            SpiritActionId action,
            string spiritRecordText,
            string reason)
        {
            TaskId = taskId ?? string.Empty;
            TaskTitle = taskTitle ?? string.Empty;
            Action = action;
            SpiritRecordText = spiritRecordText ?? string.Empty;
            Reason = reason ?? string.Empty;
        }
    }

    [Serializable]
    public sealed class ActionCandidateScore
    {
        public SpiritActionId Action;
        public float BaseScore;
        public float TaskScore;
        public float TemperamentScore;
        public float FavoriteActionScore;
        public float RepetitionPenalty;
        public float DeterministicJitter;
        public float TotalScore;
        public bool EligibleForPrimary;
        public int SelectionWeight;
        public string Reason;

        public ActionCandidateScore()
        {
            Reason = string.Empty;
        }
    }

    [Serializable]
    public sealed class SpiritDayResult
    {
        public string Date;
        public SpiritActionId PrimaryAction;
        public List<SpiritActionId> SecondaryActions;
        public string Location;
        public string Dialogue;
        public int Seed;
        public List<TaskActionLink> TaskActionLinks;

        public SpiritDayResult()
        {
            Date = string.Empty;
            SecondaryActions = new List<SpiritActionId>();
            Location = string.Empty;
            Dialogue = string.Empty;
            TaskActionLinks = new List<TaskActionLink>();
        }

        public SpiritDayResult(
            string date,
            SpiritActionId primaryAction,
            IEnumerable<SpiritActionId> secondaryActions,
            string location,
            string dialogue,
            int seed,
            IEnumerable<TaskActionLink> taskActionLinks)
        {
            Date = date ?? string.Empty;
            PrimaryAction = primaryAction;
            SecondaryActions = secondaryActions == null
                ? new List<SpiritActionId>()
                : new List<SpiritActionId>(secondaryActions);
            Location = location ?? string.Empty;
            Dialogue = dialogue ?? string.Empty;
            Seed = seed;
            TaskActionLinks = taskActionLinks == null
                ? new List<TaskActionLink>()
                : new List<TaskActionLink>(taskActionLinks);
        }
    }

    [Serializable]
    public sealed class DayGenerationReport
    {
        public string Date;
        public string SimulationVersion;
        public SpiritDayResult SpiritDay;
        public List<TaskClassification> Classifications;
        public List<ActionCandidateScore> CandidateActions;
        public string SelectionReason;

        public DayGenerationReport()
        {
            Date = string.Empty;
            SimulationVersion = string.Empty;
            SpiritDay = new SpiritDayResult();
            Classifications = new List<TaskClassification>();
            CandidateActions = new List<ActionCandidateScore>();
            SelectionReason = string.Empty;
        }
    }

    [Serializable]
    public sealed class RewardBandByDate
    {
        public string Date;
        public int HighestReward;
        public EssenceRewardBand HighestBand;

        public RewardBandByDate()
        {
            Date = string.Empty;
        }

        public RewardBandByDate(string date, int highestReward, EssenceRewardBand highestBand)
        {
            Date = date ?? string.Empty;
            HighestReward = highestReward;
            HighestBand = highestBand;
        }
    }

    [Serializable]
    public sealed class DailyCompanionRecord
    {
        public string Date;
        public string SpiritId;
        public List<CompletedTask> CompletedTasks;
        public int EssenceReward;
        public int GrantedEssenceDelta;
        public SpiritDayResult SpiritDay;
        public List<TaskClassification> Classifications;
        public List<ActionCandidateScore> CandidateActions;
        public string SelectionReason;
        public string SimulationVersion;
        public bool RewardAcknowledged;

        public DailyCompanionRecord()
        {
            Date = string.Empty;
            SpiritId = string.Empty;
            CompletedTasks = new List<CompletedTask>();
            SpiritDay = new SpiritDayResult();
            Classifications = new List<TaskClassification>();
            CandidateActions = new List<ActionCandidateScore>();
            SelectionReason = string.Empty;
            SimulationVersion = string.Empty;
        }

        public DailyCompanionRecord(
            string date,
            IEnumerable<CompletedTask> completedTasks,
            int essenceReward,
            SpiritDayResult spiritDay)
            : this()
        {
            Date = date ?? string.Empty;
            CompletedTasks = completedTasks == null
                ? new List<CompletedTask>()
                : new List<CompletedTask>(completedTasks);
            EssenceReward = essenceReward;
            SpiritDay = spiritDay ?? new SpiritDayResult();
        }

        public DailyCompanionRecord(
            string date,
            IEnumerable<CompletedTask> completedTasks,
            int essenceReward,
            int grantedEssenceDelta,
            DayGenerationReport report,
            bool rewardAcknowledged)
            : this(date, completedTasks, essenceReward, report == null ? null : report.SpiritDay)
        {
            GrantedEssenceDelta = grantedEssenceDelta;
            RewardAcknowledged = rewardAcknowledged;

            if (report == null)
            {
                return;
            }

            Classifications = report.Classifications == null
                ? new List<TaskClassification>()
                : new List<TaskClassification>(report.Classifications);
            CandidateActions = report.CandidateActions == null
                ? new List<ActionCandidateScore>()
                : new List<ActionCandidateScore>(report.CandidateActions);
            SelectionReason = report.SelectionReason ?? string.Empty;
            SimulationVersion = report.SimulationVersion ?? string.Empty;
        }
    }
}
