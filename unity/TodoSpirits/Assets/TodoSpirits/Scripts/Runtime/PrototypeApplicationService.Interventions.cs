using System;
using System.Globalization;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    public sealed partial class PrototypeApplicationService
    {
        public CompanionInterventions Interventions => _saveData.Interventions;
        public CompanionTrip PendingTrip => Interventions.Trips.Find(trip => !trip.Claimed);

        private System.Collections.Generic.IEnumerable<SpiritActionId> GetGiftActionCandidates(string date, string spiritId)
        {
            foreach (var gift in Interventions.Gifts)
                if (gift.SpiritId == spiritId && string.CompareOrdinal(gift.Date, date) < 0 &&
                    string.CompareOrdinal(date, gift.ExpiresAfterDate) <= 0)
                    yield return CompanionItemCatalog.GiftActions[gift.GiftId];
        }

        public string GiveGift(int giftId)
        {
            string reaction = string.Empty;
            MutateLife(() => {
                ValidateIndex(giftId, CompanionItemCatalog.Gifts.Length);
                RequirePresentCompanion();
                if (PendingTrip != null) throw new InvalidOperationException("여행에서 돌아오면 선물을 전해 주세요.");
                if (Interventions.Gifts.Exists(gift => gift.SpiritId == CurrentLife.SpiritId && gift.Date == CurrentDateKey))
                    throw new InvalidOperationException("오늘 전한 선물을 살펴보고 있어요. 다음 날 다시 전해 주세요.");
                SpendEssence(CompanionItemCatalog.GiftPrice);
                reaction = CompanionTemperamentText.GiftReaction(CurrentLife.Temperaments, CurrentLife.SpiritId + CurrentDateKey + giftId);
                Interventions.Gifts.Add(new CompanionGift {
                    SpiritId = CurrentLife.SpiritId, Date = CurrentDateKey, GiftId = giftId, Reaction = reaction,
                    ExpiresAfterDate = ToDateKey(CurrentDate.AddDays(3))
                });
            });
            return reaction;
        }

        public void StartTrip(int preparation, DateTime utcNow)
        {
            RequireUtc(utcNow);
            MutateLife(() => {
                ValidateIndex(preparation, 3);
                RequirePresentCompanion();
                if (PendingTrip != null) throw new InvalidOperationException("이미 진행 중인 여행이 있어요.");
                SpendEssence(CompanionItemCatalog.TravelPrice);
                var trip = new CompanionTrip {
                    TripId = Guid.NewGuid().ToString("N"),
                    SpiritId = CurrentLife.SpiritId, StartedOn = CurrentDateKey, Preparation = preparation,
                    ReturnAtUtc = utcNow.AddHours(4).ToString("O", CultureInfo.InvariantCulture)
                };
                CompanionTravelStories.Resolve(trip);
                Interventions.Trips.Add(trip);
            });
        }

        public bool CanClaimTrip(DateTime utcNow)
        {
            RequireUtc(utcNow);
            return PendingTrip != null && utcNow >= DateTime.Parse(PendingTrip.ReturnAtUtc, CultureInfo.InvariantCulture, DateTimeStyles.RoundtripKind);
        }

        public CompanionTrip ClaimTrip(DateTime utcNow)
        {
            RequireUtc(utcNow);
            CompanionTrip result = null;
            MutateLife(() => {
                if (!CanClaimTrip(utcNow)) throw new InvalidOperationException("아직 여행 중이에요.");
                result = PendingTrip;
                CompanionTravelStories.Resolve(result);
                result.Claimed = true;
            });
            return result;
        }

        public void FinishTripForDebug()
        {
            if (!UnityEngine.Application.isEditor && !UnityEngine.Debug.isDebugBuild)
                throw new InvalidOperationException("개발 빌드에서만 여행 시간을 가속할 수 있습니다.");
            MutateLife(() => {
                if (PendingTrip == null) throw new InvalidOperationException("진행 중인 여행이 없습니다.");
                CompanionTravelStories.Resolve(PendingTrip);
                PendingTrip.ReturnAtUtc = DateTime.UtcNow.ToString("O", CultureInfo.InvariantCulture);
            });
        }

        public void BuyDecoration(int decoration)
        {
            MutateLife(() => {
                ValidateIndex(decoration, CompanionItemCatalog.Decorations.Length);
                string id = decoration.ToString(CultureInfo.InvariantCulture);
                if (Interventions.OwnedDecorations.Contains(id)) throw new InvalidOperationException("이미 가지고 있는 장식이에요.");
                SpendEssence(CompanionItemCatalog.DecorationPrice);
                Interventions.OwnedDecorations.Add(id);
            });
        }

        public void EquipDecoration(int decoration, bool equipped)
        {
            MutateLife(() => {
                ValidateIndex(decoration, CompanionItemCatalog.Decorations.Length);
                string id = decoration.ToString(CultureInfo.InvariantCulture);
                if (!Interventions.OwnedDecorations.Contains(id)) throw new InvalidOperationException("먼저 장식을 구매해 주세요.");
                Interventions.EquippedDecorations.Remove(id);
                if (equipped) Interventions.EquippedDecorations.Add(id);
            });
        }

        private void SpendEssence(int price)
        {
            if (EssenceWallet.Balance < price) throw new InvalidOperationException("정수가 부족해요.");
            EssenceWallet.Balance -= price;
        }
        private void RequirePresentCompanion()
        {
            if (CurrentLife.Farewell == FarewellStep.Independent) throw new InvalidOperationException("다음 정령을 먼저 만나 주세요.");
        }
        private static void ValidateIndex(int value, int count)
        {
            if (value < 0 || value >= count) throw new ArgumentOutOfRangeException(nameof(value));
        }
        private static void RequireUtc(DateTime value)
        {
            if (value.Kind != DateTimeKind.Utc) throw new ArgumentException("여행 시각에는 UTC를 사용해야 합니다.");
        }
    }
}
