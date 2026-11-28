using System.Windows.Input;
using System.Windows.Media;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>도감 카테고리별 보기의 한 묶음. 머리띠 색은 시안(dex-v4.html)의 카테고리 색.</summary>
public sealed record SpiritGroup(string Name, string CountText, IReadOnlyList<SpiritRow> Spirits, string EmptyText,
    Brush HeadBg, Brush HeadFg, Brush HeadBgDark, Brush HeadFgDark)
{
    public bool IsEmpty => Spirits.Count == 0;
}

/// <summary>도감 정렬 메뉴의 한 줄.</summary>
public sealed class DexSortOption(string label, bool first) : ObservableObject
{
    public string Label { get; } = label;
    /// <summary>첫 줄에는 위쪽 구분선이 없다.</summary>
    public bool IsFirst { get; } = first;
    private bool _isSelected;
    public bool IsSelected { get => _isSelected; set => Set(ref _isSelected, value); }
}

/// <summary>정령 도감 화면의 상태: 선택 · 대표 정령 · 보기 방식(전체/카테고리별) · 미보유만 · 정렬.</summary>
public sealed partial class MainViewModel
{
    private const string SortAffection = "호감도 순", SortMet = "만난 순", SortName = "이름 순";

    // 카테고리 순서와 머리띠 색 (밝은 테마 바탕/글자, 다크 테마 바탕/글자)
    private static readonly (string Name, string Bg, string Fg, string BgDark, string FgDark)[] DexCategories =
    [
        ("자연", "#F1F8DC", "#66730A", "#2C3214", "#D6EC5E"),
        ("동물", "#FFF6D6", "#7E6414", "#352D14", "#F0CF6A"),
        ("우주", "#E3F2FB", "#3F6A84", "#17293A", "#9CCBEA"),
        ("미분류", "#E3F2FB", "#3F6A84", "#17293A", "#9CCBEA"),
    ];

    private static Brush DexBrush(string hex)
    {
        var b = new SolidColorBrush((Color)ColorConverter.ConvertFromString(hex));
        b.Freeze();
        return b;
    }

    private void InitDex()
    {
        SelectSpiritCommand = new RelayCommand(p => SelectSpirit(p as SpiritRow));
        SetRepresentativeCommand = new RelayCommand(_ => MakeRepresentative(SelectedSpirit), _ => !_serverBusy && SelectedSpirit is { Owned: true, IsRepresentative: false });
        SetDexModeCommand = new RelayCommand(p => { if (p is string mode && mode != _dexMode) { _dexMode = mode; Raise(nameof(DexMode)); Raise(nameof(DexAllEmpty)); } });

        if (!SpiritCatalog.IsOwned(_data.RepresentativeId)) _data.RepresentativeId = "wind";
        SetDexRows(SpiritCatalog.Build(_data.RepresentativeId, _data.RepresentativeId));
    }

    // ---------- 선택 · 대표 정령 ----------
    public ICommand SelectSpiritCommand { get; private set; } = null!;
    public ICommand SetRepresentativeCommand { get; private set; } = null!;
    public IReadOnlyList<SpiritRow> AllSpirits { get; private set; } = [];
    public SpiritRow? SelectedSpirit => AllSpirits.FirstOrDefault(s => s.IsSelected);
    public bool HasSelectedSpirit => SelectedSpirit is not null;
    public SpiritRow? Representative => AllSpirits.FirstOrDefault(s => s.IsRepresentative);
    public int SpiritsOwned => AllSpirits.Count(s => s.Owned);
    public int SpiritsTotal => AllSpirits.Count;
    public string SpiritsProgress => $"{SpiritsOwned} / {SpiritsTotal}";
    public string RepresentativeLabel => SelectedSpirit?.IsRepresentative == true ? "지금 대표 정령이에요" : "대표 정령으로 설정";
    public string DexPanelTitle => SelectedSpirit is { Owned: false } ? "힌트" : "정령 정보";
    /// <summary>시안에는 없는 안내. 요약 카드의 말풍선으로 보여 준다.</summary>
    public string DexNote => IsSignedIn
        ? "계정의 정령 도감이에요. 처음 만난 날짜와 획득 힌트는 아직 제공되지 않아요."
        : "지금 보이는 정령 · 호감도 · 날짜는 예시예요. 로그인하면 내 도감이 보여요.";

    private void SetDexRows(IReadOnlyList<SpiritRow> rows)
    {
        AllSpirits = rows;
        if (IsSignedIn && _dexSort == SortMet) _dexSort = SortAffection;
        var labels = IsSignedIn ? new[] { SortAffection, SortName } : [SortAffection, SortMet, SortName];
        DexSortOptions = labels.Select((label, i) => new DexSortOption(label, i == 0) { IsSelected = label == _dexSort }).ToList();
        if (SelectedSpirit is null) SelectSpirit(Representative ?? rows.FirstOrDefault());
        RebuildDexLists();
        Raise(nameof(DexSortOptions));
        Raise(nameof(DexSort));
        Raise(nameof(Representative));
        Raise(nameof(SpiritsOwned));
        Raise(nameof(SpiritsTotal));
        Raise(nameof(SpiritsProgress));
        Raise(nameof(SelectedSpirit));
        Raise(nameof(HasSelectedSpirit));
        Raise(nameof(RepresentativeLabel));
        Raise(nameof(DexPanelTitle));
    }

    /// <summary>정령을 고른다. 고른 정령이 바뀌었으면 true.</summary>
    public bool SelectSpirit(SpiritRow? row)
    {
        if (ReferenceEquals(SelectedSpirit, row)) return false;
        foreach (var s in AllSpirits) s.IsSelected = ReferenceEquals(s, row);
        Raise(nameof(SelectedSpirit)); Raise(nameof(HasSelectedSpirit)); Raise(nameof(RepresentativeLabel)); Raise(nameof(DexPanelTitle));
        CommandManager.InvalidateRequerySuggested();
        return true;
    }

    /// <summary>만난 정령을 대표 정령으로 정한다 (버튼 · 두 번 누르기 · Enter).</summary>
    public void MakeRepresentative(SpiritRow? row)
    {
        if (row is not { Owned: true }) return;
        if (IsSignedIn)
        {
            if (!row.IsRepresentative && row.ServerId is not null) _ = SetServerRepresentative(row);
            return;
        }
        if (!row.IsRepresentative)
        {
            _data.RepresentativeId = row.Id;
            Persist();
            foreach (var s in AllSpirits) s.IsRepresentative = ReferenceEquals(s, row);
            Raise(nameof(Representative)); Raise(nameof(RepresentativeLabel));
            CommandManager.InvalidateRequerySuggested();
        }
        ShowToast($"{row.Name}{Fmt.Josa(row.Name, "이", "가")} 대표 정령이 됐어요", null);
    }

    // ---------- 보기 방식 ----------
    public ICommand SetDexModeCommand { get; private set; } = null!;
    private string _dexMode = "all";
    public string DexMode => _dexMode;

    private bool _dexOnlyMissing;
    public bool DexOnlyMissing
    {
        get => _dexOnlyMissing;
        set
        {
            if (!Set(ref _dexOnlyMissing, value)) return;
            // 미보유만 켜면 만난 정령은 목록에서 사라지므로, 첫 번째 못 만난 정령으로 옮긴다
            if (value && SelectedSpirit is { Owned: true }) SelectSpirit(AllSpirits.FirstOrDefault(s => !s.Owned));
            RebuildDexLists();
        }
    }

    private string _dexSort = SortAffection;
    public string DexSort
    {
        get => _dexSort;
        set
        {
            if (value is null || !Set(ref _dexSort, value)) return;
            foreach (var o in DexSortOptions) o.IsSelected = o.Label == value;
            RebuildDexLists();
        }
    }
    public IReadOnlyList<DexSortOption> DexSortOptions { get; private set; } = [];

    /// <summary>화면에 보이는 정령들. 정렬이나 미보유만이 바뀔 때만 새로 만든다.</summary>
    public IReadOnlyList<SpiritRow> Spirits { get; private set; } = [];
    public IReadOnlyList<SpiritGroup> SpiritGroups { get; private set; } = [];
    public bool DexEmpty => Spirits.Count == 0;
    public bool DexAllEmpty => DexEmpty && _dexMode == "all";

    private void RebuildDexLists()
    {
        IEnumerable<SpiritRow> list = _dexOnlyMissing ? AllSpirits.Where(s => !s.Owned) : AllSpirits;
        // 못 만난 정령은 어느 정렬에서도 뒤에 둔다
        var owned = list.OrderByDescending(s => s.Owned);
        Spirits = (_dexSort switch
        {
            SortName => owned.ThenBy(s => s.Name, StringComparer.Create(System.Globalization.CultureInfo.GetCultureInfo("ko-KR"), false)),
            SortMet => owned.ThenBy(s => s.Met),
            _ => owned.ThenByDescending(s => s.Affection),
        }).ToList();

        // 빈 카테고리도 그대로 보여 준다
        SpiritGroups = DexCategories.Where(c => IsSignedIn ? c.Name == "미분류" : c.Name != "미분류").Select(c => new SpiritGroup(c.Name,
            $"({AllSpirits.Count(s => s.Category == c.Name && s.Owned)} / {AllSpirits.Count(s => s.Category == c.Name)})",
            Spirits.Where(s => s.Category == c.Name).ToList(),
            _dexOnlyMissing ? "이 카테고리의 정령을 모두 만났어요" : "아직 만난 정령이 없어요",
            DexBrush(c.Bg), DexBrush(c.Fg), DexBrush(c.BgDark), DexBrush(c.FgDark))).ToList();

        Raise(nameof(Spirits)); Raise(nameof(SpiritGroups)); Raise(nameof(DexEmpty)); Raise(nameof(DexAllEmpty));
    }
}
