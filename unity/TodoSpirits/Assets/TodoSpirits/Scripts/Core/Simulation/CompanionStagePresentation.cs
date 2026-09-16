using System;

namespace TodoSpirits.Core
{
    public static class CompanionStagePresentation
    {
        // First meetings use the desk, nearby yard and bed. The workshop and tea table open after adaptation.
        public static SpiritActionId PlaceAction(CompanionStage stage, SpiritActionId action) =>
            stage == CompanionStage.Meeting && (action == SpiritActionId.CraftRepair || action == SpiritActionId.SocialTea)
                ? SpiritActionId.ReadRecords : action;

        public static string PlaceName(CompanionStage stage, SpiritActionId action) =>
            SpiritActionCatalog.Get(PlaceAction(stage, action)).Location;

        public static LifeActionCount StrongestPattern(CompanionLife life)
        {
            LifeActionCount strongest = null;
            foreach (var action in life.Actions)
                if (action.Count > 0 && (strongest == null || action.Count > strongest.Count ||
                    (action.Count == strongest.Count && (int)action.Action < (int)strongest.Action))) strongest = action;
            return strongest;
        }

        public static string Hint(CompanionLife life)
        {
            if (life.Stage == CompanionStage.Meeting) return "책상과 집 앞, 포근한 쉼터부터 익히고 있어요.";
            if (life.Stage == CompanionStage.Adapting) return "공작대와 티테이블에도 발걸음을 옮기기 시작했어요.";
            var pattern = StrongestPattern(life);
            if (pattern == null || pattern.Count < 3) return "아직 여러 곳을 둘러보며 마음이 가는 일을 찾고 있어요.";
            if (life.Stage == CompanionStage.Preparing)
            {
                switch (CompanionLifeRules.PreviewRoute(life))
                {
                    case AdultRoute.Explorer: return "언젠가 더 먼 숲길을 걸어보고 싶은가 봐요. 작은 여행 가방을 챙기고 있어요.";
                    case AdultRoute.Artisan: return "자기 손으로 만든 물건을 나누고 싶은가 봐요. 앞치마 천을 골라 두었어요.";
                    default: return "숲의 이야기를 오래 남기고 싶은가 봐요. 작은 수첩을 소중히 챙기고 있어요.";
                }
            }
            return "요즘은 " + SpiritActionCatalog.Get(pattern.Action).Location + "에서 보내는 시간을 좋아하는 것 같아요.";
        }
    }
}
