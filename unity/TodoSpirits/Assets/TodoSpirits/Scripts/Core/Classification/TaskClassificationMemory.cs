using System;
using System.Collections.Generic;
using System.Text;

namespace TodoSpirits.Core
{
    [Serializable]
    public sealed class RememberedTaskClassification
    {
        public string TaskId;
        public string TitleKey;
        public UserTaskCategory Category;
        public string RoutineId;
        public SpiritActionId Action;
        public string Subcategory;
    }

    [Serializable]
    public sealed class TaskClassificationMemory
    {
        public List<RememberedTaskClassification> Corrections = new List<RememberedTaskClassification>();
        public List<RememberedTaskClassification> Routines = new List<RememberedTaskClassification>();

        public void EnsureCollections()
        {
            if (Corrections == null) Corrections = new List<RememberedTaskClassification>();
            if (Routines == null) Routines = new List<RememberedTaskClassification>();
        }

        public void Correct(CompletedTask task, SpiritActionId action)
        {
            if (task == null) throw new ArgumentNullException(nameof(task));
            if (!Enum.IsDefined(typeof(SpiritActionId), action)) throw new ArgumentOutOfRangeException(nameof(action));
            EnsureCollections();
            string key = TitleKey(task.Title);
            if (string.IsNullOrWhiteSpace(task.TaskId) && key.Length == 0)
                throw new ArgumentException("분류를 기억할 할 일 식별자나 제목이 필요합니다.");
            Corrections.RemoveAll(x => x != null &&
                ((!string.IsNullOrWhiteSpace(task.TaskId) && x.TaskId == task.TaskId) ||
                 (!string.IsNullOrWhiteSpace(task.RoutineId) && x.RoutineId == task.RoutineId) ||
                 (key.Length > 0 && x.TitleKey == key && x.Category == task.UserCategory)));
            Corrections.Add(new RememberedTaskClassification {
                TaskId = task.TaskId, TitleKey = key, Category = task.UserCategory, RoutineId = task.RoutineId,
                Action = action, Subcategory = SpiritActionCatalog.Get(action).DisplayName
            });
        }

        public TaskClassification Resolve(CompletedTask task)
        {
            EnsureCollections();
            // Exact identity wins over normalized titles, even if a title match was edited later.
            for (int pass = 0; pass < 3; pass++)
                for (int i = Corrections.Count - 1; i >= 0; i--)
                {
                    var entry = Corrections[i];
                    if (!Valid(entry)) continue;
                    bool match = pass == 0
                        ? !string.IsNullOrWhiteSpace(task.TaskId) && entry.TaskId == task.TaskId
                        : pass == 1 ? !string.IsNullOrWhiteSpace(task.RoutineId) && entry.RoutineId == task.RoutineId
                        : entry.Category == task.UserCategory && !string.IsNullOrEmpty(entry.TitleKey) && entry.TitleKey == TitleKey(task.Title);
                    if (match) return Result(task, entry, "과거 사용자 분류 수정 기록을 적용했습니다.");
                }
            if (!string.IsNullOrWhiteSpace(task.RoutineId))
                for (int i = Routines.Count - 1; i >= 0; i--)
                    if (Valid(Routines[i]) && Routines[i].RoutineId == task.RoutineId)
                        return Result(task, Routines[i], "반복 루틴에 저장된 분류를 적용했습니다.");
            return null;
        }

        public void RememberRoutine(CompletedTask task, TaskClassification classification)
        {
            EnsureCollections();
            if (string.IsNullOrWhiteSpace(task.RoutineId) || classification?.ActionTags == null || classification.ActionTags.Count == 0) return;
            if (Routines.Exists(x => Valid(x) && x.RoutineId == task.RoutineId)) return;
            Routines.Add(new RememberedTaskClassification { RoutineId = task.RoutineId,
                Action = classification.ActionTags[0], Subcategory = classification.InferredSubcategory });
        }

        private static bool Valid(RememberedTaskClassification entry) =>
            entry != null && Enum.IsDefined(typeof(SpiritActionId), entry.Action);

        private static TaskClassification Result(CompletedTask task, RememberedTaskClassification entry, string reason) =>
            new TaskClassification(task.TaskId, task.UserCategory, entry.Subcategory, 1f, new[] { entry.Action }, reason);

        private static string TitleKey(string title)
        {
            // Conservative similarity: whitespace and casing only; unrelated titles must not inherit edits.
            var key = new StringBuilder();
            foreach (char c in title ?? string.Empty)
                if (!char.IsWhiteSpace(c)) key.Append(char.ToUpperInvariant(c));
            return key.ToString();
        }
    }
}
