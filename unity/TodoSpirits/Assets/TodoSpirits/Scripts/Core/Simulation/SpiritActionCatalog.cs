using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    [Serializable]
    public sealed class SpiritActionDefinition
    {
        public SpiritActionId Id;
        public string DisplayName;
        public string Location;
        public string Dialogue;
        public string RecordText;

        public SpiritActionDefinition()
        {
            DisplayName = string.Empty;
            Location = string.Empty;
            Dialogue = string.Empty;
            RecordText = string.Empty;
        }

        public SpiritActionDefinition(
            SpiritActionId id,
            string displayName,
            string location,
            string dialogue,
            string recordText)
        {
            Id = id;
            DisplayName = displayName ?? string.Empty;
            Location = location ?? string.Empty;
            Dialogue = dialogue ?? string.Empty;
            RecordText = recordText ?? string.Empty;
        }
    }

    public static class SpiritActionCatalog
    {
        private static readonly List<SpiritActionDefinition> Definitions =
            new List<SpiritActionDefinition>
            {
                new SpiritActionDefinition(
                    SpiritActionId.ReadRecords,
                    "오래된 기록 읽기",
                    "책장 곁 기록 책상",
                    "책장 곁에서 오래된 기록 두루마리를 살피고 있어.",
                    "책장 곁에서 오늘의 기록을 차분히 정리했다."),
                new SpiritActionDefinition(
                    SpiritActionId.CraftRepair,
                    "기록 도구 손보기",
                    "달빛 공작대",
                    "공작대에서 기록 지도의 흐릿한 길을 고치고 있어.",
                    "달빛 공작대에서 기록 지도와 작은 도구를 손봤다."),
                new SpiritActionDefinition(
                    SpiritActionId.WalkForest,
                    "이슬 숲길 걷기",
                    "앞마당과 이슬 숲길",
                    "이슬 맺힌 숲길을 천천히 거닐고 있어.",
                    "앞마당을 지나 이슬 숲길을 천천히 걸었다."),
                new SpiritActionDefinition(
                    SpiritActionId.SocialTea,
                    "별잎차 나누기",
                    "작은 티테이블",
                    "티테이블에서 이웃 정령과 따뜻한 별잎차를 나누고 있어.",
                    "작은 티테이블에서 이웃 정령과 별잎차를 나눴다."),
                new SpiritActionDefinition(
                    SpiritActionId.Rest,
                    "포근한 쉼",
                    "이끼 침상 쉼터",
                    "이끼 침상에 기대어 조용히 숨을 고르고 있어.",
                    "이끼 침상 쉼터에서 포근히 쉬었다.")
            };

        public static IReadOnlyList<SpiritActionDefinition> All
        {
            get { return Definitions; }
        }

        public static SpiritActionDefinition Get(SpiritActionId action)
        {
            for (var i = 0; i < Definitions.Count; i++)
            {
                if (Definitions[i].Id == action)
                {
                    return Definitions[i];
                }
            }

            return Definitions[Definitions.Count - 1];
        }

        public static string ResolveDialogue(
            SpiritActionId action,
            IEnumerable<SpiritTemperament> temperaments)
        {
            if (action == SpiritActionId.Rest)
            {
                return "오늘은 이끼 침상에서 느긋하게 쉬며 내일의 빛을 모으고 있어.";
            }

            switch (action)
            {
                case SpiritActionId.ReadRecords:
                    if (ContainsTemperament(temperaments, SpiritTemperament.Curious))
                    {
                        return "새로운 기록 속에서 반짝이는 이야기를 찾고 있어.";
                    }

                    break;
                case SpiritActionId.CraftRepair:
                    if (ContainsTemperament(temperaments, SpiritTemperament.Meticulous))
                    {
                        return "흐트러진 기록 도구를 하나씩 가지런히 손보고 있어.";
                    }

                    break;
                case SpiritActionId.WalkForest:
                    if (ContainsTemperament(temperaments, SpiritTemperament.Active))
                    {
                        return "숲길의 바람과 나란히 힘차게 걷고 있어.";
                    }

                    break;
                case SpiritActionId.SocialTea:
                    if (ContainsTemperament(temperaments, SpiritTemperament.Sociable))
                    {
                        return "이웃 정령과 별잎차를 나누며 오늘 이야기를 들려주고 있어.";
                    }

                    break;
            }

            return Get(action).Dialogue;
        }

        private static bool ContainsTemperament(
            IEnumerable<SpiritTemperament> temperaments,
            SpiritTemperament target)
        {
            if (temperaments == null)
            {
                return false;
            }

            foreach (var temperament in temperaments)
            {
                if (temperament == target)
                {
                    return true;
                }
            }

            return false;
        }
    }
}
