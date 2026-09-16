using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    public enum CompanionStage { Meeting, Adapting, Interests, OwnWay, Preparing, Adult }
    public enum FarewellStep { Growing, AdultReveal, Reflection, Naming, LastCompanionship, ReadyToLeave, Independent }
    public enum AdultRoute { Recorder, Explorer, Artisan }

    [Serializable]
    public sealed class LifeActionCount
    {
        public SpiritActionId Action;
        public int Count;
        public int LongestStreak;
    }

    [Serializable]
    public sealed class CompanionProject
    {
        public SpiritActionId Action;
        public string Title;
        public string StartedOn;
        public string CompletedOn;
        public int WorkDays;
        public bool IsComplete => !string.IsNullOrEmpty(CompletedOn);
    }

    /// <summary>Persistent life state. Dates are finalized activity dates, never calendar EXP.</summary>
    [Serializable]
    public sealed class CompanionLife
    {
        public string SpiritId = string.Empty;
        public string Name = string.Empty;
        public string StartedOn = string.Empty;
        public string IndependentOn = string.Empty;
        public CompanionStage Stage;
        public FarewellStep Farewell;
        public AdultRoute Route;
        public bool StageEventSeen;
        public int StageStartedAtActivityDay;
        public int ActivityDays;
        public int CurrentActionStreak;
        public SpiritActionId LastAction;
        public List<SpiritTemperament> Temperaments = new List<SpiritTemperament>();
        public List<string> FinalizedDates = new List<string>();
        public List<LifeActionCount> Actions = new List<LifeActionCount>();
        public List<string> RouteReasons = new List<string>();
        public List<string> Letters = new List<string>();
        public List<CompanionProject> Projects = new List<CompanionProject>();
        public CompanionProject ActiveProject => Projects?.Find(project => !project.IsComplete);

        public string DisplayName => string.IsNullOrEmpty(Name) ? "작은 정령" : Name;
        public int AppearancePhase => Stage < CompanionStage.Interests ? 0 : Stage < CompanionStage.Adult ? 1 : 2;

        public void EnsureCollections()
        {
            if (Temperaments == null) Temperaments = new List<SpiritTemperament>();
            if (FinalizedDates == null) FinalizedDates = new List<string>();
            if (Actions == null) Actions = new List<LifeActionCount>();
            if (RouteReasons == null) RouteReasons = new List<string>();
            if (Letters == null) Letters = new List<string>();
            if (Projects == null) Projects = new List<CompanionProject>();
        }
    }
}
