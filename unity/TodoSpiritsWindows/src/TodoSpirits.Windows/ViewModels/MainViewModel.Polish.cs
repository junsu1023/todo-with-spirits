using System.Windows.Input;
using TodoSpirits.Windows.Models;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>일정 패널의 작은 달력 한 칸.</summary>
public sealed class MiniDay
{
    public DateOnly Date { get; init; }
    public bool IsOther { get; init; }
    public bool IsSelected { get; init; }
    public bool IsToday { get; init; }
    public string Num => Date.Day.ToString();
}

/// <summary>도감 카테고리별 보기의 한 묶음.</summary>
public sealed record SpiritGroup(string Name, string CountText, IReadOnlyList<SpiritRow> Spirits);

/// <summary>시안의 세부 요소: 사이드바 숨김, 검색, 홈 칩, 우클릭 메뉴, 일정 패널의 달력·시간, 도감 보기 방식.</summary>
public sealed partial class MainViewModel
{
    private void InitPolish()
    {
        ToggleNavCommand = new RelayCommand(_ => NavVisible = !NavVisible);
        ToggleSearchCommand = new RelayCommand(_ => { SearchOpen = !SearchOpen; if (!SearchOpen) SearchText = ""; });
        ToggleHideDoneCommand = new RelayCommand(_ => { _homeHideDone = !_homeHideDone; Refresh(); });
        ToggleImportantCommand = new RelayCommand(p => { var t = ((TaskRow)p!).Item; t.Important = !t.Important; Commit(); });
        PostponeCommand = new RelayCommand(p =>
        {
            var t = ((TaskRow)p!).Item; var from = t.Date;
            if (Recur.IsRepeating(t)) { ShowToast("반복 일정은 미룰 수 없어요. 날짜나 반복을 바꿔 주세요.", null); return; }
            t.Date = (t.Date < Today ? Today : t.Date).AddDays(1);
            Commit();
            ShowToast($"{Fmt.Short(t.Date)}로 미뤘어요", () => { t.Date = from; Commit(); });
        });
        DeleteRowCommand = new RelayCommand(p =>
        {
            var t = ((TaskRow)p!).Item; var index = _data.Tasks.IndexOf(t);
            if (index < 0) return;
            _data.Tasks.RemoveAt(index);
            if (_editing == t) ClosePanel();
            Commit();
            ShowToast(Recur.IsRepeating(t) ? $"\"{t.Title}\" 반복 일정을 모두 삭제했어요" : $"\"{t.Title}\" 삭제했어요", () => { _data.Tasks.Insert(Math.Min(index, _data.Tasks.Count), t); Commit(); });
        });

        ToggleEditCalCommand = new RelayCommand(_ => { EditCalOpen = !EditCalOpen; });
        EditCalPrevCommand = new RelayCommand(_ => { _editCalMonth = _editCalMonth.AddMonths(-1); RaiseEditCal(); });
        EditCalNextCommand = new RelayCommand(_ => { _editCalMonth = _editCalMonth.AddMonths(1); RaiseEditCal(); });
        PickEditDateCommand = new RelayCommand(p => { EditDate = ((MiniDay)p!).Date.ToDateTime(TimeOnly.MinValue); });
        HourUpCommand = new RelayCommand(_ => EditHour = (EditHour + 1) % 24);
        HourDownCommand = new RelayCommand(_ => EditHour = (EditHour + 23) % 24);
        MinuteUpCommand = new RelayCommand(_ => EditMinuteIndex = (EditMinuteIndex + 1) % 12);
        MinuteDownCommand = new RelayCommand(_ => EditMinuteIndex = (EditMinuteIndex + 11) % 12);

        SetDexModeCommand = new RelayCommand(p => { _dexMode = (string)p!; Raise(string.Empty); });
    }

    // ---------- 사이드바 ----------
    public ICommand ToggleNavCommand { get; private set; } = null!;
    public bool NavVisible
    {
        get => !_data.NavHidden;
        set { if (_data.NavHidden == !value) return; _data.NavHidden = !value; Persist(); Raise(); Raise(nameof(NavTip)); }
    }
    public string NavTip => NavVisible ? "사이드바 숨기기 (Ctrl+B)" : "사이드바 보이기 (Ctrl+B)";
    public string ProfileName => _data.Nickname.Length > 0 ? _data.Nickname : "닉네임 없음";

    // ---------- 검색 · 홈 칩 ----------
    public ICommand ToggleSearchCommand { get; private set; } = null!;
    public ICommand ToggleHideDoneCommand { get; private set; } = null!;

    private bool _searchOpen;
    public bool SearchOpen { get => _searchOpen; set => Set(ref _searchOpen, value); }

    private string _searchText = "";
    public string SearchText { get => _searchText; set { if (Set(ref _searchText, value ?? "")) Refresh(); } }
    public bool IsSearching => _searchText.Trim().Length > 0;

    private bool _homeHideDone;
    public bool HomeHideDone => _homeHideDone;

    private bool Match(TodoItem t) => !IsSearching || t.Title.Contains(_searchText.Trim(), StringComparison.CurrentCultureIgnoreCase);
    private bool ShowOnHome(Occ o) => Match(o.Item) && !(_homeHideDone && o.Done);

    public int ImportantLeft { get; private set; }
    public bool HasImportantLeft => ImportantLeft > 0;
    public string ImportantChip => $"중요 일정이 {ImportantLeft}건 있어요!";
    public bool HasDoneToday => TodayDone > 0;
    public string DoneChip => _homeHideDone ? $"완료 숨김 ({TodayDone})" : $"완료 ({TodayDone})";
    public bool HomeNoMatch => IsSearching && !HasCarry && !HasTimed && !HasAllDay;

    // ---------- 우클릭 메뉴 ----------
    public ICommand ToggleImportantCommand { get; private set; } = null!;
    public ICommand PostponeCommand { get; private set; } = null!;
    public ICommand DeleteRowCommand { get; private set; } = null!;

    // ---------- 일정 패널: 날짜(작은 달력) · 시간(올리고 내리기) ----------
    public ICommand ToggleEditCalCommand { get; private set; } = null!;
    public ICommand EditCalPrevCommand { get; private set; } = null!;
    public ICommand EditCalNextCommand { get; private set; } = null!;
    public ICommand PickEditDateCommand { get; private set; } = null!;
    public ICommand HourUpCommand { get; private set; } = null!;
    public ICommand HourDownCommand { get; private set; } = null!;
    public ICommand MinuteUpCommand { get; private set; } = null!;
    public ICommand MinuteDownCommand { get; private set; } = null!;

    private bool _editCalOpen;
    public bool EditCalOpen { get => _editCalOpen; set { if (Set(ref _editCalOpen, value) && value) { _editCalMonth = FirstOfMonth(EditDateOnly); RaiseEditCal(); } } }

    private DateOnly _editCalMonth = FirstOfMonth(DateOnly.FromDateTime(DateTime.Now));
    private static DateOnly FirstOfMonth(DateOnly d) => new(d.Year, d.Month, 1);
    private DateOnly EditDateOnly => DateOnly.FromDateTime(EditDate ?? DateTime.Now);

    public string EditDateText { get { var d = EditDateOnly; return $"{d.Year}. {d.Month:00}. {d.Day:00} ({Fmt.Dow(d)})"; } }
    public string EditCalTitle => $"{_editCalMonth.Year}년 {_editCalMonth.Month}월";
    public IReadOnlyList<MiniDay> EditCalCells
    {
        get
        {
            var start = _editCalMonth.AddDays(-(int)_editCalMonth.DayOfWeek);
            var selected = EditDateOnly;
            return Enumerable.Range(0, 42).Select(i => start.AddDays(i)).Select(d => new MiniDay
            {
                Date = d, IsOther = d.Month != _editCalMonth.Month, IsSelected = d == selected, IsToday = d == Today,
            }).ToList();
        }
    }
    private void RaiseEditCal() { Raise(nameof(EditCalTitle)); Raise(nameof(EditCalCells)); Raise(nameof(EditDateText)); }

    public string EditAmPm => EditHour < 12 ? "오전" : "오후";
    public string EditHour12 => (EditHour % 12 == 0 ? 12 : EditHour % 12).ToString();
    public string EditMinuteText => (EditMinuteIndex * 5).ToString("00");
    // 휠처럼 보이게 앞뒤 값을 흐리게 함께 보여 준다
    private static string Hour12(int h) => (h % 12 == 0 ? 12 : h % 12).ToString();
    public string EditHourPrev => Hour12((EditHour + 23) % 24);
    public string EditHourNext => Hour12((EditHour + 1) % 24);
    public string EditMinutePrev => ((EditMinuteIndex + 11) % 12 * 5).ToString("00");
    public string EditMinuteNext => ((EditMinuteIndex + 1) % 12 * 5).ToString("00");
    public string EditTimeText => EditHasTime ? Fmt.Time(new TimeOnly(EditHour, EditMinuteIndex * 5)) : "하루 종일";

    /// <summary>패널의 편집 값이 바뀔 때마다 파생 글자를 갱신한다.</summary>
    private void RaiseEditDerived()
    {
        Raise(nameof(EditDateText)); Raise(nameof(EditCalCells)); Raise(nameof(EditAmPm)); Raise(nameof(EditHour12));
        Raise(nameof(EditMinuteText)); Raise(nameof(EditTimeText));
        Raise(nameof(EditHourPrev)); Raise(nameof(EditHourNext)); Raise(nameof(EditMinutePrev)); Raise(nameof(EditMinuteNext));
        Raise(nameof(WeekdayChips)); Raise(nameof(MonthDayChips)); Raise(nameof(EditRepeatSummary));
    }

    // ---------- 도감 보기 방식 ----------
    public ICommand SetDexModeCommand { get; private set; } = null!;
    private string _dexMode = "all";
    public string DexMode => _dexMode;

    private bool _dexOnlyMissing;
    public bool DexOnlyMissing { get => _dexOnlyMissing; set { if (Set(ref _dexOnlyMissing, value)) Raise(string.Empty); } }

    private static readonly string[] DexSorts = ["만난 순", "호감도 순", "이름 순"];
    private int _dexSort;
    public string DexSortLabel => DexSorts[_dexSort];
    public ICommand NextDexSortCommand => new RelayCommand(_ => { _dexSort = (_dexSort + 1) % DexSorts.Length; Raise(string.Empty); });

    public IReadOnlyList<SpiritRow> Spirits
    {
        get
        {
            IEnumerable<SpiritRow> list = _dexOnlyMissing ? AllSpirits.Where(s => !s.Owned) : AllSpirits;
            // 못 만난 정령은 어느 정렬에서도 뒤에 둔다
            list = _dexSort switch
            {
                1 => list.OrderByDescending(s => s.Owned).ThenByDescending(s => s.Affection),
                2 => list.OrderByDescending(s => s.Owned).ThenBy(s => s.Name, StringComparer.CurrentCulture),
                _ => list,
            };
            return list.ToList();
        }
    }
    public IReadOnlyList<SpiritGroup> SpiritGroups => new[] { "자연", "동물", "우주" }
        .Select(c => new SpiritGroup(c,
            $"{AllSpirits.Count(s => s.Category == c && s.Owned)} / {AllSpirits.Count(s => s.Category == c)}",
            Spirits.Where(s => s.Category == c).ToList()))
        .Where(g => g.Spirits.Count > 0).ToList();
    public bool DexEmpty => Spirits.Count == 0;
}
