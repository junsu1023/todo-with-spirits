using System;
using System.Collections.Generic;
using System.Globalization;

namespace TodoSpirits.Core
{
    /// <summary>Prototype tuning, not a finalized design contract. Fast cycle: 28 active days, fallback: 42.</summary>
    public static class CompanionLifeRules
    {
        private static readonly int[] MinimumDays = { 4, 5, 6, 7, 6 };
        private static readonly int[] MaximumDays = { 6, 8, 8, 10, 10 };
        private static readonly string[] StageTexts = {
            "새로운 집에 적응하고 있어요.", "집과 숲이 익숙해지고 있어요.",
            "좋아하는 일이 조금씩 생기는 것 같아요.", "요즘 스스로 이어가는 일이 있어요.",
            "자기 길을 찾기 시작한 것 같아요.", "이제 자기만의 길을 걸을 준비가 되었어요."
        };
        private static readonly string[] RouteNames = { "기록가", "탐방가", "공예가" };
        public static string StageText(CompanionLife life) => StageTexts[(int)life.Stage];
        public static string RouteName(AdultRoute route) => RouteNames[(int)route];

        public static CompanionLife CreateFirst(string spiritId, string date)
        {
            ValidateDate(date);
            if (string.IsNullOrWhiteSpace(spiritId)) throw new ArgumentException("정령 ID가 필요합니다.");
            return new CompanionLife {
                SpiritId = spiritId, StartedOn = date,
                Temperaments = new List<SpiritTemperament> { SpiritTemperament.Curious, SpiritTemperament.Meticulous }
            };
        }

        public static CompanionLife CreateFromEgg(string spiritId, string date, int egg)
        {
            if (egg < 0 || egg > 2) throw new ArgumentOutOfRangeException(nameof(egg));
            var life = CreateFirst(spiritId, date);
            // Weighted pool: favored traits occur more often, but are never disclosed as guaranteed results.
            SpiritTemperament[][] pools = {
                new[] { SpiritTemperament.Active, SpiritTemperament.Active, SpiritTemperament.Playful, SpiritTemperament.Independent, SpiritTemperament.Curious },
                new[] { SpiritTemperament.Relaxed, SpiritTemperament.Cautious, SpiritTemperament.Relaxed, SpiritTemperament.Meticulous, SpiritTemperament.Independent },
                new[] { SpiritTemperament.Curious, SpiritTemperament.Sociable, SpiritTemperament.Curious, SpiritTemperament.Playful, SpiritTemperament.Active }
            };
            uint hash = StableHash.Fnv1A(spiritId + ":egg:" + egg);
            var random = new Random(unchecked((int)hash));
            int count = 2 + random.Next(2);
            life.Temperaments.Clear();
            while (life.Temperaments.Count < count)
            {
                var trait = pools[egg][random.Next(pools[egg].Length)];
                if (!life.Temperaments.Contains(trait)) life.Temperaments.Add(trait);
            }
            return life;
        }

        public static bool FinalizeDay(CompanionLife life, DailyCompanionRecord record)
        {
            if (life == null || record == null) throw new ArgumentNullException();
            life.EnsureCollections();
            ValidateDate(record.Date);
            if (record.SpiritId != life.SpiritId) throw new InvalidOperationException("다른 정령 기록은 반영할 수 없습니다.");
            if (life.FinalizedDates.Contains(record.Date) || life.Farewell == FarewellStep.Independent) return false;
            if (string.CompareOrdinal(record.Date, life.StartedOn) < 0 ||
                (life.FinalizedDates.Count > 0 && string.CompareOrdinal(record.Date, life.FinalizedDates[life.FinalizedDates.Count - 1]) < 0))
                throw new InvalidOperationException("하루 기록은 만남 이후 날짜순으로 확정해야 합니다.");
            if (record.SpiritDay == null || !Enum.IsDefined(typeof(SpiritActionId), record.SpiritDay.PrimaryAction))
                throw new ArgumentException("유효한 정령 행동이 필요합니다.");

            life.FinalizedDates.Add(record.Date);
            if (record.CompletedTasks == null || record.CompletedTasks.Count == 0) return true;
            life.ActivityDays++;
            var action = record.SpiritDay.PrimaryAction;
            life.CurrentActionStreak = life.ActivityDays > 1 && action == life.LastAction ? life.CurrentActionStreak + 1 : 1;
            life.LastAction = action;
            var entry = life.Actions.Find(item => item.Action == action);
            if (entry == null) { entry = new LifeActionCount { Action = action }; life.Actions.Add(entry); }
            entry.Count++;
            entry.LongestStreak = Math.Max(entry.LongestStreak, life.CurrentActionStreak);
            ContinueProject(life, record);
            TryAdvance(life);
            return true;
        }

        private static void ContinueProject(CompanionLife life, DailyCompanionRecord record)
        {
            if (life.Stage < CompanionStage.OwnWay || life.Stage == CompanionStage.Adult) return;
            var project = life.ActiveProject;
            if (project == null)
            {
                LifeActionCount preferred = null;
                foreach (var count in life.Actions)
                    if (count.Count >= 3 && !life.Projects.Exists(p => p.Action == count.Action) &&
                        (preferred == null || count.Count > preferred.Count ||
                            (count.Count == preferred.Count && count.Action < preferred.Action))) preferred = count;
                if (preferred == null) return;
                string[] titles = { "오래된 책 정리하기", "작은 나무 장식 만들기", "숲길 지도 만들기", "이웃에게 보낼 편지 모으기", "포근한 쉼터 가꾸기" };
                project = new CompanionProject { Action = preferred.Action, Title = titles[(int)preferred.Action], StartedOn = record.Date };
                life.Projects.Add(project);
            }
            if (record.SpiritDay.PrimaryAction != project.Action) return;
            project.WorkDays++;
            if (project.WorkDays >= 3) project.CompletedOn = record.Date;
            record.LifeActivityText = project.IsComplete ? project.Title + "를 마무리했어요." : project.Title + "를 조금 더 이어갔어요.";
        }

        public static void ObserveStageEvent(CompanionLife life)
        {
            life.StageEventSeen = true;
            // May release a transition waiting on the stage event, without inventing a new activity day.
            TryAdvance(life);
        }

        private static void TryAdvance(CompanionLife life)
        {
            if (life.Stage == CompanionStage.Adult) return;
            int index = (int)life.Stage;
            int elapsed = life.ActivityDays - life.StageStartedAtActivityDay;
            bool pattern = life.Actions.Exists(item => item.Count >= 3);
            bool normal = elapsed >= MinimumDays[index] && life.StageEventSeen &&
                (index == 0 || life.Actions.Count >= 2) && (index < 2 || pattern);
            if (!normal && elapsed < MaximumDays[index]) return;
            life.Stage++;
            life.StageStartedAtActivityDay = life.ActivityDays;
            life.StageEventSeen = false;
            if (life.Stage == CompanionStage.Adult)
            {
                ResolveRoute(life);
                life.Farewell = FarewellStep.AdultReveal;
            }
        }

        public static AdultRoute PreviewRoute(CompanionLife life)
        {
            // Repeated actual actions dominate; continued activities and traits are subordinate evidence.
            var scores = new int[3];
            foreach (var action in life.Actions)
            {
                int route = action.Action == SpiritActionId.WalkForest ? 1 : action.Action == SpiritActionId.CraftRepair ? 2 : 0;
                scores[route] += action.Count * 100 + action.LongestStreak * 5;
            }
            foreach (var trait in life.Temperaments)
            {
                int route = trait == SpiritTemperament.Active || trait == SpiritTemperament.Curious ? 1 :
                    trait == SpiritTemperament.Meticulous || trait == SpiritTemperament.Playful ? 2 : 0;
                scores[route]++;
            }
            foreach (var project in life.Projects)
            {
                int route = project.Action == SpiritActionId.WalkForest ? 1 : project.Action == SpiritActionId.CraftRepair ? 2 : 0;
                scores[route] += project.WorkDays * 3;
            }
            int winner = 0;
            for (int i = 1; i < scores.Length; i++) if (scores[i] > scores[winner]) winner = i;
            return (AdultRoute)winner;
        }

        private static void ResolveRoute(CompanionLife life)
        {
            life.Route = PreviewRoute(life);
            var strongest = CompanionStagePresentation.StrongestPattern(life);
            life.RouteReasons.Clear();
            life.RouteReasons.Add($"함께 활동한 {life.ActivityDays}일의 생활 기록을 돌아봤어요.");
            if (strongest != null)
            {
                life.RouteReasons.Add($"'{SpiritActionCatalog.Get(strongest.Action).DisplayName}'을 {strongest.Count}번 선택했어요.");
                var completed = life.Projects.Find(project => project.IsComplete);
                life.RouteReasons.Add(completed == null ? $"그 활동을 가장 길게 {strongest.LongestStreak}활동일 동안 이어갔어요." : completed.Title + "를 스스로 마무리했어요.");
            }
        }

        public static void AdvanceFarewell(CompanionLife life)
        {
            if (life.Farewell == FarewellStep.AdultReveal) life.Farewell = FarewellStep.Reflection;
            else if (life.Farewell == FarewellStep.Reflection) life.Farewell = FarewellStep.Naming;
            else throw new InvalidOperationException("현재 단계에서는 회고를 진행할 수 없습니다.");
        }

        public static void Name(CompanionLife life, string name)
        {
            if (life.Farewell != FarewellStep.Naming) throw new InvalidOperationException("회고 후 이름을 지어 주세요.");
            string value = (name ?? string.Empty).Trim();
            int length = new StringInfo(value).LengthInTextElements;
            if (length < 1 || length > 12) throw new ArgumentException("이름은 1~12자로 입력해 주세요.");
            foreach (char c in value) if (char.IsControl(c) || c == '<' || c == '>')
                throw new ArgumentException("이름에는 줄바꿈이나 꺾쇠를 넣을 수 없습니다.");
            life.Name = value;
            life.Farewell = FarewellStep.LastCompanionship;
        }

        public static void ObserveNamedCompanion(CompanionLife life)
        {
            if (life.Farewell == FarewellStep.LastCompanionship) life.Farewell = FarewellStep.ReadyToLeave;
        }

        public static void Leave(CompanionLife life, string date)
        {
            ValidateDate(date);
            if (life.Farewell != FarewellStep.ReadyToLeave) throw new InvalidOperationException("이름을 가진 마지막 동행을 먼저 만나 주세요.");
            if (string.CompareOrdinal(date, life.StartedOn) < 0 ||
                (life.FinalizedDates.Count > 0 && string.CompareOrdinal(date, life.FinalizedDates[life.FinalizedDates.Count - 1]) < 0))
                throw new ArgumentException("마지막 활동보다 앞선 날짜에 독립할 수 없습니다.");
            life.IndependentOn = date;
            life.Farewell = FarewellStep.Independent;
        }

        private static void ValidateDate(string date)
        {
            if (!DateTime.TryParseExact(date, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out _))
                throw new ArgumentException("날짜는 yyyy-MM-dd 형식이어야 합니다.");
        }
    }
}
