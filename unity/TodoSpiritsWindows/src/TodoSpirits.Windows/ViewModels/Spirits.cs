using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>
/// 도감의 정령 한 칸. 정령을 만나는 규칙과 호감도는 아직 앱에 없어서,
/// 목록·호감도·처음 만난 날·함께한 날은 시안(unity/windows-ui/design/dex-v4.html)의 예시 값이다.
/// 칸은 한 번만 만들고 선택·대표 표시만 바꾼다 (목록을 다시 만들면 초점과 움직임이 끊긴다).
/// </summary>
public sealed class SpiritRow : ObservableObject
{
    internal long? ServerId { get; init; }
    public string Id { get; init; } = "";
    private string _name = "???";
    public string Name { get => _name; set { if (Set(ref _name, value)) Raise(nameof(Tip)); } }
    private bool _important;
    public bool IsImportant { get => _important; set { if (Set(ref _important, value)) Raise(nameof(ImportantButtonText)); } }
    public string ImportantButtonText => IsImportant ? "즐겨찾기 해제" : "즐겨찾기 추가";
    public string Element { get; init; } = "";
    public Brush ElementBrush { get; init; } = Brushes.Transparent;
    public string Category { get; init; } = "";
    public int Affection { get; init; }
    public bool Owned { get; init; }
    public string Description { get; init; } = "";
    public string Hint { get; init; } = "";
    public string FirstMet { get; init; } = "";
    /// <summary>만난 순서 (1부터). 못 만난 정령은 99.</summary>
    public int Met { get; init; } = 99;
    /// <summary>함께한 날 수. 예시 값.</summary>
    public int Days { get; init; }
    public string DaysText => Days > 0 ? $"{Days}일" : "정보 없음";
    /// <summary>다음 하트까지 찬 정도(0~100). 예시 값.</summary>
    public int? NextHeart { get; init; }
    public bool HeartsFull => Affection >= 5;
    /// <summary>만났고 하트가 아직 덜 찼을 때만 "다음 하트까지" 막대를 보인다.</summary>
    public bool ShowNextHeart => Owned && !HeartsFull && NextHeart.HasValue;
    public bool ShowHeartsFull => Owned && HeartsFull;
    public string NextHeartText => $"다음 하트까지 {NextHeart}%";

    /// <summary>그림. 못 만난 정령은 실루엣(밝은 테마용: 바탕에 곱해지는 회색).</summary>
    public ImageSource? Art { get; init; }
    /// <summary>다크 테마용 그림. 만난 정령은 Art 와 같고, 못 만난 정령은 밝게 뒤집은 실루엣.</summary>
    public ImageSource? ArtDark { get; init; }

    /// <summary>같은 카테고리에서 만난 수 / 전체 수 (힌트 패널).</summary>
    public int CategoryOwned { get; init; }
    public int CategoryTotal { get; init; }
    public string CategoryProgressText => $"{Category}에서 {CategoryOwned} / {CategoryTotal} 만났어요";

    private bool _isRepresentative;
    public bool IsRepresentative { get => _isRepresentative; set { if (Set(ref _isRepresentative, value)) Raise(nameof(Tip)); } }

    private bool _isSelected;
    public bool IsSelected { get => _isSelected; set => Set(ref _isSelected, value); }

    public bool Unknown => !Owned;
    public bool HasElement => Element.Length > 0;
    public IReadOnlyList<bool> Hearts { get; private set; } = [];
    public string AffectionText => $"호감도 {Affection} / 5";
    public string HeartsTip => Owned ? AffectionText : "아직 만나지 못했어요";
    public string Tip => Owned ? $"{Name} · {Element} · 호감도 {Affection}/5{(IsRepresentative ? " · 대표 정령" : "")}" : "아직 만나지 못한 정령";

    internal SpiritRow WithHearts() { Hearts = Enumerable.Range(1, 5).Select(i => i <= Affection).ToList(); return this; }
}

public static class SpiritCatalog
{
    private sealed record Def(string Id, string Name, string Element, string Color, string Category, int Affection, bool Owned,
        string Description, string Hint, string FirstMet, int SheetX = -1, int SheetY = -1, int NextHeart = 0, int Days = 0);

    // 그림: 구름이는 투명 배경 원본, 나머지는 Figma 캡처(dex-sheet.png, 360×800)에서 96×96 을 잘라 쓴다.
    // 못 만난 정령은 모두 같은 실루엣 칸([246,370])을 쓴다.
    private const int SilhouetteX = 246, SilhouetteY = 370;

    private static readonly Def[] Defs =
    [
        new("wind", "구름이", "바람", "#FF96DE", "자연", 5, true, "몽실몽실한 바람 정령이에요. 할 일을 하나씩 끝낼 때마다 볼이 더 발그레해져요.", "", "2026. 08. 02", NextHeart: 100, Days: 72),
        new("leaf", "꽃잎이", "숲", "#8BC34A", "자연", 4, true, "꽃잎 모자를 쓴 숲 정령이에요. 꾸준히 해낸 날이 쌓이면 꽃이 하나씩 피어나요.", "", "2026. 08. 20", 132, 214, 60, 54),
        new("fire", "불꽃이", "불", "#FF8A3D", "자연", 3, true, "열정 가득한 불 정령이에요. 중요한 일정을 끝내면 불꽃이 더 크게 타올라요.", "", "2026. 09. 03", 246, 214, 35, 40),
        new("water", "물순이", "물", "#6EC6F0", "자연", 2, true, "말랑말랑한 물방울 정령이에요. 차분하게 하루를 정리하는 걸 좋아해요.", "", "2026. 09. 21", 18, 370, 70, 22),
        new("star", "별아기", "별", "#F7C948", "우주", 1, true, "반짝이는 별 정령이에요. 늦은 밤까지 남은 일을 끝내면 살며시 빛나요.", "", "2026. 10. 05", 132, 370, 20, 8),
        new("q1", "???", "", "", "자연", 0, false, "", "작심삼일을 무사히 넘긴 날에 나타나요.", ""),
        new("q2", "???", "", "", "동물", 0, false, "", "일주일 동안 매일 하나씩 완료하면 만날 수 있어요.", ""),
        new("q3", "???", "", "", "동물", 0, false, "", "중요 일정을 10번 끝내면 찾아와요.", ""),
    ];

    private static readonly Dictionary<string, ImageSource?> ArtCache = [];
    private static BitmapSource? _sheet;
    private static BitmapSource Sheet => _sheet ??= Frozen(new BitmapImage(new Uri("pack://application:,,,/Assets/dex-sheet.png")));

    private static T Frozen<T>(T freezable) where T : Freezable { if (freezable.CanFreeze) freezable.Freeze(); return freezable; }

    private static ImageSource? ArtOf(Def d, bool dark)
    {
        var key = d.Owned ? d.Id : dark ? "?dark" : "?light";
        if (ArtCache.TryGetValue(key, out var cached)) return cached;
        ImageSource? art;
        try
        {
            art = !d.Owned ? Silhouette(dark)
                : d.SheetX < 0 ? Frozen(new BitmapImage(new Uri("pack://application:,,,/Assets/spirit_neutral.png")))
                : Frozen(new CroppedBitmap(Sheet, new Int32Rect(d.SheetX, d.SheetY, 96, 96)));
        }
        catch (Exception ex) when (ex is System.IO.IOException or NotSupportedException or ArgumentException)
        {
            art = null; // 그림을 읽지 못해도 도감은 열린다
        }
        return ArtCache[key] = art;
    }

    /// <summary>
    /// 실루엣 칸은 흰 바탕 위의 회색 그림이라 그대로 올리면 흰 네모가 보인다.
    /// 밝기를 투명도로 바꿔서, 밝은 테마에서는 바탕에 곱해지는 검정(시안과 같은 회색)으로,
    /// 다크 테마에서는 바탕 위에 밝게 얹히는 흰색(시안의 invert + screen)으로 만든다.
    /// </summary>
    private static BitmapSource Silhouette(bool dark)
    {
        const int size = 96;
        var crop = new FormatConvertedBitmap(new CroppedBitmap(Sheet, new Int32Rect(SilhouetteX, SilhouetteY, size, size)), PixelFormats.Bgra32, null, 0);
        var px = new byte[size * size * 4];
        crop.CopyPixels(px, size * 4, 0);
        for (int i = 0; i < px.Length; i += 4)
        {
            // 흰 바탕 위에 올렸을 때의 밝기
            double a = px[i + 3] / 255.0;
            double gray = (0.114 * px[i] + 0.587 * px[i + 1] + 0.299 * px[i + 2]) * a + 255 * (1 - a);
            byte alpha = (byte)Math.Clamp(255 - Math.Round(gray), 0, 255);
            byte c = dark ? alpha : (byte)0; // Pbgra32: 색에 투명도가 미리 곱해져 있다
            px[i] = c; px[i + 1] = c; px[i + 2] = c; px[i + 3] = alpha;
        }
        return Frozen(BitmapSource.Create(size, size, 96, 96, PixelFormats.Pbgra32, null, px, size * 4));
    }

    public static bool IsOwned(string id) => Defs.Any(d => d.Id == id && d.Owned);

    internal static ImageSource? FallbackArt(bool owned, bool dark) => ArtOf(Defs[owned ? 0 : 5], dark);

    public static IReadOnlyList<SpiritRow> Build(string representativeId, string selectedId) =>
        Defs.Select((d, index) => new SpiritRow
        {
            Id = d.Id, Name = d.Name, Element = d.Element, Category = d.Category, Affection = d.Affection, Owned = d.Owned,
            Description = d.Description, Hint = d.Hint, FirstMet = d.FirstMet, NextHeart = d.NextHeart, Days = d.Days,
            Met = d.Owned ? index + 1 : 99,
            Art = ArtOf(d, dark: false), ArtDark = ArtOf(d, dark: true),
            CategoryOwned = Defs.Count(x => x.Category == d.Category && x.Owned), CategoryTotal = Defs.Count(x => x.Category == d.Category),
            ElementBrush = d.Color.Length > 0 ? Frozen((SolidColorBrush)new BrushConverter().ConvertFromString(d.Color)!) : Brushes.Transparent,
            IsRepresentative = d.Id == representativeId, IsSelected = d.Id == selectedId,
        }.WithHearts()).ToList();
}
