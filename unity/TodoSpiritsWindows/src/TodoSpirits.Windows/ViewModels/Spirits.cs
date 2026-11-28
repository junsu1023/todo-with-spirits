using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>
/// 도감의 정령 한 칸. 정령을 만나는 규칙과 호감도는 아직 앱에 없어서,
/// 목록·호감도·처음 만난 날은 시안(unity/windows-ui/design/dex-v4.html)의 예시 값이다.
/// </summary>
public sealed class SpiritRow
{
    public string Id { get; init; } = "";
    public string Name { get; init; } = "???";
    public string Element { get; init; } = "";
    public Brush ElementBrush { get; init; } = Brushes.Transparent;
    public string Category { get; init; } = "";
    public int Affection { get; init; }
    public bool Owned { get; init; }
    public string Description { get; init; } = "";
    public string Hint { get; init; } = "";
    public string FirstMet { get; init; } = "";
    public ImageSource? Art { get; init; }
    public bool IsRepresentative { get; init; }
    public bool IsSelected { get; init; }

    public bool Unknown => !Owned;
    public bool HasElement => Element.Length > 0;
    public IReadOnlyList<bool> Hearts => Enumerable.Range(1, 5).Select(i => i <= Affection).ToList();
    public string AffectionText => $"호감도 {Affection} / 5";
    public string Tip => Owned ? $"{Name} · {Element} · 호감도 {Affection}/5{(IsRepresentative ? " · 대표 정령" : "")}" : "아직 만나지 못한 정령";
}

public static class SpiritCatalog
{
    private sealed record Def(string Id, string Name, string Element, string Color, string Category, int Affection, bool Owned,
        string Description, string Hint, string FirstMet, int SheetX = -1, int SheetY = -1);

    // 그림: 구름이는 투명 배경 원본, 나머지는 Figma 캡처(dex-sheet.png, 360×800)에서 96×96 을 잘라 쓴다.
    private static readonly Def[] Defs =
    [
        new("wind", "구름이", "바람", "#FF96DE", "자연", 5, true, "몽실몽실한 바람 정령이에요. 할 일을 하나씩 끝낼 때마다 볼이 더 발그레해져요.", "", "2026. 08. 02"),
        new("leaf", "꽃잎이", "숲", "#8BC34A", "자연", 4, true, "꽃잎 모자를 쓴 숲 정령이에요. 꾸준히 해낸 날이 쌓이면 꽃이 하나씩 피어나요.", "", "2026. 08. 20", 132, 214),
        new("fire", "불꽃이", "불", "#FF8A3D", "자연", 3, true, "열정 가득한 불 정령이에요. 중요한 일정을 끝내면 불꽃이 더 크게 타올라요.", "", "2026. 09. 03", 246, 214),
        new("water", "물순이", "물", "#6EC6F0", "자연", 2, true, "말랑말랑한 물방울 정령이에요. 차분하게 하루를 정리하는 걸 좋아해요.", "", "2026. 09. 21", 18, 370),
        new("star", "별아기", "별", "#F7C948", "우주", 1, true, "반짝이는 별 정령이에요. 늦은 밤까지 남은 일을 끝내면 살며시 빛나요.", "", "2026. 10. 05", 132, 370),
        new("q1", "???", "", "", "자연", 0, false, "", "작심삼일을 무사히 넘긴 날에 나타나요.", ""),
        new("q2", "???", "", "", "동물", 0, false, "", "일주일 동안 매일 하나씩 완료하면 만날 수 있어요.", ""),
        new("q3", "???", "", "", "동물", 0, false, "", "중요 일정을 10번 끝내면 찾아와요.", ""),
    ];

    private static readonly Dictionary<string, ImageSource?> ArtCache = [];

    private static ImageSource? ArtOf(Def d)
    {
        if (!d.Owned) return null;
        if (ArtCache.TryGetValue(d.Id, out var cached)) return cached;
        ImageSource art;
        if (d.SheetX < 0)
        {
            art = new BitmapImage(new Uri("pack://application:,,,/Assets/spirit_neutral.png"));
        }
        else
        {
            var sheet = new BitmapImage(new Uri("pack://application:,,,/Assets/dex-sheet.png"));
            art = new CroppedBitmap(sheet, new Int32Rect(d.SheetX, d.SheetY, 96, 96));
        }
        art.Freeze();
        return ArtCache[d.Id] = art;
    }

    public static bool IsOwned(string id) => Defs.Any(d => d.Id == id && d.Owned);

    public static IReadOnlyList<SpiritRow> Build(string representativeId, string selectedId) =>
        Defs.Select(d => new SpiritRow
        {
            Id = d.Id, Name = d.Name, Element = d.Element, Category = d.Category, Affection = d.Affection, Owned = d.Owned,
            Description = d.Description, Hint = d.Hint, FirstMet = d.FirstMet, Art = ArtOf(d),
            ElementBrush = d.Color.Length > 0 ? (Brush)new BrushConverter().ConvertFromString(d.Color)! : Brushes.Transparent,
            IsRepresentative = d.Id == representativeId, IsSelected = d.Id == selectedId,
        }).ToList();
}
