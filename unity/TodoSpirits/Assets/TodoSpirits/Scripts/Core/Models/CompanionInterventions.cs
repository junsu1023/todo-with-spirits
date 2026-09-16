using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    [Serializable]
    public sealed class CompanionInterventions
    {
        public List<CompanionGift> Gifts = new List<CompanionGift>();
        public List<CompanionTrip> Trips = new List<CompanionTrip>();
        public List<string> OwnedDecorations = new List<string>();
        public List<string> EquippedDecorations = new List<string>();
        public void EnsureCollections()
        {
            if (Gifts == null) Gifts = new List<CompanionGift>();
            if (Trips == null) Trips = new List<CompanionTrip>();
            if (OwnedDecorations == null) OwnedDecorations = new List<string>();
            if (EquippedDecorations == null) EquippedDecorations = new List<string>();
        }
    }
    [Serializable]
    public sealed class CompanionGift
    {
        public string SpiritId;
        public string Date;
        public int GiftId;
        public string Reaction;
        public string ExpiresAfterDate;
    }
    [Serializable]
    public sealed class CompanionTrip
    {
        public string TripId;
        public string SpiritId;
        public string StartedOn;
        public string ReturnAtUtc;
        public int Preparation;
        public bool Claimed;
        public string EventText;
        public string Discovery;
    }

    public static class CompanionTravelStories
    {
        public static void Resolve(CompanionTrip trip)
        {
            if (!string.IsNullOrEmpty(trip.EventText) && !string.IsNullOrEmpty(trip.Discovery)) return;
            string key = string.IsNullOrEmpty(trip.TripId) ? trip.SpiritId + ":" + trip.StartedOn + ":" + trip.Preparation : trip.TripId;
            uint hash = StableHash.Fnv1A(key);
            int theme = hash % 10 < 7 ? trip.Preparation : (trip.Preparation + 1 + (int)(hash % 2)) % 3;
            int variant = (int)((hash / 10) % 2);
            string[,] events = {
                { "숲길에서 만난 이웃과 도시락을 나누며 먼 마을의 이야기를 들었어요.", "길을 잃은 이웃에게 쉼터를 알려 주고 감사 인사를 받았어요." },
                { "작은 공터에서 반짝이는 씨앗을 찾아 소중히 챙겼어요.", "개울가에서 물결무늬가 새겨진 조약돌을 발견했어요." },
                { "오래된 숲의 표식을 관찰해 수첩에 작은 지도를 그렸어요.", "바람이 지나가는 나뭇잎의 모양을 종이에 기록했어요." }
            };
            string[,] discoveries = { { "이웃의 감사 쪽지", "작은 마을 엽서" }, { "반짝이는 씨앗", "물결무늬 조약돌" }, { "숲길 관찰 지도", "나뭇잎 관찰장" } };
            trip.EventText = events[theme,variant];
            trip.Discovery = discoveries[theme,variant];
        }
    }
    public static class CompanionItemCatalog
    {
        public const int GiftPrice = 15;
        public const int TravelPrice = 40;
        public const int DecorationPrice = 30;
        public static readonly string[] Gifts = { "작은 책", "따뜻한 담요", "그림 도구", "작은 화분", "나무 열매 차", "반짝이는 돌" };
        public static readonly SpiritActionId[] GiftActions = { SpiritActionId.ReadRecords, SpiritActionId.Rest,
            SpiritActionId.CraftRepair, SpiritActionId.CraftRepair, SpiritActionId.SocialTea, SpiritActionId.WalkForest };
        public static readonly string[] Preparations = { "작은 도시락", "빈 가방", "관찰 수첩" };
        public static readonly string[] Decorations = { "나뭇잎 책꽂이", "작은 공구함", "꽃무늬 찻잔", "새싹 화분", "포근한 쿠션" };
        public static readonly string[] Slots = { "책장", "공작대", "티테이블", "화분", "쉼터" };
    }
}
