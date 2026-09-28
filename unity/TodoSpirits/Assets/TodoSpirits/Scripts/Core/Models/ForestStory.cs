using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    public enum ForestStoryFamily { Legacy, Focus, Movement, Everyday }
    [Serializable]
    public sealed class ForestStory
    {
        public bool Enabled;
        public ForestStoryFamily Family;
        public List<string> FamilyEvidence = new List<string>();
        public List<ForestChoice> Choices = new List<ForestChoice>();
        public bool ReunionClaimed;
        public string ReunionDate;
        public string ReunionGift;
        public string ReunionLetter;
        public void EnsureCollections()
        {
            if (Choices == null) Choices = new List<ForestChoice>();
            if (FamilyEvidence == null) FamilyEvidence = new List<string>();
        }
    }

    [Serializable]
    public sealed class ForestChoice
    {
        public int Episode;
        public int Option;
        public string Date;
        public string Caption;
    }

    public static class ForestStoryRules
    {
        // Prototype tuning: around 4–6 weeks at 4–5 active days/week, with no missed-day penalty.
        public const int IndependenceActivityDays = 21;
        public static readonly int[] UnlockDays = { 1, 4, 7 };
        public static readonly string[] RouteProps = { "나뭇잎 관찰 수첩", "숲길 탐험 가방", "꽃잎 바람개비" };
        public static string Question(CompanionLife life)
        {
            if (life.Story.Family != ForestStoryFamily.Legacy) return FamilyQuestion(life);
            int episode = life.Story.Choices.Count;
            if (episode == 0) return "바람에 낯선 잎이 날아왔어.\n어떻게 기억하면 좋을까?";
            bool drew = life.Story.Choices[0].Option == 0;
            if (episode == 1) return drew ? "수첩 속 잎과 같은 나무를 찾았어!\n이 발견을 누구와 나눌까?" : "그 잎이 자란 나무를 찾았어!\n돌아가는 길에 무엇을 남길까?";
            return "너와 함께한 날들이 내 꿈이 되었어.\n앞으로 어떤 일을 해 보면 좋을까?";
        }
        public static string[] Options(CompanionLife life)
        {
            if (life.Story.Family != ForestStoryFamily.Legacy) return FamilyOptions(life);
            switch (life.Story.Choices.Count)
            {
                case 0: return new[] { "그림으로 남겨 보자", "직접 찾아보자" };
                case 1: return new[] { "이웃에게 이야기하자", "우리만의 표식을 만들자" };
                default: return new[] { "이야기를 모으는 기록가", "새 길을 찾는 탐방가", "추억을 만드는 공예가" };
            }
        }
        public static string Result(int episode, int option)
        {
            if (episode == 0) return option == 0 ? "잎을 그린 첫 수첩이 생겼어요. 다음 발견을 이곳에 이어 그릴 거예요." : "작은 가방을 챙겼어요. 다음 숲길에서 이 잎의 나무를 찾아볼 거예요.";
            if (episode == 1) return option == 0 ? "이웃에게 발견을 들려줬어요. 독립 후에도 이야기를 전하러 올 거예요." : "함께 만든 표식을 나무에 걸었어요. 다시 만날 때의 약속이 되었어요.";
            return RouteProps[option] + "에 첫 꿈을 담았어요. 생활의 흔적은 그대로, 앞으로 표현할 특기가 정해졌어요.";
        }
        public static bool CanChoose(CompanionLife life, int activeDays)
        {
            life.EnsureCollections();
            int episode = life.Story.Choices.Count;
            return life.Story.Enabled && life.Farewell == FarewellStep.Growing && episode < 3 && activeDays >= UnlockDays[episode];
        }
        public static void Choose(CompanionLife life, int activeDays, int option, string date)
        {
            if (!CanChoose(life, activeDays)) throw new InvalidOperationException("다음 이야기는 함께한 날이 조금 더 쌓이면 열려요.");
            int episode = life.Story.Choices.Count;
            if (option < 0 || option >= Options(life).Length) throw new ArgumentOutOfRangeException(nameof(option));
            string caption = life.Story.Family == ForestStoryFamily.Legacy ? Result(episode, option) : FamilyResult(life, episode, option);
            life.Story.Choices.Add(new ForestChoice { Episode = episode, Option = option, Date = date, Caption = caption });
            if (episode == 2) life.Route = (AdultRoute)option;
            UpdateStage(life);
        }
        public static void UpdateStage(CompanionLife life)
        {
            if (!life.Story.Enabled || life.Farewell != FarewellStep.Growing) return;
            int days = life.ActivityDays;
            life.Stage = days < 2 ? CompanionStage.Meeting : days < 4 ? CompanionStage.Adapting :
                days < 7 ? CompanionStage.Interests : days < 10 ? CompanionStage.OwnWay : CompanionStage.Preparing;
            if (days < IndependenceActivityDays || life.Story.Choices.Count < 3) return;
            life.Stage = CompanionStage.Adult;
            // Growing also means continuing adult companionship. Farewell starts only by an explicit choice.
            life.RouteReasons.Clear();
            life.RouteReasons.Add($"실제로 할 일을 함께 마친 {days}일이 쌓였어요.");
            life.RouteReasons.Add("함께 고른 세 번의 선택을 이어 " + CompanionLifeRules.RouteName(life.Route) + "의 꿈을 찾았어요.");
            LifeActionCount strongest = null;
            foreach (var action in life.Actions)
                if (action.Count > 0 && (strongest == null || action.Count > strongest.Count)) strongest = action;
            life.RouteReasons.Add(strongest != null
                ? "완료 기록이 있는 " + strongest.Count + "활동일에 정령이 ‘" + SpiritActionCatalog.Get(strongest.Action).DisplayName + "’을 이어갔어요."
                : "함께 선택한 결과가 ‘" + VisiblePropName(life) + "’로 남았어요. 미정의 분야를 추측하지 않았어요.");
        }
        private static string FamilyQuestion(CompanionLife life)
        {
            int episode = life.Story.Choices.Count;
            bool firstA = episode > 0 && life.Story.Choices[0].Option == 0;
            bool shared = episode > 1 && life.Story.Choices[1].Option == 0;
            if (life.Story.Family == ForestStoryFamily.Focus) {
                if (episode == 0) return "책상에서 작은 잎 무늬를 발견했어.\n관찰한 것을 어떻게 이어 볼까?";
                if (episode == 1) return firstA ? "수첩에 그린 무늬와 닮은 잎을 찾았어.\n이 관찰을 누구에게 남길까?" : "탐험 가방에 담아 온 잎을 책상에 펼쳤어.\n발견을 어떻게 기억할까?";
                return shared ? "이웃에게 보낸 관찰 편지에 답장이 왔어.\n이 작은 모음을 어떤 작품으로 완성할까?" : "우리만의 표식 옆에 관찰이 쌓였어.\n이 작은 모음을 어떤 작품으로 완성할까?";
            }
            if (life.Story.Family == ForestStoryFamily.Movement) {
                if (episode == 0) return "숲길에 바람이 쉬어 가는 공터를 찾았어.\n다음에도 찾아오려면 무엇을 만들까?";
                if (episode == 1) return firstA ? "우리가 그린 지도를 따라 공터에 왔어.\n이 길을 어떻게 나누면 좋을까?" : "우리가 세운 길 표식이 공터를 알려 줬어.\n이 장소를 어떻게 남겨 둘까?";
                return shared ? "이웃도 우리의 산책길을 좋아했어.\n함께 걸은 길을 어떤 모습으로 완성할까?" : "우리만 아는 공터에 작은 약속이 쌓였어.\n함께 걸은 길을 어떤 모습으로 완성할까?";
            }
            if (episode == 0) return "평범한 하루에도 작은 숲의 조각이 모였어.\n우리 자리에 무엇을 만들어 둘까?";
            if (episode == 1) return firstA ? "함께 만든 작은 등불이 자리를 밝혀 줘.\n이 따뜻한 풍경을 어떻게 이어 갈까?" : "함께 매단 바람 장식이 살랑거려.\n이 작은 소리를 어떻게 이어 갈까?";
            return shared ? "이웃과 나눈 작은 풍경이 기억에 남았어.\n우리의 하루를 어떤 모습으로 모을까?" : "우리만의 자리에 추억이 차곡차곡 쌓였어.\n우리의 하루를 어떤 모습으로 모을까?";
        }
        private static string[] FamilyOptions(CompanionLife life)
        {
            int episode = life.Story.Choices.Count;
            if (episode == 0) {
                switch (life.Story.Family) {
                    case ForestStoryFamily.Focus: return new[] { "관찰 수첩에 그리기", "가방을 챙겨 찾아가기" };
                    case ForestStoryFamily.Movement: return new[] { "산책 지도 그리기", "작은 길 표식 세우기" };
                    default: return new[] { "작은 등불 만들기", "바람 장식 매달기" };
                }
            }
            if (episode == 1) return new[] { "이웃에게 초대 편지 보내기", "우리만의 기억 표식 남기기" };
            return new[] { "작은 이야기 책으로 모으기", "다시 찾아갈 지도로 모으기", "손으로 만든 기념물로 모으기" };
        }
        private static string FamilyResult(CompanionLife life, int episode, int option)
        {
            if (episode == 0) {
                switch (life.Story.Family) {
                    case ForestStoryFamily.Focus: return option == 0 ? "관찰 수첩을 책상에 펼쳤어요. 다음 발견을 이어 그릴 거예요." : "관찰 가방을 챙겼어요. 다음 숲길에서 닮은 잎을 찾아볼 거예요.";
                    case ForestStoryFamily.Movement: return option == 0 ? "공터까지의 지도를 그렸어요. 다음 산책도 이 지도를 펼쳐 볼 거예요." : "숲길에 작은 표식을 세웠어요. 다음에는 표식 옆의 풍경을 살펴볼 거예요.";
                    default: return option == 0 ? "우리 자리에 작은 등불을 놓았어요. 다음 방문에도 따뜻한 빛이 반겨 줄 거예요." : "우리 자리에 바람 장식을 매달았어요. 다음 방문에도 바람과 함께 움직일 거예요.";
                }
            }
            if (episode == 1) return option == 0 ? "이웃에게 초대 편지를 보냈어요. 편지가 티테이블에 남고, 다음 이야기는 답장을 기다려요." : "우리만의 기억 표식을 남겼어요. 다음 이야기에서 이 약속을 다시 꺼내 볼 거예요.";
            string[] endings = { "작은 이야기 책", "우리 숲의 지도", "손으로 만든 기념물" };
            return endings[option] + "이 완성됐어요. 함께 고른 방법과 오늘의 기록이 앨범에 남아요.";
        }
        public static string VisiblePropName(CompanionLife life)
        {
            int count = life.Story.Choices.Count;
            if (count == 0) return "";
            var latest = life.Story.Choices[count - 1];
            if (latest.Episode == 2) return new[] { "작은 이야기 책", "우리 숲의 지도", "손으로 만든 기념물" }[latest.Option];
            if (latest.Episode == 1) return latest.Option == 0 ? "이웃에게 보낸 초대 편지" : "우리만의 기억 표식";
            switch (life.Story.Family) {
                case ForestStoryFamily.Focus: return latest.Option == 0 ? "관찰 수첩" : "관찰 가방";
                case ForestStoryFamily.Movement: return latest.Option == 0 ? "공터 산책 지도" : "작은 길 표식";
                case ForestStoryFamily.Everyday: return latest.Option == 0 ? "따뜻한 등불" : "바람 장식";
                default: return latest.Option == 0 ? "잎을 그린 수첩" : "작은 탐험 가방";
            }
        }
        public static string ContinuingDialogue(CompanionLife life)
        {
            if (life.Story.Choices.Count == 0) return "";
            var latest = life.Story.Choices[life.Story.Choices.Count - 1];
            if (latest.Episode == 2) return "함께 만든 " + VisiblePropName(life) + ", 오늘도 곁에 두었어. 다음 기억도 함께 만들자.";
            if (latest.Episode == 1) return latest.Option == 0 ? "함께 보낸 초대 편지에 어떤 답장이 올까? 티테이블에 자리를 남겨 뒀어." : "우리만의 기억 표식을 오늘도 살펴봤어. 다음 이야기도 여기에 이어 두자.";
            return "우리가 고른 " + VisiblePropName(life) + "을 오늘도 챙겼어. 다음 발견에 함께 써 보자.";
        }
        public static string CategoryName(UserTaskCategory category)
        {
            string[] names = { "미정", "일·공부", "건강", "생활", "관계", "성장", "취미", "휴식·마음", "자산", "경제" };
            return Enum.IsDefined(typeof(UserTaskCategory), category) ? names[(int)category] : "미정";
        }
    }
}
