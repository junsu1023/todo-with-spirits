using System;
using System.Collections.Generic;
using System.Linq;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    public sealed partial class PrototypeApplicationService
    {
        public int ForestActiveDays => CurrentLife.ActivityDays +
            (HasActiveForestCompanion && !CurrentLife.FinalizedDates.Contains(CurrentDateKey) && CurrentCompletedTasks.Count > 0 ? 1 : 0);
        public bool HasForestChoice => ForestStoryRules.CanChoose(CurrentLife, ForestActiveDays);
        public IReadOnlyList<ForestTodo> TodayTodos => _saveData.Forest.Todos.Where(t =>
            !t.Archived && string.CompareOrdinal(t.CreatedOn, CurrentDateKey) <= 0 &&
            (t.Habit || t.CreatedOn == CurrentDateKey)).ToList();
        public CompanionLife ReunionGuest => ForestActiveDays < 2 ? null : _saveData.Companions.Find(l =>
            l.SpiritId != CurrentLife.SpiritId && l.Farewell == FarewellStep.Independent && !l.Story.ReunionClaimed);

        public void EnableForest() => MutateLife(() => {
            _saveData.Forest.Enabled = true;
            CurrentLife.Story.Enabled = true;
        });
        public void SetReducedMotion(bool reduced) => MutateLife(() => _saveData.Forest.ReduceMotion = reduced);

        public string AddForestTodo(string title, UserTaskCategory category, bool habit)
        {
            string id = Guid.NewGuid().ToString("N");
            MutateLife(() => {
                RequireForestToday();
                string value = (title ?? "").Trim();
                if (value.Length == 0 || value.Length > 80 || value.Any(char.IsControl))
                    throw new ArgumentException("할 일은 줄바꿈 없이 1~80자로 적어 주세요.");
                if (!Enum.IsDefined(typeof(UserTaskCategory), category)) throw new ArgumentException("카테고리를 선택해 주세요.");
                if (TodayTodos.Count >= 50) throw new InvalidOperationException("오늘의 할 일은 50개까지 적을 수 있어요.");
                _saveData.Forest.Todos.Add(new ForestTodo { Id = id, Title = value, Category = category, Habit = habit, CreatedOn = CurrentDateKey });
                CaptureForestDay();
            });
            return id;
        }
        public void ToggleForestTodo(string id) => MutateLife(() => {
            RequireForestToday();
            var todo = TodayTodos.FirstOrDefault(t => t.Id == id);
            if (todo == null) throw new ArgumentException("오늘의 할 일을 찾지 못했어요.");
            if (!todo.CompletedDates.Remove(CurrentDateKey)) todo.CompletedDates.Add(CurrentDateKey);
            RebuildForestRecord();
        });
        public void EditForestTodo(string id, string title, UserTaskCategory category, bool habit) => MutateLife(() => {
            RequireForestToday();
            string value = (title ?? "").Trim();
            if (value.Length == 0 || value.Length > 80 || (title != null && title.Any(char.IsControl)))
                throw new ArgumentException("할 일은 줄바꿈 없이 1~80자로 적어 주세요.");
            if (!Enum.IsDefined(typeof(UserTaskCategory), category)) throw new ArgumentException("카테고리를 선택해 주세요.");
            var todo = TodayTodos.FirstOrDefault(t => t.Id == id);
            if (string.IsNullOrWhiteSpace(id) || todo == null) throw new ArgumentException("오늘의 할 일을 찾지 못했어요.");

            // A recurring task changed to a one-day task still belongs to today. Saved records
            // retain their own title/category/routine snapshot, including previous completions.
            if (todo.Habit && !habit) todo.CreatedOn = CurrentDateKey;
            todo.Title = value;
            todo.Category = category;
            todo.Habit = habit;
            RebuildForestRecord();
        });
        public void ArchiveForestTodo(string id) => MutateLife(() => {
            RequireForestToday();
            var todo = TodayTodos.FirstOrDefault(t => t.Id == id);
            if (todo == null) throw new ArgumentException("오늘의 할 일을 찾지 못했어요.");
            todo.Archived = true;
            todo.CompletedDates.Remove(CurrentDateKey);
            RebuildForestRecord();
        });
        private void RequireForestToday()
        {
            if (IsHostConnected) throw new InvalidOperationException("연결된 TODO 앱에서 일정을 수정해 주세요.");
            if (!_saveData.Forest.Enabled) throw new InvalidOperationException("숲 일기를 먼저 시작해 주세요.");
            if (CurrentLife.FinalizedDates.Contains(CurrentDateKey)) throw new InvalidOperationException("이미 마무리한 하루예요.");
            if (CurrentLife.Farewell == FarewellStep.Independent) throw new InvalidOperationException("다음 정령을 먼저 만나 주세요.");
        }
        private IReadOnlyList<CompletedTask> CompletionInput(string date)
        {
            if (!_saveData.Forest.Enabled) return _todoCompletionSource.GetCompletedTasks(date);
            if (!HasActiveForestCompanion) return Array.Empty<CompletedTask>();
            return AllForestCompletions(date).Where(t => ForestOwner(date, t.TaskId)?.SpiritId == CurrentLife.SpiritId).ToList();
        }
        private void RebuildForestRecord()
        {
            AssignForestCompletionOwnership(CurrentDateKey);
            var all = AllForestCompletions(CurrentDateKey);
            UpdateForestRecordFacts(CurrentDateKey, all);
            if (HasActiveForestCompanion && !CurrentLife.FinalizedDates.Contains(CurrentDateKey)) {
                var replacement = BuildRecord(_saveData, CurrentSpiritState, CurrentDateKey, CompletionInput(CurrentDateKey));
                _saveData.Records.RemoveAll(r => r.Date == CurrentDateKey && r.SpiritId == CurrentLife.SpiritId);
                _saveData.Records.Add(replacement);
                _currentRecord = replacement;
                RefreshForestStoryFamily();
            } else {
                // While awaiting a new companion, only validated account facts and daily rewards advance.
                EssenceWallet.ApplyCompletedTaskCount(CurrentDateKey, all.Count);
                _currentRecord = FindRecord(CurrentDateKey, CurrentSpiritState.SpiritId);
            }
            CaptureForestDay();
        }
        public string ChooseForestStory(int option)
        {
            string result = "";
            MutateLife(() => {
                ForestStoryRules.Choose(CurrentLife, ForestActiveDays, option, CurrentDateKey);
                result = CurrentLife.Story.Choices.Last().Caption;
                CaptureForestDay();
            });
            return result;
        }
        public string ClaimForestReunion()
        {
            string letter = "";
            MutateLife(() => {
                var guest = ReunionGuest;
                if (guest == null) throw new InvalidOperationException("지금 도착한 편지는 없어요.");
                bool shared = guest.Story.Choices.Count > 1 && guest.Story.Choices[1].Option == 0;
                string[] gifts = { "숲의 이야기를 담은 책갈피", "새로운 숲길 지도", "꽃잎으로 만든 풍경" };
                guest.Story.ReunionGift = gifts[(int)guest.Route];
                letter = "나야, " + guest.DisplayName + ". " + (shared ? "그때 이웃과 나눈 이야기가 새 친구를 사귀는 용기가 됐어. " : "그때 함께 만든 표식을 기억하며 내 첫 작품을 만들었어. ") +
                    "네가 응원해 준 " + CompanionLifeRules.RouteName(guest.Route) + "로 지내고 있어. " + guest.Story.ReunionGift + "을 가져왔어. 새 친구에게도 보여 줘!";
                guest.Story.ReunionLetter = letter;
                guest.Story.ReunionDate = CurrentDateKey;
                guest.Story.ReunionClaimed = true;
                guest.Letters.Add(letter);
                CaptureForestDay();
            });
            return letter;
        }
        public string ForestDayReflection()
        {
            var tasks = CurrentCompletedTasks;
            if (tasks.Count == 0) return "오늘은 어떤 하루를 보내고 싶나요?\n작은 할 일 하나부터 함께해요.";
            var known = tasks.Where(t => t.UserCategory != UserTaskCategory.Unspecified).GroupBy(t => t.UserCategory)
                .OrderByDescending(g => g.Count()).ThenBy(g => (int)g.Key).FirstOrDefault();
            if (known == null) return $"함께 {tasks.Count}개의 일을 마쳤어요.\n미정의 기록도 우리 성장에 남아요.";
            return ForestStoryRules.CategoryName(known.Key) + "의 흔적이 숲에 남았어요.\n" + known.Last().Title + " · 함께 해냈어요!";
        }
        public string MonthlyTrace(string month, string spiritId)
        {
            var records = _saveData.Records.Where(r => r.SpiritId == spiritId && r.Date.StartsWith(month, StringComparison.Ordinal)).ToList();
            var groups = records.SelectMany(r => r.CompletedTasks).GroupBy(t => t.UserCategory).OrderByDescending(g => g.Count()).ThenBy(g => (int)g.Key);
            var parts = groups.Select(g => ForestStoryRules.CategoryName(g.Key) + " " + g.Count() + "회").ToArray();
            int days = records.Count(r => r.CompletedTasks.Count > 0);
            return days + "일의 생활 · " + (parts.Length == 0 ? "아직 첫 기록을 기다려요" : string.Join(" / ", parts));
        }
    }
}
