using System.Collections.Generic;

namespace TodoSpirits.Core
{
    public static class CompanionTemperamentText
    {
        public static string Describe(SpiritTemperament trait)
        {
            switch(trait)
            {
                case SpiritTemperament.Curious: return "새로운 것을 보면 그냥 지나치지 못해요.";
                case SpiritTemperament.Relaxed: return "서두르지 않고 쉬어 가는 시간을 좋아해요.";
                case SpiritTemperament.Active: return "몸을 움직이며 주변을 살필 때 활기가 넘쳐요.";
                case SpiritTemperament.Sociable: return "누군가와 이야기를 나누면 더 생기 있어져요.";
                case SpiritTemperament.Cautious: return "익숙한 것을 소중히 여기고 한 번 더 살펴봐요.";
                case SpiritTemperament.Meticulous: return "작은 부분도 놓치지 않고 가지런히 정리해요.";
                case SpiritTemperament.Playful: return "평범한 물건에서도 재미있는 놀이를 찾아내요.";
                case SpiritTemperament.Independent: return "혼자 생각한 일을 자기 방식으로 이어가요.";
                default: return "함께 지내며 조금씩 알아가고 있어요.";
            }
        }
        public static string GiftReaction(IReadOnlyList<SpiritTemperament> traits, string key)
        {
            if(traits == null || traits.Count == 0) return "선물을 곁에 두고 천천히 살펴봐요.";
            var trait = traits[(int)(StableHash.Fnv1A(key) % (uint)traits.Count)];
            switch(trait)
            {
                case SpiritTemperament.Curious: return "선물을 이리저리 살펴보며 눈을 반짝여요.";
                case SpiritTemperament.Relaxed: return "선물을 옆에 두고 느긋하게 기대어 쉬어요.";
                case SpiritTemperament.Active: return "선물을 잠깐 살펴보고 신나게 주변을 돌아다녀요.";
                case SpiritTemperament.Sociable: return "선물을 이웃에게 보여주고 싶어 해요.";
                case SpiritTemperament.Cautious: return "선물을 조심스럽게 만져 보고 익숙한 자리에 두어요.";
                case SpiritTemperament.Meticulous: return "선물의 작은 부분까지 살피고 반듯하게 정리해요.";
                case SpiritTemperament.Playful: return "선물로 새로운 놀이를 떠올리고 즐거워해요.";
                case SpiritTemperament.Independent: return "선물을 자기만의 활동에 써 볼 방법을 생각해요.";
                default: return "선물을 소중히 받아들었어요.";
            }
        }
    }
}
