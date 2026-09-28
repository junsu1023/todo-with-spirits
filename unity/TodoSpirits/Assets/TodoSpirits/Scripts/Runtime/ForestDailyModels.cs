using System;
using System.Collections.Generic;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    // A cached snapshot is not evidence that this session's network request succeeded.
    public enum ForestSyncState { Awaiting, Confirmed, Local, Offline, Error }

    [Serializable]
    public sealed class ForestDailyTodo
    {
        public string Id;
        public string Title;
        public UserTaskCategory Category;
        public bool IsHabit;
        public bool IsCompleted;
        public string CompletedWithSpiritId;
        public bool CompletedWithPreviousSpirit;
        public bool AwaitingCompanion;
        public string ActivityKey;
        public string Location;
        public string Activity;
        public string Reason;
    }

    [Serializable]
    public sealed class ForestDailyActivity
    {
        public string ActivityKey;
        public string Location;
        public string Activity;
        public string Reason;
        public bool IsCompleted;
        public bool IsAutonomous;
        public string GiftSource;
        public string GiftGivenOn;
        public List<string> SourceTodoIds = new List<string>();
    }

    [Serializable]
    public sealed class ForestDailyMemory
    {
        public string AccountId;
        public string Date;
        public string SpiritId;
        public long Revision = -1;
        public string LastConfirmedAtUtc;
        public bool Displayed;
        public int AcknowledgedEssence;
        public int TotalEssence;
        public List<string> AcknowledgedAcquisitions = new List<string>();
        public List<ForestDailyTodo> Todos = new List<ForestDailyTodo>();
        public List<ForestDailyActivity> Activities = new List<ForestDailyActivity>();
        // Facts may be corrected. The scene actually seen remains a separate historical layer.
        public long ObservedRevision = -1;
        public string ObservedAtUtc;
        public CompanionStage AppearancePhase;
        public string PrimaryActivity;
        public int ChoiceCount;
        public int LastChoiceOption = -1;
        public ForestStoryFamily StoryFamily;
        public string VisiblePropName;
        public List<ForestDailyActivity> ObservedActivities = new List<ForestDailyActivity>();
        public List<string> Acquisitions = new List<string>();
        public void EnsureCollections()
        {
            if (Todos == null) Todos = new List<ForestDailyTodo>();
            if (Activities == null) Activities = new List<ForestDailyActivity>();
            if (ObservedActivities == null) ObservedActivities = new List<ForestDailyActivity>();
            if (Acquisitions == null) Acquisitions = new List<string>();
            if (AcknowledgedAcquisitions == null) AcknowledgedAcquisitions = new List<string>();
        }
    }

    public sealed class ForestTodaySummary
    {
        public string AccountId;
        public string Date;
        public string SpiritId;
        public long Revision;
        public ForestSyncState SyncState;
        public string SyncMessage;
        public string LastConfirmedAtUtc;
        public bool HasConfirmedData;
        public bool IsFirstVisit;
        public int TotalEssence;
        public int NewEssence;
        public string Mood;
        public List<ForestDailyTodo> Todos = new List<ForestDailyTodo>();
        public List<ForestDailyActivity> Activities = new List<ForestDailyActivity>();
        public List<string> Acquisitions = new List<string>();
    }
}
