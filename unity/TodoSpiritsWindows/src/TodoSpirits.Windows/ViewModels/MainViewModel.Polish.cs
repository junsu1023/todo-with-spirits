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

/// <summary>시안의 세부 요소: 사이드바 숨김, 검색, 홈 칩, 우클릭 메뉴, 일정 패널의 달력·시간, 도감 보기 방식.</summary>
public sealed partial class MainViewModel
{
    private void InitPolish()
    {
        ToggleNavCommand = new RelayCommand(_ => TogglePin());
        NavMenuCommand = new RelayCommand(_ => { if (NavPinned) TogglePin(); else { NavPeekSticky = !NavPeek; NavPeek = !NavPeek; } });
        // 검색 버튼: 닫혀 있으면 열고, 검색어가 비어 있을 때만 닫는다. Ctrl+F 는 열기만 한다.
        ToggleSearchCommand = new RelayCommand(_ => { if (!SearchOpen) SearchOpen = true; else if (!IsSearching) SearchOpen = false; else SearchFocusRequested?.Invoke(); });
        OpenSearchCommand = new RelayCommand(_ => { if (Page is not ("home" or "calendar")) return; SearchOpen = true; SearchFocusRequested?.Invoke(); });
        CloseSearchCommand = new RelayCommand(_ => { SearchText = ""; SearchOpen = false; });
        ToggleOnlyImportantCommand = new RelayCommand(_ => { _onlyImportant = !_onlyImportant; Refresh(); });
        ToggleEditSectionCommand = new RelayCommand(p => { var k = (string)p!; EditOpen = EditOpen == k ? "" : k; });
        PickRepeatCommand = new RelayCommand(p => { EditOpen = ""; EditRepeat = (string)p!; });
        PickAlarmCommand = new RelayCommand(p => { EditOpen = ""; EditAlarm = (string)p!; });
        ToggleHideDoneCommand = new RelayCommand(_ => { _homeHideDone = !_homeHideDone; Refresh(); });
        ToggleImportantCommand = new RelayCommand(p =>
        {
            var item = ((TaskRow)p!).Item;
            if (IsSignedIn) { _ = ToggleServerImportant(item); return; }
            item.Important = !item.Important;
            Commit();
        }, _ => !_serverBusy);
        PostponeCommand = new RelayCommand(p =>
        {
            var t = ((TaskRow)p!).Item;
            if (Recur.IsRepeating(t)) { ShowToast("반복 일정은 미룰 수 없어요. 날짜나 반복을 바꿔 주세요.", null); return; }
            var date = (t.Date < Today ? Today : t.Date).AddDays(1);
            MoveOne(t, date, $"{Fmt.Short(date)}로 미뤘어요");
        }, _ => !_serverBusy);
        DeleteRowCommand = new RelayCommand(p => DeleteItem(((TaskRow)p!).Item), _ => !_serverBusy);

        ToggleEditCalCommand = new RelayCommand(_ => { EditOpen = EditOpen == "date" ? "" : "date"; });
        EditCalPrevCommand = new RelayCommand(_ => { _editCalMonth = _editCalMonth.AddMonths(-1); RaiseEditCal(); });
        EditCalNextCommand = new RelayCommand(_ => { _editCalMonth = _editCalMonth.AddMonths(1); RaiseEditCal(); });
        PickEditDateCommand = new RelayCommand(p =>
        {
            var from = _editing?.Date;
            EditDate = ((MiniDay)p!).Date.ToDateTime(TimeOnly.MinValue);
            EditOpen = ""; // 고르면 접는다
            if (_editing is { } t && from is { } f && f != t.Date && !Recur.IsRepeating(t)) ShowToast($"{Fmt.Short(t.Date)}로 옮겼어요", null);
        });
        HourUpCommand = new RelayCommand(_ => EditHour = (EditHour + 1) % 24);
        HourDownCommand = new RelayCommand(_ => EditHour = (EditHour + 23) % 24);
        MinuteUpCommand = new RelayCommand(_ => EditMinuteIndex = (EditMinuteIndex + 1) % 12);
        MinuteDownCommand = new RelayCommand(_ => EditMinuteIndex = (EditMinuteIndex + 11) % 12);
    }

    // ---------- 사이드바 ----------
    public ICommand ToggleNavCommand { get; private set; } = null!;
    public bool NavVisible
    {
        get => !_data.NavHidden;
        set { if (_data.NavHidden == !value) return; _data.NavHidden = !value; Persist(); Raise(); Raise(nameof(NavTip)); }
    }
    public string NavTip => NavPinned ? "사이드바 숨기기 (Ctrl+B)" : "사이드바 고정 (Ctrl+B)";
    public string ProfileName => _data.Nickname.Length > 0 ? _data.Nickname : "닉네임 없음";

    // ---------- 검색 · 홈 칩 ----------
    public ICommand ToggleSearchCommand { get; private set; } = null!;
    public ICommand OpenSearchCommand { get; private set; } = null!;
    public ICommand CloseSearchCommand { get; private set; } = null!;
    public ICommand ToggleOnlyImportantCommand { get; private set; } = null!;
    /// <summary>검색 칸에 글자를 칠 수 있게 초점을 달라는 신호.</summary>
    public event Action? SearchFocusRequested;
    public ICommand ToggleHideDoneCommand { get; private set; } = null!;

    private bool _searchOpen;
    public bool SearchOpen { get => _searchOpen; set => Set(ref _searchOpen, value); }

    private string _searchText = "";
    public string SearchText { get => _searchText; set { if (Set(ref _searchText, value ?? "")) Refresh(); } }
    public bool IsSearching => _searchText.Trim().Length > 0;

    private bool _homeHideDone;
    public bool HomeHideDone => _homeHideDone;

    private bool _onlyImportant;
    /// <summary>홈의 "중요 일정" 칩을 눌러 중요한 일정만 보는 중.</summary>
    public bool OnlyImportant => _onlyImportant;
    public bool IsFiltering => IsSearching || _onlyImportant;

    private bool Match(TodoItem t) => !IsSearching || t.Title.Contains(_searchText.Trim(), StringComparison.CurrentCultureIgnoreCase);
    private bool ShowOnHome(TodoItem t) => Match(t) && (!_onlyImportant || t.Important);

    public int ImportantLeft { get; private set; }
    public bool HasImportantLeft => ImportantLeft > 0 || _onlyImportant;
    public string ImportantChip => $"중요 일정이 {ImportantLeft}건 있어요!";
    public bool HasDoneToday => TodayDone > 0;
    public bool HasTodayTasks => TodayTotal > 0;
    public string DoneChip => $"완료 ({TodayDone})";
    public bool HomeNoMatch => IsFiltering && !HasCarry && !HasTimed && !HasAllDay && !HasDoneRows;
    public string HomeNoMatchText => IsSearching ? "검색한 일정이 없어요" : "조건에 맞는 일정이 없어요";

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

    public ICommand ToggleEditSectionCommand { get; private set; } = null!;
    public ICommand PickRepeatCommand { get; private set; } = null!;
    public ICommand PickAlarmCommand { get; private set; } = null!;

    private string _editOpen = "";
    /// <summary>패널에서 펼친 항목: "" / date / time / repeat / alarm. 한 번에 하나만 펼친다.</summary>
    public string EditOpen
    {
        get => _editOpen;
        set
        {
            if (!Set(ref _editOpen, value ?? "")) return;
            if (_editOpen == "date") { _editCalMonth = FirstOfMonth(EditDateOnly); RaiseEditCal(); }
            Raise(nameof(EditCalOpen)); Raise(nameof(EditTimeOpen)); Raise(nameof(RepeatMenuOpen)); Raise(nameof(AlarmMenuOpen));
        }
    }
    public bool EditCalOpen { get => _editOpen == "date"; set => EditOpen = value ? "date" : ""; }
    public bool EditTimeOpen => _editOpen == "time" && EditHasTime;
    public bool RepeatMenuOpen { get => _editOpen == "repeat"; set { if (!value && _editOpen == "repeat") EditOpen = ""; } }
    public bool AlarmMenuOpen { get => _editOpen == "alarm"; set { if (!value && _editOpen == "alarm") EditOpen = ""; } }

    private DateOnly _editCalMonth = FirstOfMonth(DateOnly.FromDateTime(DateTime.Now));
    private static DateOnly FirstOfMonth(DateOnly d) => new(d.Year, d.Month, 1);
    private DateOnly EditDateOnly => DateOnly.FromDateTime(EditDate ?? DateTime.Now);

    public string EditDateText { get { var d = EditDateOnly; return $"{d.Year}. {d.Month:00}. {d.Day:00} ({Fmt.Dow(d)})"; } }
    public string EditCalTitle => _editCalMonth.Year == Today.Year ? $"{_editCalMonth.Month}월" : $"{_editCalMonth.Year}년 {_editCalMonth.Month}월";
    public IReadOnlyList<MiniDay> EditCalCells
    {
        get
        {
            var selected = EditDateOnly;
            // 시안처럼 그 달의 날짜만 그린다. 앞쪽 빈칸은 IsOther 로 표시해 화면에서 숨긴다
            return Enumerable.Range(0, (int)_editCalMonth.DayOfWeek).Select(i => new MiniDay { Date = _editCalMonth.AddDays(-1 - i), IsOther = true })
                .Concat(Enumerable.Range(0, DateTime.DaysInMonth(_editCalMonth.Year, _editCalMonth.Month)).Select(i => _editCalMonth.AddDays(i))
                    .Select(d => new MiniDay { Date = d, IsSelected = d == selected, IsToday = d == Today })).ToList();
        }
    }
    private void RaiseEditCal() { Raise(nameof(EditCalTitle)); Raise(nameof(EditCalCells)); Raise(nameof(EditDateText)); Raise(nameof(MonthDayChips)); }

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

    // 도감 보기 방식(전체/카테고리별 · 미보유만 · 정렬)은 MainViewModel.Dex.cs 로 옮겼다
}
