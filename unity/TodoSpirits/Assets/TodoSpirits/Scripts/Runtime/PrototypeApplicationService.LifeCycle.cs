using System;
using System.Globalization;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime
{
    public sealed partial class PrototypeApplicationService
    {
        private void InitializeLifeCycle()
        {
            CurrentDate = DateTime.TryParseExact(_saveData.SessionDate, "yyyy-MM-dd", CultureInfo.InvariantCulture,
                DateTimeStyles.None, out var date) ? date : _initialDate;
            if (CurrentLife == null)
            {
                var first = CompanionLifeRules.CreateFirst("companion-001", CurrentDateKey);
                _saveData.Companions.Add(first);
                _saveData.ActiveCompanionId = first.SpiritId;
            }
            _saveData.SessionDate = CurrentDateKey;
            BindLifeSpirit();
            SetLifeDayPreset();
        }

        private void BindLifeSpirit()
        {
            var life = CurrentLife;
            var favorite = SpiritActionId.Rest;
            int count = 0;
            foreach (var action in life.Actions)
                if (action.Count > count) { favorite = action.Action; count = action.Count; }
            _saveData.SpiritState = new SpiritState(life.SpiritId, life.Temperaments,
                Array.Empty<SpiritActionId>(), favorite);
        }

        private void SetLifeDayPreset()
        {
            if (_mockTodoCompletionSource == null) return;
            // Local prototype fixture. No fake prior records are generated to fill missed days.
            int offset = (CurrentDate - DateTime.ParseExact(CurrentLife.StartedOn, "yyyy-MM-dd", CultureInfo.InvariantCulture)).Days;
            var presets = new[] { MockTodoPreset.DemoDay1WorkFocus, MockTodoPreset.DemoDay2HealthRelationship, MockTodoPreset.StandardThree };
            _mockTodoCompletionSource.SetPreset(presets[Math.Abs(offset % presets.Length)]);
        }

        /// <summary>Developer day acceleration. Production clock rollover will call the same finalization path.</summary>
        public bool SynchronizeLifeDate(DateTime localToday)
        {
            EnsureInitialized();
            if (!_lifeCycleEnabled || localToday.Date <= CurrentDate ||
                (!_saveData.Forest.Enabled && CurrentLife.Farewell == FarewellStep.Independent)) return false;
            MutateLife(() => {
                // Finalize only the day actually loaded. Absent days never receive invented TODOs or rewards.
                if (_currentRecord != null && CurrentLife.Farewell != FarewellStep.Independent)
                    CompanionLifeRules.FinalizeDay(CurrentLife, _currentRecord);
                CurrentDate = localToday.Date;
                _saveData.SessionDate = CurrentDateKey;
                BindLifeSpirit();
                SetLifeDayPreset();
                if (_saveData.Forest.Enabled) RestoreForestCurrentRecord();
                else {
                    _currentRecord = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey, CompletionInput(CurrentDateKey));
                    _saveData.Records.Add(_currentRecord);
                }
            });
            return true;
        }

        /// <summary>Developer day acceleration, intentionally separate from real clock synchronization.</summary>
        public DailyCompanionRecord AdvanceLifeDay()
        {
            MutateLife(() => {
                if (CurrentLife.Farewell == FarewellStep.Independent)
                    throw new InvalidOperationException("알을 선택해 다음 정령을 만나 주세요.");
                CompanionLifeRules.FinalizeDay(CurrentLife, _currentRecord);
                CurrentDate = CurrentDate.AddDays(1);
                _saveData.SessionDate = CurrentDateKey;
                BindLifeSpirit();
                SetLifeDayPreset();
                var tasks = CompletionInput(CurrentDateKey);
                _currentRecord = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey, tasks);
                _saveData.Records.Add(_currentRecord);
            });
            return _currentRecord;
        }

        public void RememberTaskClassification(string taskId, SpiritActionId action) => MutateLife(() => {
            var task = _currentRecord.CompletedTasks.Find(x => x != null && x.TaskId == taskId);
            if (task == null) throw new ArgumentException("현재 기록에서 할 일을 찾을 수 없습니다.");
            // Applies to future generation. Today's saved action, classification and reward remain immutable.
            _saveData.ClassificationMemory.Correct(task, action);
        });

        public void ObserveLifeStage() => MutateLife(() => CompanionLifeRules.ObserveStageEvent(CurrentLife));
        public void AdvanceLifeFarewell() => MutateLife(() => CompanionLifeRules.AdvanceFarewell(CurrentLife));
        public void NameCurrentCompanion(string name) => MutateLife(() => CompanionLifeRules.Name(CurrentLife, name));
        public void ObserveNamedCompanion() => MutateLife(() => CompanionLifeRules.ObserveNamedCompanion(CurrentLife));
        public void LetCompanionLeave() => MutateLife(() => {
            if (PendingTrip != null) throw new InvalidOperationException("여행에서 돌아온 뒤 독립을 배웅해 주세요.");
            if (_saveData.Forest.Enabled && _currentRecord != null)
                CompanionLifeRules.FinalizeDay(CurrentLife, _currentRecord);
            CompanionLifeRules.Leave(CurrentLife, CurrentDateKey);
        });

        public DailyCompanionRecord SelectNextEgg(int egg, DateTime? localToday = null)
        {
            MutateLife(() => {
                if (CurrentLife.Farewell != FarewellStep.Independent)
                    throw new InvalidOperationException("현재 정령의 독립 후 알을 선택할 수 있습니다.");
                // Forest occurrence ownership permits a real same-day handoff without replaying growth.
                var nextDate = _saveData.Forest.Enabled ? CurrentDate : CurrentDate.AddDays(1);
                if (localToday.HasValue && nextDate < localToday.Value.Date) nextDate = localToday.Value.Date;
                var next = CompanionLifeRules.CreateFromEgg("companion-" + Guid.NewGuid().ToString("N"), ToDateKey(nextDate), egg);
                next.Story.Enabled = _saveData.Forest.Enabled;
                if (_saveData.Forest.Enabled && !IsHostConnected && nextDate > CurrentDate)
                {
                    // One-day tasks still awaiting completion must stay reachable after the handoff.
                    // CompletedDates and archived daily snapshots keep their original ownership;
                    // connected TODO apps remain authoritative for their own task dates.
                    string departingDate = CurrentDateKey;
                    string arrivingDate = ToDateKey(nextDate);
                    foreach (var todo in _saveData.Forest.Todos)
                        if (!todo.Archived && !todo.Habit && todo.CreatedOn == departingDate && !todo.CompletedDates.Contains(departingDate))
                            todo.CreatedOn = arrivingDate;
                }
                CurrentDate = nextDate;
                _saveData.Companions.Add(next);
                _saveData.ActiveCompanionId = next.SpiritId;
                _saveData.SessionDate = CurrentDateKey;
                BindLifeSpirit();
                SetLifeDayPreset();
                if (_saveData.Forest.Enabled) AssignForestCompletionOwnership(CurrentDateKey);
                _currentRecord = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey,
                    CompletionInput(CurrentDateKey));
                _saveData.Records.Add(_currentRecord);
                if (_saveData.Forest.Enabled && (!IsHostConnected || _saveData.Forest.HostDate == CurrentDateKey)) CaptureForestDay();
            });
            return _currentRecord;
        }

        private void MutateLife(Action action)
        {
            EnsureInitialized();
            if (!_lifeCycleEnabled) throw new InvalidOperationException("동행 모드를 먼저 활성화해 주세요.");
            string previous = JsonUtility.ToJson(_saveData);
            DateTime previousDate = CurrentDate;
            try
            {
                action();
                // Failure must not leave purchases, lifecycle transitions, or generated records committed in memory.
                _saveRepository.Save(_saveData);
            }
            catch
            {
                _saveData = JsonUtility.FromJson<PrototypeSaveData>(previous);
                _saveData.EnsureCollections();
                CurrentDate = previousDate;
                _currentRecord = FindRecord(CurrentDateKey, CurrentSpiritState.SpiritId);
                SetLifeDayPreset();
                throw;
            }
        }
    }
}
