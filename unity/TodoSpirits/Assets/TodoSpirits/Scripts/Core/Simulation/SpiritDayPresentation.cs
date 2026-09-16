using System;
using System.Globalization;

namespace TodoSpirits.Core
{
    public static class SpiritDayPresentation
    {
        private static readonly string[,] Lines = {
            { "책장을 천천히 넘기며 낯선 이야기를 찾고 있어.", "읽은 내용을 작은 수첩에 옮겨 적고 있어.", "마음에 드는 페이지를 다시 펼쳐 보고 있어." },
            { "작은 도구를 차례로 늘어놓고 손보고 있어.", "나무 조각을 맞추어 새로운 모양을 만들고 있어.", "완성한 부분을 이리저리 돌려 보며 살피고 있어." },
            { "숲길의 발자국을 따라 천천히 걷고 있어.", "풀잎 사이에서 새로운 흔적을 찾고 있어.", "잠깐 멈춰 바람이 가는 방향을 살펴보고 있어." },
            { "따뜻한 차를 준비하며 이웃을 기다리고 있어.", "이웃의 이야기에 귀를 기울이며 차를 마시고 있어.", "오늘 만난 일들을 이웃에게 들려주고 있어." },
            { "포근한 자리에 기대어 잠깐 눈을 감고 있어.", "고개를 들어 느리게 흐르는 구름을 바라보고 있어.", "잔잔한 숲의 소리를 들으며 숨을 고르고 있어." }
        };

        public static void Apply(SpiritDayResult day, string spiritId)
        {
            if (day == null) throw new ArgumentNullException(nameof(day));
            var date = DateTime.ParseExact(day.Date,"yyyy-MM-dd",CultureInfo.InvariantCulture);
            int variation = (int)(((ulong)(date.Ticks / TimeSpan.TicksPerDay) + StableHash.Fnv1A(spiritId)) % 3);
            day.PresentationVariant = variation + 1;
            day.Dialogue = Lines[(int)day.PrimaryAction,variation];
        }
    }
}
