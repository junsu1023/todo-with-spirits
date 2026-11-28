using System;
using System.Collections.Generic;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    [Serializable]
    public sealed class ForestJournal
    {
        public bool Enabled;
        public bool ReduceMotion;
        public string HostAccountId;
        public long HostRevision = -1;
        public string HostDate;
        public string LastConfirmedAtUtc;
        public List<ForestTodo> Todos = new List<ForestTodo>();
        public List<ForestDailyMemory> Days = new List<ForestDailyMemory>();
        public List<ForestCompletionOwnership> CompletionOwners = new List<ForestCompletionOwnership>();
        public void EnsureCollections()
        {
            if (Todos == null) Todos = new List<ForestTodo>();
            if (Days == null) Days = new List<ForestDailyMemory>();
            if (CompletionOwners == null) CompletionOwners = new List<ForestCompletionOwnership>();
            foreach (var day in Days) day?.EnsureCollections();
            foreach (var todo in Todos)
                if (todo != null && todo.CompletedDates == null) todo.CompletedDates = new List<string>();
        }
    }
    [Serializable]
    public sealed class ForestCompletionOwnership
    {
        public string Date;
        public string TodoId;
        // Empty while no companion is present. Once assigned, cancellation never changes this owner.
        public string SpiritId;
    }
    [Serializable]
    public sealed class ForestTodo
    {
        public string Id;
        public string Title;
        public UserTaskCategory Category;
        public string CreatedOn;
        public bool Habit;
        public bool Archived;
        public List<string> CompletedDates = new List<string>();
    }
    /// <summary>No demonstration completions are injected into the real local journal.</summary>
    public sealed class EmptyTodoCompletionSource : ITodoCompletionSource
    {
        public IReadOnlyList<CompletedTask> GetCompletedTasks(string date) => Array.Empty<CompletedTask>();
    }
}
