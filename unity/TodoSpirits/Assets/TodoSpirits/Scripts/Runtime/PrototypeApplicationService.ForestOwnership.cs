using System;
using System.Collections.Generic;
using System.Linq;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    public sealed partial class PrototypeApplicationService
    {
        private bool HasActiveForestCompanion => CurrentLife != null && CurrentLife.Farewell != FarewellStep.Independent;
        private string ForestMemorySpiritId => HasActiveForestCompanion ? CurrentLife.SpiritId : "";

        private ForestCompletionOwnership ForestOwner(string date, string todoId) =>
            _saveData.Forest.CompletionOwners.Find(o => o != null && o.Date == date && o.TodoId == todoId);

        // Called only when loading an old save, not during rollback normalization. Existing ownership
        // always wins, including an occurrence that was later cancelled in the current factual record.
        private void MigrateForestCompletionOwnership()
        {
            if (!_saveData.Forest.Enabled) return;
            foreach (var record in _saveData.Records)
                foreach (var task in record.CompletedTasks)
                    if (task != null && ForestOwner(record.Date, task.TaskId) == null)
                        _saveData.Forest.CompletionOwners.Add(new ForestCompletionOwnership {
                            Date = record.Date, TodoId = task.TaskId, SpiritId = record.SpiritId
                        });
        }

        private IReadOnlyList<CompletedTask> AllForestCompletions(string date) =>
            _saveData.Forest.Todos.Where(t => !t.Archived && t.CompletedDates.Contains(date))
                .Select(t => new CompletedTask(t.Id, t.Title, t.Category, date) { RoutineId = t.Habit ? t.Id : "" }).ToList();

        private void AssignForestCompletionOwnership(string date)
        {
            foreach (var task in AllForestCompletions(date)) {
                var owner = ForestOwner(date, task.TaskId);
                if (owner == null) {
                    owner = new ForestCompletionOwnership { Date = date, TodoId = task.TaskId, SpiritId = "" };
                    _saveData.Forest.CompletionOwners.Add(owner);
                }
                if (string.IsNullOrEmpty(owner.SpiritId) && HasActiveForestCompanion)
                    owner.SpiritId = CurrentLife.SpiritId;
            }
        }

        private void UpdateForestRecordFacts(string date, IReadOnlyList<CompletedTask> completed)
        {
            foreach (var record in _saveData.Records.Where(r => r.Date == date)) {
                // The record's already-observed animation/story/reward remains unchanged.
                record.CompletedTasks = completed.Where(t => ForestOwner(date, t.TaskId)?.SpiritId == record.SpiritId).ToList();
            }
        }

        private void DecorateForestTodoOwnership(ForestDailyTodo todo, string date, string spiritId)
        {
            var owner = ForestOwner(date, todo.Id);
            todo.CompletedWithSpiritId = owner?.SpiritId ?? "";
            todo.CompletedWithPreviousSpirit = todo.IsCompleted && !string.IsNullOrEmpty(owner?.SpiritId) && owner.SpiritId != spiritId;
            todo.AwaitingCompanion = todo.IsCompleted && string.IsNullOrEmpty(owner?.SpiritId);
            if (todo.CompletedWithPreviousSpirit) {
                todo.Activity = "이전 정령과 함께한 기록으로 남아 있어요";
                todo.Reason = "이 완료의 성장과 이야기는 당시 함께했던 정령에게 남아요.";
            } else if (todo.AwaitingCompanion) {
                todo.Activity = "완료한 기록을 보관하고 새 동행을 기다려요";
                todo.Reason = "실제 정수는 저장했어요. 떠난 정령의 활동으로 추가하지 않아요.";
            }
        }

        private void RestoreForestCurrentRecord()
        {
            _currentRecord = FindRecord(CurrentDateKey, CurrentSpiritState.SpiritId);
            if (_currentRecord == null && HasActiveForestCompanion) {
                AssignForestCompletionOwnership(CurrentDateKey);
                _currentRecord = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey, CompletionInput(CurrentDateKey));
                _saveData.Records.Add(_currentRecord);
            }
        }
    }
}
