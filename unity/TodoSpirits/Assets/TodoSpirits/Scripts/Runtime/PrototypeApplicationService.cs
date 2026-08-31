using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime
{
    /// <summary>
    /// Coordinates the internal demo input, deterministic core simulation, reward wallet, and save data.
    /// </summary>
    public sealed class PrototypeApplicationService
    {
        private const string ProfileASpiritId = "prototype-spirit-001";
        private const string ProfileBSpiritId = "prototype-spirit-002";
        private const string SimulationVersion = SpiritDayGenerator.DefaultSimulationVersion;
        private const int RecentActionLimit = 3;

        private readonly ITodoCompletionSource _todoCompletionSource;
        private readonly MockTodoCompletionSource _mockTodoCompletionSource;
        private readonly ISpiritSaveRepository _saveRepository;
        private readonly TaskClassifier _taskClassifier;
        private readonly SpiritDayGenerator _dayGenerator;
        private readonly DateTime _initialDate;

        private bool _initialized;
        private PrototypeSaveData _saveData;
        private DailyCompanionRecord _currentRecord;

        public PrototypeApplicationService(
            ITodoCompletionSource todoCompletionSource,
            ISpiritSaveRepository saveRepository,
            TaskClassifier taskClassifier,
            SpiritDayGenerator dayGenerator,
            DateTime? initialDate = null)
        {
            _todoCompletionSource = todoCompletionSource ??
                throw new ArgumentNullException(nameof(todoCompletionSource));
            _mockTodoCompletionSource = todoCompletionSource as MockTodoCompletionSource;
            _saveRepository = saveRepository ??
                throw new ArgumentNullException(nameof(saveRepository));
            _taskClassifier = taskClassifier ??
                throw new ArgumentNullException(nameof(taskClassifier));
            _dayGenerator = dayGenerator ??
                throw new ArgumentNullException(nameof(dayGenerator));
            _initialDate = (initialDate ?? DateTime.Today).Date;
        }

        public bool IsInitialized => _initialized;

        public DateTime CurrentDate { get; private set; }

        public string CurrentDateKey => ToDateKey(CurrentDate);

        public DemoDayId CurrentDemoDay { get; private set; }

        public DemoTemperamentProfile CurrentDemoProfile { get; private set; }

        public string CurrentProfileName =>
            CurrentDemoProfile == DemoTemperamentProfile.ProfileB
                ? "Profile B"
                : "Profile A";

        public DailyCompanionRecord CurrentRecord => _currentRecord;

        public PrototypeSaveData SaveData => _saveData;

        public SpiritState CurrentSpiritState => _saveData == null ? null : _saveData.SpiritState;

        // Compatibility alias for the original prototype presentation API.
        public SpiritState SpiritState => CurrentSpiritState;

        public EssenceWallet EssenceWallet => _saveData == null ? null : _saveData.EssenceWallet;

        public string SavePath => _saveRepository.SavePath;

        public IReadOnlyList<CompletedTask> CurrentCompletedTasks =>
            _currentRecord == null || _currentRecord.CompletedTasks == null
                ? Array.Empty<CompletedTask>()
                : _currentRecord.CompletedTasks;

        public IReadOnlyList<TaskClassification> CurrentClassifications =>
            _currentRecord == null || _currentRecord.Classifications == null
                ? Array.Empty<TaskClassification>()
                : _currentRecord.Classifications;

        public IReadOnlyList<ActionCandidateScore> CurrentCandidateActions =>
            _currentRecord == null || _currentRecord.CandidateActions == null
                ? Array.Empty<ActionCandidateScore>()
                : _currentRecord.CandidateActions;

        public string CurrentSelectionReason =>
            _currentRecord == null ? string.Empty : _currentRecord.SelectionReason;

        public int TodoPresetIndex =>
            _mockTodoCompletionSource == null ? -1 : _mockTodoCompletionSource.CurrentPresetIndex;

        public int TodoPresetCount =>
            _mockTodoCompletionSource == null ? 0 : _mockTodoCompletionSource.PresetCount;

        public string TodoPresetName =>
            _mockTodoCompletionSource == null ? "외부 입력" : _mockTodoCompletionSource.CurrentPresetName;

        public bool HasPendingReward =>
            _currentRecord != null &&
            _currentRecord.GrantedEssenceDelta > 0 &&
            !_currentRecord.RewardAcknowledged;

        public int PendingRewardAmount => HasPendingReward
            ? _currentRecord.GrantedEssenceDelta
            : 0;

        public string CurrentDebugInfo => BuildCurrentDebugInfo();

        public DailyCompanionRecord Initialize()
        {
            if (_initialized)
            {
                return RefreshCachedCurrentDay();
            }

            _saveData = _saveRepository.Load() ?? new PrototypeSaveData();
            NormalizeSaveData();
            CurrentDemoProfile = ResolveProfile(_saveData.SpiritState.SpiritId);
            _saveData.SpiritState = CreateDemoSpirit(CurrentDemoProfile);
            CurrentDemoDay = DemoDayId.Day1WorkFocus;
            CurrentDate = GetDateForDemoDay(CurrentDemoDay);
            SetMockPresetForDemoDay(CurrentDemoDay);
            _initialized = true;

            var record = LoadOrCreateCurrentRecord();
            Persist();
            return record;
        }

        public DailyCompanionRecord GetOrCreateCurrentRecord()
        {
            EnsureInitialized();
            return LoadOrCreateCurrentRecord();
        }

        /// <summary>
        /// Compatibility wrapper. Dates inside the demo window use the named day presets.
        /// </summary>
        public DailyCompanionRecord MoveDate(int days)
        {
            EnsureInitialized();
            return SetCurrentDate(CurrentDate.AddDays(days));
        }

        /// <summary>
        /// Compatibility wrapper for direct date-based debug controls.
        /// </summary>
        public DailyCompanionRecord SetCurrentDate(DateTime date)
        {
            EnsureInitialized();
            if (TryGetDemoDay(date.Date, out var demoDay))
            {
                return SelectDemoDay(demoDay);
            }

            CurrentDate = date.Date;
            _currentRecord = null;
            return LoadOrCreateCurrentRecord();
        }

        public DailyCompanionRecord SelectDemoDay(DemoDayId day)
        {
            EnsureInitialized();
            ValidateDemoDay(day);
            if (_mockTodoCompletionSource == null)
            {
                throw new InvalidOperationException("The active TODO source does not support demo day presets.");
            }

            CurrentDemoDay = day;
            CurrentDate = GetDateForDemoDay(day);
            SetMockPresetForDemoDay(day);
            _currentRecord = null;
            return LoadOrCreateCurrentRecord();
        }

        public DailyCompanionRecord SelectDemoProfile(DemoTemperamentProfile profile)
        {
            EnsureInitialized();
            ValidateDemoProfile(profile);

            CurrentDemoProfile = profile;
            _saveData.SpiritState = CreateDemoSpirit(profile);
            _currentRecord = null;
            var record = LoadOrCreateCurrentRecord();
            Persist();
            return record;
        }

        /// <summary>
        /// Selects the next mock preset. A cached record remains authoritative until explicitly regenerated.
        /// </summary>
        public void CycleTodoPreset()
        {
            EnsureInitialized();
            if (_mockTodoCompletionSource == null)
            {
                throw new InvalidOperationException("The active TODO source does not support mock presets.");
            }

            _mockTodoCompletionSource.CyclePreset();
            RefreshCachedCurrentDay();
        }

        /// <summary>
        /// Rebinds the current view state to the saved profile-and-date record without rerunning simulation.
        /// </summary>
        public DailyCompanionRecord RefreshCachedCurrentDay()
        {
            EnsureInitialized();
            _currentRecord = FindRecord(CurrentDateKey, CurrentSpiritState.SpiritId);
            return _currentRecord ?? CreateCurrentRecord();
        }

        /// <summary>
        /// Replaces only the current profile's current-day record. Account wallet high-water marks remain intact.
        /// </summary>
        public DailyCompanionRecord RegenerateCurrentDay()
        {
            EnsureInitialized();
            var removedRecords = new List<DailyCompanionRecord>();
            for (var i = _saveData.Records.Count - 1; i >= 0; i--)
            {
                var record = _saveData.Records[i];
                if (IsRecordFor(record, CurrentDateKey, CurrentSpiritState.SpiritId))
                {
                    removedRecords.Add(record);
                    _saveData.Records.RemoveAt(i);
                }
            }

            _currentRecord = null;
            try
            {
                return CreateCurrentRecord();
            }
            catch
            {
                _saveData.Records.AddRange(removedRecords);
                _currentRecord = FindRecord(CurrentDateKey, CurrentSpiritState.SpiritId);
                throw;
            }
        }

        /// <summary>
        /// Compatibility wrapper retained for the original debug overlay.
        /// </summary>
        public DailyCompanionRecord ClearCurrentDayAndRegenerate()
        {
            return RegenerateCurrentDay();
        }

        /// <summary>
        /// Replaces the demo save with Profile A / Day 1 only after the fresh save succeeds.
        /// </summary>
        public bool ResetDemoSave()
        {
            EnsureInitialized();

            try
            {
                var resetDate = ToDateKey(GetDateForDemoDay(DemoDayId.Day1WorkFocus));
                var resetSpirit = CreateDemoSpirit(DemoTemperamentProfile.ProfileA);
                var freshSave = new PrototypeSaveData
                {
                    SpiritState = resetSpirit,
                    EssenceWallet = new EssenceWallet()
                };
                freshSave.EnsureCollections();

                var resetTodoSource = new MockTodoCompletionSource(MockTodoPreset.DemoDay1WorkFocus);
                var resetTasks = resetTodoSource.GetCompletedTasks(resetDate);
                var resetRecord = BuildRecord(
                    freshSave,
                    resetSpirit,
                    resetDate,
                    resetTasks);
                freshSave.Records.Add(resetRecord);

                _saveRepository.Save(freshSave);

                _saveData = freshSave;
                _currentRecord = resetRecord;
                CurrentDemoProfile = DemoTemperamentProfile.ProfileA;
                CurrentDemoDay = DemoDayId.Day1WorkFocus;
                CurrentDate = _initialDate;
                SetMockPresetForDemoDay(CurrentDemoDay);
                return true;
            }
            catch (Exception exception)
            {
                Debug.LogError($"Could not reset TodoSpirits demo data at {_saveRepository.SavePath}.\n{exception}");
                return false;
            }
        }

        public void AcknowledgeReward()
        {
            EnsureInitialized();
            if (_currentRecord == null || _currentRecord.RewardAcknowledged)
            {
                return;
            }

            _currentRecord.RewardAcknowledged = true;
            Persist();
        }

        private DailyCompanionRecord LoadOrCreateCurrentRecord()
        {
            var savedRecord = FindRecord(CurrentDateKey, CurrentSpiritState.SpiritId);
            if (savedRecord != null)
            {
                NormalizeRecord(savedRecord);
                _currentRecord = savedRecord;
                return savedRecord;
            }

            return CreateCurrentRecord();
        }

        private DailyCompanionRecord CreateCurrentRecord()
        {
            var completedTasks = _todoCompletionSource.GetCompletedTasks(CurrentDateKey);
            var record = BuildRecord(
                _saveData,
                CurrentSpiritState,
                CurrentDateKey,
                completedTasks);

            _saveData.Records.Add(record);
            _currentRecord = record;
            Persist();
            return record;
        }

        private DailyCompanionRecord BuildRecord(
            PrototypeSaveData saveData,
            SpiritState baseSpiritState,
            string date,
            IReadOnlyList<CompletedTask> completedTasks)
        {
            var tasks = completedTasks == null
                ? new List<CompletedTask>()
                : new List<CompletedTask>(completedTasks);
            var classifications = _taskClassifier.Classify(tasks);
            var historyRecords = BuildHistoryInputRecords(
                saveData,
                baseSpiritState,
                date);
            var historyState = SpiritHistorySnapshot.Build(
                baseSpiritState,
                historyRecords,
                date,
                RecentActionLimit);
            var report = _dayGenerator.Generate(
                date,
                tasks,
                classifications,
                historyState,
                SimulationVersion);

            var essenceReward = EssenceRewardCalculator.Calculate(tasks.Count);
            var rewardBand = EssenceRewardCalculator.GetBand(tasks.Count);
            var grantedDelta = saveData.EssenceWallet.ApplyReward(
                date,
                essenceReward,
                rewardBand);
            var rewardAcknowledged = grantedDelta <= 0;

            var record = new DailyCompanionRecord(
                date,
                tasks,
                essenceReward,
                grantedDelta,
                report,
                rewardAcknowledged)
            {
                SpiritId = baseSpiritState.SpiritId
            };
            return record;
        }

        /// <summary>
        /// Fills only missing earlier demo days in memory so generation is independent of debug visit order.
        /// Existing saved records remain authoritative and transient records never touch wallet or persistence.
        /// </summary>
        private List<DailyCompanionRecord> BuildHistoryInputRecords(
            PrototypeSaveData saveData,
            SpiritState baseSpiritState,
            string targetDate)
        {
            var historyRecords = new List<DailyCompanionRecord>(saveData.Records);
            if (!TryGetDemoDay(targetDate, out var targetDemoDay))
            {
                return historyRecords;
            }

            for (var dayIndex = (int)DemoDayId.Day1WorkFocus;
                 dayIndex < (int)targetDemoDay;
                 dayIndex++)
            {
                var priorDay = (DemoDayId)dayIndex;
                var priorDate = ToDateKey(GetDateForDemoDay(priorDay));
                if (FindRecord(historyRecords, priorDate, baseSpiritState.SpiritId) != null)
                {
                    continue;
                }

                var historyTodoSource = new MockTodoCompletionSource(
                    GetPresetForDemoDay(priorDay));
                var historyTasks = historyTodoSource.GetCompletedTasks(priorDate);
                var transientRecord = BuildHistoryOnlyRecord(
                    baseSpiritState,
                    historyRecords,
                    priorDate,
                    historyTasks);
                historyRecords.Add(transientRecord);
            }

            return historyRecords;
        }

        private DailyCompanionRecord BuildHistoryOnlyRecord(
            SpiritState baseSpiritState,
            IReadOnlyList<DailyCompanionRecord> historyRecords,
            string date,
            IReadOnlyList<CompletedTask> completedTasks)
        {
            var tasks = completedTasks == null
                ? new List<CompletedTask>()
                : new List<CompletedTask>(completedTasks);
            var classifications = _taskClassifier.Classify(tasks);
            var historyState = SpiritHistorySnapshot.Build(
                baseSpiritState,
                historyRecords,
                date,
                RecentActionLimit);
            var report = _dayGenerator.Generate(
                date,
                tasks,
                classifications,
                historyState,
                SimulationVersion);

            return new DailyCompanionRecord(
                date,
                tasks,
                EssenceRewardCalculator.Calculate(tasks.Count),
                0,
                report,
                true)
            {
                SpiritId = baseSpiritState.SpiritId
            };
        }

        private DailyCompanionRecord FindRecord(string date, string spiritId)
        {
            return FindRecord(_saveData.Records, date, spiritId);
        }

        private static DailyCompanionRecord FindRecord(
            IReadOnlyList<DailyCompanionRecord> records,
            string date,
            string spiritId)
        {
            for (var i = 0; i < records.Count; i++)
            {
                var record = records[i];
                if (IsRecordFor(record, date, spiritId))
                {
                    return record;
                }
            }

            return null;
        }

        private static bool IsRecordFor(
            DailyCompanionRecord record,
            string date,
            string spiritId)
        {
            return record != null &&
                   string.Equals(record.Date, date, StringComparison.Ordinal) &&
                   string.Equals(record.SpiritId, spiritId, StringComparison.Ordinal);
        }

        private void NormalizeSaveData()
        {
            _saveData.EnsureCollections();
            _saveData.Records.RemoveAll(record => record == null);

            var profile = _saveData.SpiritState == null
                ? DemoTemperamentProfile.ProfileA
                : ResolveProfile(_saveData.SpiritState.SpiritId);
            _saveData.SpiritState = CreateDemoSpirit(profile);

            if (_saveData.EssenceWallet == null)
            {
                _saveData.EssenceWallet = new EssenceWallet();
            }

            for (var i = 0; i < _saveData.Records.Count; i++)
            {
                NormalizeRecord(_saveData.Records[i]);
            }
        }

        private static SpiritState CreateDemoSpirit(DemoTemperamentProfile profile)
        {
            ValidateDemoProfile(profile);
            if (profile == DemoTemperamentProfile.ProfileB)
            {
                return new SpiritState(
                    ProfileBSpiritId,
                    new[]
                    {
                        SpiritTemperament.Active,
                        SpiritTemperament.Sociable
                    },
                    Array.Empty<SpiritActionId>(),
                    SpiritActionId.SocialTea);
            }

            return new SpiritState(
                ProfileASpiritId,
                new[]
                {
                    SpiritTemperament.Curious,
                    SpiritTemperament.Meticulous
                },
                Array.Empty<SpiritActionId>(),
                SpiritActionId.ReadRecords);
        }

        private static DemoTemperamentProfile ResolveProfile(string spiritId)
        {
            return string.Equals(spiritId, ProfileBSpiritId, StringComparison.Ordinal)
                ? DemoTemperamentProfile.ProfileB
                : DemoTemperamentProfile.ProfileA;
        }

        private static void NormalizeRecord(DailyCompanionRecord record)
        {
            if (string.IsNullOrWhiteSpace(record.SpiritId))
            {
                record.SpiritId = ProfileASpiritId;
            }

            if (record.CompletedTasks == null)
            {
                record.CompletedTasks = new List<CompletedTask>();
            }

            if (record.SpiritDay == null)
            {
                record.SpiritDay = new SpiritDayResult();
            }

            if (record.SpiritDay.SecondaryActions == null)
            {
                record.SpiritDay.SecondaryActions = new List<SpiritActionId>();
            }

            if (record.SpiritDay.TaskActionLinks == null)
            {
                record.SpiritDay.TaskActionLinks = new List<TaskActionLink>();
            }

            if (record.Classifications == null)
            {
                record.Classifications = new List<TaskClassification>();
            }

            if (record.CandidateActions == null)
            {
                record.CandidateActions = new List<ActionCandidateScore>();
            }
        }

        private DateTime GetDateForDemoDay(DemoDayId day)
        {
            ValidateDemoDay(day);
            return _initialDate.AddDays((int)day).Date;
        }

        private bool TryGetDemoDay(DateTime date, out DemoDayId day)
        {
            var offset = (date.Date - _initialDate).Days;
            if (offset >= (int)DemoDayId.Day1WorkFocus &&
                offset <= (int)DemoDayId.Day3Rest)
            {
                day = (DemoDayId)offset;
                return true;
            }

            day = CurrentDemoDay;
            return false;
        }

        private bool TryGetDemoDay(string date, out DemoDayId day)
        {
            for (var dayIndex = (int)DemoDayId.Day1WorkFocus;
                 dayIndex <= (int)DemoDayId.Day3Rest;
                 dayIndex++)
            {
                var candidate = (DemoDayId)dayIndex;
                if (string.Equals(
                        date,
                        ToDateKey(GetDateForDemoDay(candidate)),
                        StringComparison.Ordinal))
                {
                    day = candidate;
                    return true;
                }
            }

            day = CurrentDemoDay;
            return false;
        }

        private void SetMockPresetForDemoDay(DemoDayId day)
        {
            if (_mockTodoCompletionSource != null)
            {
                _mockTodoCompletionSource.SetPreset(GetPresetForDemoDay(day));
            }
        }

        private static MockTodoPreset GetPresetForDemoDay(DemoDayId day)
        {
            switch (day)
            {
                case DemoDayId.Day2HealthRelationship:
                    return MockTodoPreset.DemoDay2HealthRelationship;
                case DemoDayId.Day3Rest:
                    return MockTodoPreset.DemoDay3Rest;
                default:
                    return MockTodoPreset.DemoDay1WorkFocus;
            }
        }

        private static void ValidateDemoDay(DemoDayId day)
        {
            var value = (int)day;
            if (value < (int)DemoDayId.Day1WorkFocus ||
                value > (int)DemoDayId.Day3Rest)
            {
                throw new ArgumentOutOfRangeException(nameof(day), day, "Unknown demo day.");
            }
        }

        private static void ValidateDemoProfile(DemoTemperamentProfile profile)
        {
            var value = (int)profile;
            if (value < (int)DemoTemperamentProfile.ProfileA ||
                value > (int)DemoTemperamentProfile.ProfileB)
            {
                throw new ArgumentOutOfRangeException(nameof(profile), profile, "Unknown demo temperament profile.");
            }
        }

        private void EnsureInitialized()
        {
            if (!_initialized)
            {
                Initialize();
            }
        }

        private bool Persist()
        {
            try
            {
                _saveRepository.Save(_saveData);
                return true;
            }
            catch (Exception exception)
            {
                Debug.LogError($"Could not save TodoSpirits prototype data to {_saveRepository.SavePath}.\n{exception}");
                return false;
            }
        }

        private string BuildCurrentDebugInfo()
        {
            if (!_initialized || _currentRecord == null)
            {
                return "Prototype service is not initialized.";
            }

            var builder = new StringBuilder(1280);
            builder.AppendLine($"Demo Day: {CurrentDemoDay}");
            builder.AppendLine(
                $"Demo Profile: {CurrentProfileName} ({CurrentSpiritState.SpiritId})");
            builder.AppendLine($"Current Date: {CurrentDateKey}");
            builder.AppendLine($"Mock Preset: {TodoPresetName} ({TodoPresetIndex + 1}/{TodoPresetCount})");
            builder.AppendLine("Completed TODOs:");
            for (var i = 0; i < CurrentCompletedTasks.Count; i++)
            {
                var task = CurrentCompletedTasks[i];
                builder.AppendLine($"  - {task.Title} [{task.UserCategory}] ({task.TaskId})");
            }

            if (CurrentCompletedTasks.Count == 0)
            {
                builder.AppendLine("  - 없음");
            }

            builder.AppendLine("Classifications:");
            for (var i = 0; i < CurrentClassifications.Count; i++)
            {
                var classification = CurrentClassifications[i];
                builder.AppendLine(
                    $"  - {classification.TaskId}: {classification.InferredSubcategory}, " +
                    $"confidence {classification.ClassificationConfidence.ToString("0.00", CultureInfo.InvariantCulture)}");
                builder.AppendLine($"    reason: {classification.Reason}");
            }

            builder.Append("Temperaments: ");
            AppendValues(builder, CurrentSpiritState.Temperaments);
            builder.AppendLine();
            builder.AppendLine("Candidate Actions:");
            for (var i = 0; i < CurrentCandidateActions.Count; i++)
            {
                var candidate = CurrentCandidateActions[i];
                builder.AppendLine(
                    $"  - {candidate.Action}: total {FormatScore(candidate.TotalScore)} " +
                    $"(base {FormatScore(candidate.BaseScore)}, task {FormatScore(candidate.TaskScore)}, " +
                    $"temperament {FormatScore(candidate.TemperamentScore)}, favorite {FormatScore(candidate.FavoriteActionScore)}, " +
                    $"repeat {FormatScore(candidate.RepetitionPenalty)}, jitter {FormatScore(candidate.DeterministicJitter)})");
                builder.AppendLine($"    reason: {candidate.Reason}");
            }

            var day = _currentRecord.SpiritDay;
            builder.AppendLine($"Primary Action: {day.PrimaryAction}");
            builder.Append("Secondary Actions: ");
            AppendValues(builder, day.SecondaryActions);
            builder.AppendLine();
            builder.AppendLine($"Location: {day.Location}");
            builder.AppendLine($"Selection Reason: {_currentRecord.SelectionReason}");
            builder.AppendLine($"Seed: {day.Seed}");
            builder.AppendLine(
                $"Essence Reward: {_currentRecord.EssenceReward} " +
                $"(granted delta {_currentRecord.GrantedEssenceDelta}, wallet {_saveData.EssenceWallet.Balance})");
            return builder.ToString();
        }

        private static string ToDateKey(DateTime date)
        {
            return date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
        }

        private static string FormatScore(float score)
        {
            return score.ToString("0.00", CultureInfo.InvariantCulture);
        }

        private static void AppendValues<T>(StringBuilder builder, IReadOnlyList<T> values)
        {
            if (values == null || values.Count == 0)
            {
                builder.Append("없음");
                return;
            }

            for (var i = 0; i < values.Count; i++)
            {
                if (i > 0)
                {
                    builder.Append(", ");
                }

                builder.Append(values[i]);
            }
        }
    }
}
