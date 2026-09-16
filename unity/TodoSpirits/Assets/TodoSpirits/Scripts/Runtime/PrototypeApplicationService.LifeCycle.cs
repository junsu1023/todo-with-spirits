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
            if (!_lifeCycleEnabled || localToday.Date <= CurrentDate || CurrentLife.Farewell == FarewellStep.Independent) return false;
            MutateLife(() => {
                // Finalize only the day actually loaded. Absent days never receive invented TODOs or rewards.
                CompanionLifeRules.FinalizeDay(CurrentLife, _currentRecord);
                CurrentDate = localToday.Date;
                _saveData.SessionDate = CurrentDateKey;
                BindLifeSpirit();
                SetLifeDayPreset();
                _currentRecord = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey,
                    _todoCompletionSource.GetCompletedTasks(CurrentDateKey));
                _saveData.Records.Add(_currentRecord);
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
                var tasks = _todoCompletionSource.GetCompletedTasks(CurrentDateKey);
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
            CompanionLifeRules.Leave(CurrentLife, CurrentDateKey);
        });

        public DailyCompanionRecord SelectNextEgg(int egg)
        {
            MutateLife(() => {
                if (CurrentLife.Farewell != FarewellStep.Independent)
                    throw new InvalidOperationException("현재 정령의 독립 후 알을 선택할 수 있습니다.");
                // Next cycle starts next day so one account activity date cannot belong to two companions.
                var nextDate = CurrentDate.AddDays(1);
                var next = CompanionLifeRules.CreateFromEgg("companion-" + Guid.NewGuid().ToString("N"), ToDateKey(nextDate), egg);
                CurrentDate = nextDate;
                _saveData.Companions.Add(next);
                _saveData.ActiveCompanionId = next.SpiritId;
                _saveData.SessionDate = CurrentDateKey;
                BindLifeSpirit();
                SetLifeDayPreset();
                _currentRecord = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey,
                    _todoCompletionSource.GetCompletedTasks(CurrentDateKey));
                _saveData.Records.Add(_currentRecord);
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
