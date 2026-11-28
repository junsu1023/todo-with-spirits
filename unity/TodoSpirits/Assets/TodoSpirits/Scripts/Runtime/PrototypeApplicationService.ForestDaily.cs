using System;
using System.Collections.Generic;
using System.Linq;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime
{
    public sealed partial class PrototypeApplicationService
    {
        private ForestSyncState _forestSyncState = ForestSyncState.Awaiting;
        private string _forestSyncMessage;
        private int _forestDialogueIndex;
        private string _forestDialogueDate;
        public event Action ForestTodayChanged;
        public event Action ForestSnapshotRequested;
        public event Action<IReadOnlyList<string>> ForestHistoryRequested;
        private string ForestAccountKey => IsHostConnected ? _saveData.Forest.HostAccountId : "local";

        public void BindForestHostAccount(string accountId)
        {
            EnsureInitialized();
            if (string.IsNullOrWhiteSpace(accountId) || accountId.Length > 128) throw new ArgumentException("계정을 확인해 주세요.");
            if (IsHostConnected) {
                if (ForestAccountKey != accountId) throw new InvalidOperationException("계정별 저장을 먼저 열어 주세요.");
                return;
            }
            if (_saveData.Forest.Todos.Count > 0 || _saveData.Records.Any(r => r.CompletedTasks.Count > 0))
                throw new InvalidOperationException("로컬 생활 기록을 다른 계정에 덮어쓸 수 없어요.");
            MutateLife(() => _saveData.Forest.HostAccountId = accountId);
        }

        public void RequestForestHistory(string month)
        {
            EnsureInitialized();
            if (!IsHostConnected) return;
            if (!DateTime.TryParseExact(month + "-01", "yyyy-MM-dd", System.Globalization.CultureInfo.InvariantCulture,
                    System.Globalization.DateTimeStyles.None, out _)) throw new ArgumentException("확인할 월이 필요해요.");
            var dates = _saveData.Forest.Days.Where(d => d.AccountId == ForestAccountKey && d.Date.StartsWith(month, StringComparison.Ordinal) &&
                    string.CompareOrdinal(d.Date, CurrentDateKey) < 0).Select(d => d.Date).Distinct().OrderBy(d => d).ToList();
            if (dates.Count > 0) ForestHistoryRequested?.Invoke(dates);
        }

        public void SetCompanionNameEarly(string name) => MutateLife(() => CompanionLifeRules.SetName(CurrentLife, name));
        public void SkipCompanionNaming() => MutateLife(() => CurrentLife.NamingOffered = true);
        public void BeginFarewell() => MutateLife(() => {
            if (CurrentLife.Stage != CompanionStage.Adult || CurrentLife.Farewell != FarewellStep.Growing)
                throw new InvalidOperationException("성체가 된 정령과 계속 지내다가, 원할 때 새로운 동행을 준비할 수 있어요.");
            if (PendingTrip != null) throw new InvalidOperationException("나들이에서 돌아온 뒤 다음 동행을 준비해 주세요.");
            CurrentLife.Farewell = FarewellStep.AdultReveal;
        });

        private void RefreshForestStoryFamily()
        {
            if (CurrentLife.Story.Choices.Count > 0) return;
            var groups = CurrentCompletedTasks.GroupBy(t => t.UserCategory).ToList();
            int focused = groups.Where(g => g.Key == UserTaskCategory.WorkStudy || g.Key == UserTaskCategory.SelfDevelopment ||
                g.Key == UserTaskCategory.Assets || g.Key == UserTaskCategory.Economy).Sum(g => Math.Min(2, g.Count()));
            int moving = groups.Where(g => g.Key == UserTaskCategory.Health || g.Key == UserTaskCategory.Living).Sum(g => Math.Min(2, g.Count()));
            int others = groups.Where(g => g.Key != UserTaskCategory.WorkStudy && g.Key != UserTaskCategory.SelfDevelopment &&
                g.Key != UserTaskCategory.Assets && g.Key != UserTaskCategory.Economy && g.Key != UserTaskCategory.Health &&
                g.Key != UserTaskCategory.Living).Sum(g => Math.Min(2, g.Count()));
            CurrentLife.Story.Family = focused > moving && focused > others ? ForestStoryFamily.Focus :
                moving > focused && moving > others ? ForestStoryFamily.Movement : ForestStoryFamily.Everyday;
            CurrentLife.Story.FamilyEvidence = groups.OrderBy(g => (int)g.Key).Select(g =>
                ForestStoryRules.CategoryName(g.Key) + " · 완료 " + g.Count() + "개 · 이야기 기여 " + Math.Min(2, g.Count())).ToList();
        }

        public void SetForestSyncState(ForestSyncState state, string message = null)
        {
            EnsureInitialized();
            if (state == ForestSyncState.Confirmed || state == ForestSyncState.Local)
                throw new ArgumentException("확인 상태는 검증한 목록을 저장한 뒤에만 설정할 수 있어요.");
            _forestSyncState = state;
            _forestSyncMessage = message;
            ForestTodayChanged?.Invoke();
        }

        public void RequestForestSnapshot()
        {
            SetForestSyncState(ForestSyncState.Awaiting);
            ForestSnapshotRequested?.Invoke();
        }

        public void RestorePrototypeDemoConnection()
        {
            EnsureInitialized();
            if (_saveData.Forest.HostAccountId != "prototype-demo-v1" || _saveData.Forest.HostDate != CurrentDateKey)
                throw new InvalidOperationException("현재 날짜의 별도 예시 저장에서만 연결 상태를 복원할 수 있어요.");
            _forestSyncState = ForestSyncState.Confirmed;
            _forestSyncMessage = null;
            ForestTodayChanged?.Invoke();
        }

        private ForestDailyMemory FindForestDay(string account, string date, string spiritId = null) =>
            _saveData.Forest.Days.LastOrDefault(d => d != null && d.AccountId == account && d.Date == date &&
                (spiritId == null || d.SpiritId == spiritId));

        private ForestDailyMemory EnsureForestDay()
        {
            var day = FindForestDay(ForestAccountKey, CurrentDateKey, ForestMemorySpiritId);
            if (day != null) return day;
            day = new ForestDailyMemory { AccountId = ForestAccountKey, Date = CurrentDateKey, SpiritId = ForestMemorySpiritId };
            _saveData.Forest.Days.Add(day);
            return day;
        }

        public ForestTodaySummary GetForestTodaySummary()
        {
            EnsureInitialized();
            var memory = FindForestDay(ForestAccountKey, CurrentDateKey, ForestMemorySpiritId);
            var accountDay = _saveData.Forest.Days.Where(d => d.AccountId == ForestAccountKey && d.Date == CurrentDateKey).ToList();
            bool local = !IsHostConnected && (TodayTodos.Count > 0 || (memory != null && memory.Revision >= 0));
            bool cached = IsHostConnected && _saveData.Forest.HostDate == CurrentDateKey;
            var state = local ? ForestSyncState.Local : _forestSyncState;
            // Midnight invalidates yesterday's successful connection, even while the host is slow.
            if (state == ForestSyncState.Confirmed && !cached) state = ForestSyncState.Awaiting;
            var rows = cached || local ? BuildForestDailyTodos() : new List<ForestDailyTodo>();
            var summary = new ForestTodaySummary {
                AccountId = ForestAccountKey, Date = CurrentDateKey, SpiritId = CurrentLife.SpiritId,
                Revision = cached ? _saveData.Forest.HostRevision : memory?.Revision ?? -1,
                SyncState = state, SyncMessage = ForestSyncMessage(state),
                LastConfirmedAtUtc = IsHostConnected ? _saveData.Forest.LastConfirmedAtUtc : memory?.LastConfirmedAtUtc,
                HasConfirmedData = cached || local, IsFirstVisit = !accountDay.Any(d => d.Displayed),
                TotalEssence = EssenceWallet.GetHighestReward(CurrentDateKey),
                Todos = rows, Activities = BuildForestActivities(rows, cached || local, HasActiveForestCompanion)
            };
            summary.NewEssence = Math.Max(0, summary.TotalEssence - accountDay.Select(d => d.AcknowledgedEssence).DefaultIfEmpty(0).Max());
            var acknowledgedAcquisitions = accountDay.Where(d => d.SpiritId == CurrentLife.SpiritId || d.SpiritId == ForestMemorySpiritId)
                .SelectMany(d => d.AcknowledgedAcquisitions).ToList();
            summary.Acquisitions = CollectForestAcquisitions(CurrentDateKey)
                .Where(x => !acknowledgedAcquisitions.Contains(x)).ToList();
            summary.Mood = summary.Acquisitions.Count > 0 || rows.Any(t => t.IsCompleted && !t.CompletedWithPreviousSpirit && !t.AwaitingCompanion) ||
                Interventions.Gifts.Any(g => g.SpiritId == CurrentLife.SpiritId && g.Date == CurrentDateKey)
                ? "기쁨" : rows.Count > 0 ? "기대" : "차분함";
            return summary;
        }

        private string ForestSyncMessage(ForestSyncState state)
        {
            if (!string.IsNullOrEmpty(_forestSyncMessage) && (state == ForestSyncState.Error || state == ForestSyncState.Offline))
                return _forestSyncMessage;
            switch (state) {
                case ForestSyncState.Confirmed: return "오늘 기록을 확인했어요";
                case ForestSyncState.Local: return "이 기기에 저장된 기록이에요";
                case ForestSyncState.Offline: return "오프라인 · 최신 일정 확인 불가";
                case ForestSyncState.Error: return "기록을 확인하지 못했어요 · 다시 시도해 주세요";
                default: return IsHostConnected ? "오늘 기록을 확인하고 있어요" : "TODO 앱의 오늘 기록을 기다리고 있어요";
            }
        }

        /// <summary>The displayed snapshot is acknowledged, never a newer unseen reward revision.
        /// This only records reading; rewards were already committed with the TODO snapshot.</summary>
        public void AcknowledgeForestToday(ForestTodaySummary displayed = null)
        {
            displayed = displayed ?? GetForestTodaySummary();
            if (displayed.AccountId != ForestAccountKey || displayed.Date != CurrentDateKey || displayed.SpiritId != CurrentLife.SpiritId)
                throw new InvalidOperationException("날짜나 계정이 바뀌었어요. 오늘 기록을 다시 열어 주세요.");
            MutateLife(() => {
                var day = EnsureForestDay();
                if (displayed.HasConfirmedData && day.Revision < 0) CaptureForestDay();
                day.Displayed = true;
                day.AcknowledgedEssence = Math.Max(day.AcknowledgedEssence,
                    Math.Min(displayed.TotalEssence, EssenceWallet.GetHighestReward(CurrentDateKey)));
                var actual = CollectForestAcquisitions(CurrentDateKey);
                foreach (var acquisition in displayed.Acquisitions)
                    if (actual.Contains(acquisition) && !day.AcknowledgedAcquisitions.Contains(acquisition))
                        day.AcknowledgedAcquisitions.Add(acquisition);
                if (displayed.HasConfirmedData && HasActiveForestCompanion && string.IsNullOrEmpty(day.ObservedAtUtc)) {
                    day.ObservedRevision = displayed.Revision;
                    day.ObservedAtUtc = DateTime.UtcNow.ToString("O");
                    day.ObservedActivities = CloneActivities(displayed.Activities);
                    day.AppearancePhase = CurrentLife.Stage;
                    day.PrimaryActivity = displayed.Activities.FirstOrDefault()?.ActivityKey;
                    day.ChoiceCount = CurrentLife.Story.Choices.Count;
                    day.LastChoiceOption = CurrentLife.Story.Choices.LastOrDefault()?.Option ?? -1;
                }
            });
        }

        // Invoked inside the same MutateLife transaction as each fact/choice change.
        private void CaptureForestDay()
        {
            var day = EnsureForestDay();
            day.Revision = IsHostConnected ? _saveData.Forest.HostRevision : Math.Max(0, day.Revision + 1);
            day.LastConfirmedAtUtc = IsHostConnected ? _saveData.Forest.LastConfirmedAtUtc : DateTime.UtcNow.ToString("O");
            day.TotalEssence = EssenceWallet.GetHighestReward(CurrentDateKey);
            day.Todos = BuildForestDailyTodos();
            day.Activities = BuildForestActivities(day.Todos, true, HasActiveForestCompanion);
            day.Acquisitions = CollectForestAcquisitions(CurrentDateKey);
            day.ChoiceCount = HasActiveForestCompanion ? CurrentLife.Story.Choices.Count : 0;
            day.LastChoiceOption = HasActiveForestCompanion ? CurrentLife.Story.Choices.LastOrDefault()?.Option ?? -1 : -1;
            day.StoryFamily = HasActiveForestCompanion ? CurrentLife.Story.Family : ForestStoryFamily.Legacy;
            day.VisiblePropName = HasActiveForestCompanion ? ForestStoryRules.VisiblePropName(CurrentLife) : "";
            // An unseen day's latest appearance is useful in the album; once observed it is frozen.
            if (string.IsNullOrEmpty(day.ObservedAtUtc)) {
                day.AppearancePhase = HasActiveForestCompanion ? CurrentLife.Stage : CompanionStage.Meeting;
                day.PrimaryActivity = day.Activities.FirstOrDefault()?.ActivityKey;
            }
            foreach (var previous in _saveData.Forest.Days.Where(d => d != day && d.AccountId == ForestAccountKey && d.Date == CurrentDateKey)) {
                previous.Todos = BuildForestDailyTodos(previous.SpiritId);
                previous.Revision = day.Revision;
                previous.LastConfirmedAtUtc = day.LastConfirmedAtUtc;
                // The previous companion's already-seen scene is kept, while account facts stay current.
            }
        }

        private List<ForestDailyTodo> BuildForestDailyTodos(string spiritId = null) => TodayTodos.Select(t => {
            var row = MapForestTodo(t, t.CompletedDates.Contains(CurrentDateKey));
            DecorateForestTodoOwnership(row, CurrentDateKey, spiritId ?? ForestMemorySpiritId);
            return row;
        }).ToList();

        private static ForestDailyTodo MapForestTodo(ForestTodo todo, bool completed)
        {
            string key, place, done, planned;
            switch (todo.Category) {
                case UserTaskCategory.WorkStudy:
                    key = "read"; place = "책상"; done = "책을 읽고 메모를 남겼어요"; planned = "책과 메모를 펼쳐 둘게요"; break;
                case UserTaskCategory.Health:
                    key = "walk"; place = "숲길"; done = "숲길을 걷고 표식을 살폈어요"; planned = "작은 가방을 챙겨 숲길에 나설 준비를 해요"; break;
                case UserTaskCategory.Living:
                    key = "craft"; place = "공작대"; done = "생활 소품을 손질하고 정리했어요"; planned = "정리할 소품과 도구를 꺼내 두었어요"; break;
                case UserTaskCategory.Relationships:
                    key = "tea"; place = "티테이블"; done = "차를 나누고 작은 편지를 남겼어요"; planned = "인사를 나눌 찻잔과 편지를 준비해요"; break;
                case UserTaskCategory.SelfDevelopment:
                    key = "read"; place = "책상"; done = "새로운 것을 연습하고 수첩에 기록했어요"; planned = "연습 노트를 펴고 함께 시작할 준비를 해요"; break;
                case UserTaskCategory.Hobby:
                    key = "craft"; place = "공작대"; done = "작은 작품에 좋아하는 모양을 담았어요"; planned = "만들고 싶은 모양을 떠올리며 도구를 골라요"; break;
                case UserTaskCategory.RestMind:
                    key = "rest"; place = "쉼터"; done = "방석에 앉아 숲의 소리를 들었어요"; planned = "편히 머물 방석을 준비해요"; break;
                case UserTaskCategory.Assets:
                case UserTaskCategory.Economy:
                    key = "read"; place = "책상"; done = "필요한 물건을 장부에 적고 정리했어요"; planned = "필요한 것을 살펴볼 장부를 펼쳐요"; break;
                default:
                    key = "companion"; place = "숲 공터"; done = "네 기록을 곁에 두고 오늘의 흔적을 남겼어요"; planned = "숲을 둘러보며 네 약속을 곁에 두어요"; break;
            }
            return new ForestDailyTodo {
                Id = todo.Id, Title = todo.Title, Category = todo.Category, IsHabit = todo.Habit,
                IsCompleted = completed, ActivityKey = key, Location = place, Activity = completed ? done : planned,
                Reason = todo.Category == UserTaskCategory.Unspecified ? "분야는 미정으로 보존하고, 정령의 공통 생활과 연결해요."
                    : ForestStoryRules.CategoryName(todo.Category) + " 기록을 정령의 " + place + " 생활로 연결해요."
            };
        }

        private CompanionGift ActiveForestGift(string date, string spiritId) => Interventions.Gifts
            .Where(g => g != null && g.SpiritId == spiritId && g.GiftId >= 0 && g.GiftId < CompanionItemCatalog.Gifts.Length &&
                string.CompareOrdinal(g.Date, date) < 0 && string.CompareOrdinal(date, g.ExpiresAfterDate) <= 0)
            .OrderBy(g => g.Date, StringComparer.Ordinal).LastOrDefault();

        private List<ForestDailyActivity> BuildForestActivities(List<ForestDailyTodo> rows, bool confirmed,
            bool hasCompanion = true, string date = null, string spiritId = null)
        {
            var activities = BuildForestTodoActivities(rows, confirmed, hasCompanion);
            if (!confirmed || !hasCompanion) return activities;
            var gift = ActiveForestGift(date ?? CurrentDateKey, spiritId ?? CurrentLife.SpiritId);
            if (gift == null) return activities;
            string[] keys = { "read", "rest", "craft", "craft", "tea", "walk" };
            string[] places = { "책상", "쉼터", "공작대", "공작대", "티테이블", "숲길" };
            string[] actions = { "선물 받은 책을 펼쳐 마음에 드는 부분을 살펴봐요", "선물 받은 담요를 곁에 두고 편히 쉬어요",
                "선물 받은 그림 도구로 작은 모양을 그려 봐요", "선물 받은 화분을 돌보며 새싹을 살펴봐요",
                "선물 받은 차를 준비하며 향을 느껴요", "선물 받은 돌과 닮은 무늬를 숲길에서 찾아봐요" };
            activities.Insert(0, new ForestDailyActivity {
                ActivityKey = keys[gift.GiftId], Location = places[gift.GiftId], Activity = actions[gift.GiftId],
                IsAutonomous = true, GiftSource = CompanionItemCatalog.Gifts[gift.GiftId], GiftGivenOn = gift.Date,
                Reason = gift.Date + "에 전한 " + CompanionItemCatalog.Gifts[gift.GiftId] +
                    "에서 이어진 정령의 관심이에요. 내 일정 완료나 추가 보상으로 세지 않아요."
            });
            return activities;
        }

        private static List<ForestDailyActivity> BuildForestTodoActivities(List<ForestDailyTodo> rows, bool confirmed, bool hasCompanion = true)
        {
            if (!hasCompanion) return new List<ForestDailyActivity>();
            bool previousCompletions = rows.Any(t => t.CompletedWithPreviousSpirit);
            rows = rows.Where(t => !t.CompletedWithPreviousSpirit && !t.AwaitingCompanion).ToList();
            if (rows.Count == 0) return !confirmed ? new List<ForestDailyActivity>() : new List<ForestDailyActivity> {
                new ForestDailyActivity { ActivityKey = "rest", Location = "쉼터", IsAutonomous = true,
                    Activity = "정령은 숲을 둘러보며 느긋한 하루를 보내요", Reason = previousCompletions
                        ? "오늘 완료는 이전 정령과 함께한 기록이에요. 새 정령은 자기만의 시간을 보내요."
                        : "확인된 오늘 일정이 없어요. 정령 스스로 고른 하루이며 완료나 보상으로 세지 않아요." }
            };
            // At most two contributions per category; splitting one task into many cannot dominate.
            return rows.GroupBy(t => new { t.ActivityKey, t.IsCompleted })
                .OrderByDescending(g => g.Key.IsCompleted)
                .ThenByDescending(g => g.GroupBy(t => t.Category).Sum(c => Math.Min(2, c.Count())))
                .ThenBy(g => g.Key.ActivityKey, StringComparer.Ordinal).Take(3)
                .Select(g => new ForestDailyActivity {
                    ActivityKey = g.Key.ActivityKey, Location = g.First().Location, Activity = g.First().Activity,
                    Reason = g.First().Reason, IsCompleted = g.Key.IsCompleted, SourceTodoIds = g.Select(t => t.Id).ToList()
                }).ToList();
        }

        private List<string> CollectForestAcquisitions(string date)
        {
            var results = CurrentLife.Story.Choices.Where(c => c.Date == date).Select(c => c.Caption).ToList();
            results.AddRange(Interventions.Trips.Where(t => t.SpiritId == CurrentLife.SpiritId && t.Claimed && t.ClaimedOn == date)
                .Select(t => t.Discovery));
            results.AddRange(_saveData.Companions.Where(l => l.Story.ReunionClaimed && l.Story.ReunionDate == date)
                .Select(l => l.Story.ReunionGift));
            return results.Where(x => !string.IsNullOrEmpty(x)).Distinct().ToList();
        }

        private static List<ForestDailyActivity> CloneActivities(IEnumerable<ForestDailyActivity> activities) =>
            activities.Select(a => JsonUtility.FromJson<ForestDailyActivity>(JsonUtility.ToJson(a))).ToList();

        /// <summary>Explicit historical correction updates factual counts only. Previously granted
        /// essence, observed scenes, choices and growth are not replayed or retroactively revoked.</summary>
        public bool CorrectForestDayFacts(ForestTodoSnapshot snapshot)
        {
            EnsureInitialized();
            if (snapshot == null || !IsHostConnected || snapshot.schemaVersion != 1 || snapshot.accountId != ForestAccountKey ||
                snapshot.revision < 0 || !DateTime.TryParseExact(snapshot.date, "yyyy-MM-dd", System.Globalization.CultureInfo.InvariantCulture,
                    System.Globalization.DateTimeStyles.None, out var date) || date >= CurrentDate)
                throw new ArgumentException("현재 계정의 이전 날짜에 대한 정정 목록이 필요해요.");
            var memory = FindForestDay(ForestAccountKey, snapshot.date);
            if (memory == null) throw new InvalidOperationException("보관한 날짜의 기록만 정정할 수 있어요.");
            if (snapshot.revision <= memory.Revision) return false;
            if (snapshot.items == null || snapshot.items.Count > 50) throw new ArgumentException("전체 목록은 0~50개여야 해요.");
            var rows = new List<ForestDailyTodo>(); var ids = new HashSet<string>();
            foreach (var item in snapshot.items) {
                if (item == null || string.IsNullOrWhiteSpace(item.id) || item.id.Length > 128 || !ids.Add(item.id) ||
                    string.IsNullOrWhiteSpace(item.title) || item.title.Length > 255 || item.title.Any(char.IsControl))
                    throw new ArgumentException("중복 없는 ID와 1~255자의 제목이 필요해요.");
                rows.Add(MapForestTodo(new ForestTodo { Id = item.id, Title = item.title, Category = MapHostCategory(item.category), Habit = item.isHabit }, item.isCompleted));
            }
            MutateLife(() => {
                foreach (var row in rows.Where(t => t.IsCompleted))
                    if (ForestOwner(snapshot.date, row.Id) == null)
                        _saveData.Forest.CompletionOwners.Add(new ForestCompletionOwnership { Date = snapshot.date, TodoId = row.Id, SpiritId = memory.SpiritId });
                foreach (var day in _saveData.Forest.Days.Where(d => d.AccountId == ForestAccountKey && d.Date == snapshot.date)) {
                    day.Todos = rows.Select(t => JsonUtility.FromJson<ForestDailyTodo>(JsonUtility.ToJson(t))).ToList();
                    foreach (var row in day.Todos) DecorateForestTodoOwnership(row, snapshot.date, day.SpiritId);
                    day.Activities = BuildForestActivities(day.Todos, true, !string.IsNullOrEmpty(day.SpiritId), day.Date, day.SpiritId);
                    day.Revision = snapshot.revision;
                    day.LastConfirmedAtUtc = DateTime.UtcNow.ToString("O");
                }
                UpdateForestRecordFacts(snapshot.date, rows.Where(t => t.IsCompleted).Select(t =>
                    new CompletedTask(t.Id, t.Title, t.Category, snapshot.date) { RoutineId = t.IsHabit ? t.Id : "" }).ToList());
            });
            ForestTodayChanged?.Invoke();
            return true;
        }

        public string NextForestDialogue()
        {
            var summary = GetForestTodaySummary();
            if (_forestDialogueDate != CurrentDateKey) { _forestDialogueDate = CurrentDateKey; _forestDialogueIndex = 0; }
            int index = _forestDialogueIndex++;
            bool careful = CurrentLife.Temperaments.Count > 0 &&
                (CurrentLife.Temperaments[index % CurrentLife.Temperaments.Count] == SpiritTemperament.Meticulous ||
                 CurrentLife.Temperaments[index % CurrentLife.Temperaments.Count] == SpiritTemperament.Cautious);
            if (PendingTrip != null)
            {
                if (CanClaimTrip(DateTime.UtcNow))
                    return index % 2 == 0 ? "나들이에서 돌아왔어! 숲길에서 가져온 이야기를 같이 펼쳐 볼래?"
                        : careful ? "발견한 것을 차근차근 챙겨 왔어. 나들이 소식에서 확인해 줘." : "숲길에 재미있는 풍경이 있었어. 무슨 일이 있었는지 들려줄게!";
                string preparation = PendingTrip.Preparation >= 0 && PendingTrip.Preparation < CompanionItemCatalog.Preparations.Length
                    ? CompanionItemCatalog.Preparations[PendingTrip.Preparation] : "작은 짐";
                return index % 2 == 0 ? preparation + "을 챙겨 숲길을 둘러보고 있어. 돌아오면 이야기를 들려줄게."
                    : careful ? "숲길의 표식을 하나씩 살피고 있어. 서두르지 않고 다녀올게." : "숲길 모퉁이에는 어떤 풍경이 있을까? 조금 더 둘러보고 올게!";
            }
            if (summary.SyncState == ForestSyncState.Awaiting || summary.SyncState == ForestSyncState.Error || summary.SyncState == ForestSyncState.Offline)
                return index % 2 == 0 ? "오늘 기록은 아직 새로 확인하지 못했어. 여기서 같이 기다릴게."
                    : careful ? "확인된 이야기부터 차근차근 모아 둘게." : "기록이 도착하면 오늘 이야기를 들려줘. 지금은 숲을 둘러볼까?";
            var inspired = summary.Activities.FirstOrDefault(a => !string.IsNullOrEmpty(a.GiftSource));
            if (inspired != null && index % 2 == 0)
                return "전에 전해 준 " + inspired.GiftSource + ", 오늘도 곁에 두고 있어. " +
                    (careful ? "내 속도로 천천히 살펴볼게." : "덕분에 나만의 작은 시간이 생겼어!");
            if (summary.Acquisitions.Count > 0)
                return index % 2 == 0 ? "우리에게 새 기억이 생겼어. 오늘 기록에서 함께 살펴보자!"
                    : careful ? "새로 얻은 기억을 잊지 않게 차곡차곡 모아 뒀어." : "새 기억 다음에는 어떤 이야기가 기다리고 있을까?";
            if (HasForestChoice)
                return index % 2 == 0 ? "우리에게 새로운 이야기가 생겼어. 함께 골라 줄래?"
                    : careful ? "두 가지 방법을 생각해 뒀어. 천천히 살펴보자." : "함께 고르면 다음 풍경이 달라질 것 같아!";
            var gift = Interventions.Gifts.LastOrDefault(g => g.SpiritId == CurrentLife.SpiritId && g.Date == CurrentDateKey);
            if (gift != null && index % 3 == 1)
                return "오늘 전해 준 " + CompanionItemCatalog.Gifts[gift.GiftId] + ", 곁에 두니 기뻐. " +
                    (careful ? "소중히 정리해 둘게." : "다음에는 어떻게 써 볼까?");
            if (CurrentLife.Story.Choices.Count > 0 && index % 3 == 2)
                return ForestStoryRules.ContinuingDialogue(CurrentLife);
            var eligible = summary.Todos.Where(t => !t.CompletedWithPreviousSpirit && !t.AwaitingCompanion).ToList();
            var completed = eligible.Where(t => t.IsCompleted).ToList();
            var candidates = completed.Count > 0 ? completed : eligible;
            if (candidates.Count == 0)
            {
                if (summary.Todos.Any(t => t.CompletedWithPreviousSpirit))
                    return "이전 친구와 함께한 오늘의 기록을 소중히 둘게. 우리도 새 약속부터 천천히 시작하자.";
                return index % 2 == 0 ? "확인한 오늘 일정은 없네. 나는 천천히 숲을 둘러볼게."
                    : careful ? "오늘은 숲의 작은 풍경을 하나씩 기억해 둘게." : "일정이 없는 날에도 숲에는 새로 볼 것이 있네.";
            }
            var todo = candidates[(index / 2) % candidates.Count];
            var titleElements = new System.Globalization.StringInfo(todo.Title);
            string spokenTitle = titleElements.LengthInTextElements > 18 ? titleElements.SubstringByTextElements(0, 18) + "…" : todo.Title;
            string fact = "‘" + spokenTitle + "’" + (todo.IsCompleted ? "을 마쳤구나. " : "이 오늘의 약속이구나. ");
            if (!todo.IsCompleted) return fact + (careful ? "차근차근 준비해 둘게. 네 속도로 함께하자." : "나는 " + todo.Location + "에서 준비하고 있을게!");
            return fact + (careful ? "오늘의 흔적을 수첩에 가지런히 남길게." : "나도 " + todo.Location + "에서 보낸 이야기가 있어!");
        }
    }
}
