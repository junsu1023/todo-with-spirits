using System.Windows.Input;
using System.Windows.Threading;
using TodoSpirits.Windows.Models;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>
/// 화면 전체의 상태. 일정이 바뀔 때마다 저장하고 Refresh 로 화면용 목록을 다시 만든다.
/// 일정 수가 개인 TODO 규모라서 매번 다시 만드는 쪽이 단순하고 틀릴 일이 적다.
/// </summary>
public sealed partial class MainViewModel : ObservableObject
{
    private readonly TaskStore _store;
    private readonly AppData _data;
    private readonly DispatcherTimer _toastTimer = new() { Interval = TimeSpan.FromSeconds(4) };
    private readonly DispatcherTimer _clock = new() { Interval = TimeSpan.FromSeconds(30) };
    private Action? _undo;

    public MainViewModel() : this(new TaskStore()) { }

    public MainViewModel(TaskStore store)
    {
        _store = store;
        _data = store.Load(out var warning);
        _calMode = _data.CalendarMode is "week" or "month" or "year" ? _data.CalendarMode : "month";
        _isDark = _data.Theme == "dark";
        Today = DateOnly.FromDateTime(DateTime.Now);
        _selected = Today;

        NavigateCommand = new RelayCommand(p => { Page = (string)p!; ClosePanel(); });
        SetModeCommand = new RelayCommand(p => CalMode = (string)p!);
        SelectDateCommand = new RelayCommand(p => SelectDate(((DayCell)p!).Date));
        YearDayCommand = new RelayCommand(p => { _calMode = "month"; Raise(nameof(CalMode)); SaveMode(); SelectDate(((DayCell)p!).Date); });
        GoMonthCommand = new RelayCommand(p => { var m = (MonthCard)p!; _calMode = "month"; Raise(nameof(CalMode)); SaveMode(); SelectDate(new DateOnly(m.Year, m.Month, 1)); });
        PrevCommand = new RelayCommand(_ => Shift(-1));
        NextCommand = new RelayCommand(_ => Shift(1));
        GoTodayCommand = new RelayCommand(_ => SelectDate(Today));
        ToggleThemeCommand = new RelayCommand(_ => IsDark = !IsDark);
        AddCommand = new RelayCommand(p => BeginAdd(p is DayCell c ? c.Date : Page == "calendar" ? SelectedDate : Today));
        OpenCommand = new RelayCommand(p => BeginEdit((TaskRow)p!));
        ToggleCommand = new RelayCommand(p => { var row = (TaskRow)p!; Toggle(row.Item, row.Date); });
        MoveToTodayCommand = new RelayCommand(p => { var t = ((TaskRow)p!).Item; var from = t.Date; t.Date = Today; Commit(); ShowToast("오늘 일정으로 옮겼어요", () => { t.Date = from; Commit(); }); });
        MoveAllToTodayCommand = new RelayCommand(_ => MoveAllToToday());
        SaveCommand = new RelayCommand(_ => SaveNew(), _ => IsNew && !string.IsNullOrWhiteSpace(EditTitle));
        DeleteCommand = new RelayCommand(_ => DeleteEditing(), _ => _editing is not null);
        ToggleEditingCommand = new RelayCommand(_ => { if (_editing is not null) { Toggle(_editing, _editingDate); Raise(nameof(EditDoneLabel)); } }, _ => _editing is not null);
        ClosePanelCommand = new RelayCommand(_ => { if (HelpOpen) HelpOpen = false; else if (NotiOpen) NotiOpen = false; else ClosePanel(); });
        UndoCommand = new RelayCommand(_ => { _undo?.Invoke(); HideToast(); });

        _toastTimer.Tick += (_, _) => HideToast();
        _clock.Tick += (_, _) => { var d = DateOnly.FromDateTime(DateTime.Now); if (d != Today) { Today = d; Raise(nameof(Today)); } Refresh(); CheckAlarms(); };
        _clock.Start();

        ThemeService.Apply(_isDark, _data.ContrastMore);
        InitExtras();
        InitPolish();
        InitAlarms();
        Refresh();
        InitSpirit();
        if (warning is not null) ShowToast(warning, null);
    }

    // ---------- 명령 ----------
    public ICommand NavigateCommand { get; }
    public ICommand SetModeCommand { get; }
    public ICommand SelectDateCommand { get; }
    public ICommand YearDayCommand { get; }
    public ICommand GoMonthCommand { get; }
    public ICommand PrevCommand { get; }
    public ICommand NextCommand { get; }
    public ICommand GoTodayCommand { get; }
    public ICommand ToggleThemeCommand { get; }
    public ICommand AddCommand { get; }
    public ICommand OpenCommand { get; }
    public ICommand ToggleCommand { get; }
    public ICommand MoveToTodayCommand { get; }
    public ICommand MoveAllToTodayCommand { get; }
    public ICommand SaveCommand { get; }
    public ICommand DeleteCommand { get; }
    public ICommand ToggleEditingCommand { get; }
    public ICommand ClosePanelCommand { get; }
    public ICommand UndoCommand { get; }

    // ---------- 공통 상태 ----------
    public DateOnly Today { get; private set; }

    private string _page = "home";
    public string Page { get => _page; set { if (Set(ref _page, value)) Refresh(); } }

    private string _calMode;
    public string CalMode { get => _calMode; set { if (Set(ref _calMode, value)) { SaveMode(); ClosePanel(); Refresh(); } } }

    private bool _isDark;
    public bool IsDark
    {
        get => _isDark;
        set
        {
            if (!Set(ref _isDark, value)) return;
            ThemeService.Apply(value, _data.ContrastMore);
            _data.Theme = value ? "dark" : "light";
            if (!_applyingSystem) _data.FollowSystem = false; // 직접 바꾸면 Windows 따라가기를 끈다
            Persist(); Raise(nameof(ThemeLabel)); Raise(nameof(FollowSystem));
        }
    }
    public string ThemeLabel => IsDark ? "라이트 모드로" : "다크 모드로";

    private DateOnly _selected;
    public DateOnly SelectedDate => _selected;

    public string DataPath => _store.FilePath;

    // ---------- 홈 ----------
    public string Greeting { get; private set; } = "";
    public string TodayText { get; private set; } = "";
    public int TodayTotal { get; private set; }
    public int TodayDone { get; private set; }
    public string NextTitle { get; private set; } = "";
    public string NextWhen { get; private set; } = "";
    public bool HasNext => NextTitle.Length > 0;
    public IReadOnlyList<TaskRow> Carry { get; private set; } = [];
    public IReadOnlyList<TaskRow> Timed { get; private set; } = [];
    public IReadOnlyList<TaskRow> AllDay { get; private set; } = [];
    public bool HasCarry => Carry.Count > 0;
    public bool HasTimed => Timed.Count > 0;
    public bool HasAllDay => AllDay.Count > 0;
    public bool HomeEmpty => TodayTotal == 0 && !HasCarry && !IsSearching;
    public string CarryCount => Carry.Count.ToString();
    public string TimedCount => Timed.Count.ToString();
    public string AllDayCount => AllDay.Count.ToString();

    // ---------- 캘린더 ----------
    public string PeriodTitle { get; private set; } = "";
    public string CalHint { get; private set; } = "";
    public IReadOnlyList<DayCell> WeekCells { get; private set; } = [];
    public IReadOnlyList<DayCell> MonthCells { get; private set; } = [];
    public int MonthRows { get; private set; } = 5;
    public IReadOnlyList<MonthCard> YearCards { get; private set; } = [];
    public IReadOnlyList<TaskRow> DayTasks { get; private set; } = [];
    public bool DayEmpty => DayTasks.Count == 0;
    public string SelectedLong { get; private set; } = "";
    public bool SelectedIsToday => _selected == Today;
    public string SelectedStat { get; private set; } = "";

    // ---------- 일정 패널 (추가·상세) ----------
    private TodoItem? _editing;
    private DateOnly _editingDate;
    private bool _loading;

    private bool _panelOpen;
    public bool PanelOpen { get => _panelOpen; private set => Set(ref _panelOpen, value); }
    public bool IsNew => _panelOpen && _editing is null;
    public bool IsExisting => _editing is not null;
    public string PanelTitle => _editing is null ? "일정 추가" : "일정 상세";
    public string EditDoneLabel => _editing is not null && Recur.IsDone(_editing, _editingDate) ? "완료 취소" : "완료";

    private string _editTitle = "";
    public string EditTitle { get => _editTitle; set { if (Set(ref _editTitle, value)) { ApplyEdit(); CommandManager.InvalidateRequerySuggested(); } } }

    private bool _editImportant;
    public bool EditImportant { get => _editImportant; set { if (Set(ref _editImportant, value)) ApplyEdit(); } }

    private DateTime? _editDate;
    public DateTime? EditDate { get => _editDate; set { if (value is not null && Set(ref _editDate, value)) { ApplyEdit(); RaiseEditDerived(); } } }

    private bool _editHasTime;
    public bool EditHasTime { get => _editHasTime; set { if (Set(ref _editHasTime, value)) { ApplyEdit(); RaiseEditDerived(); } } }

    private int _editHour = 9;
    public int EditHour { get => _editHour; set { if (Set(ref _editHour, value)) { ApplyEdit(); RaiseEditDerived(); } } }

    private int _editMinuteIndex;
    /// <summary>5분 단위 목록의 위치 (0 = 00분, 11 = 55분).</summary>
    public int EditMinuteIndex { get => _editMinuteIndex; set { if (Set(ref _editMinuteIndex, value)) { ApplyEdit(); RaiseEditDerived(); } } }

    public IReadOnlyList<string> Hours { get; } = Enumerable.Range(0, 24).Select(h => $"{(h < 12 ? "오전" : "오후")} {(h % 12 == 0 ? 12 : h % 12)}시").ToList();
    public IReadOnlyList<string> Minutes { get; } = Enumerable.Range(0, 12).Select(i => $"{i * 5:00}분").ToList();

    // ---------- 토스트 ----------
    private string _toastText = "";
    public string ToastText { get => _toastText; private set => Set(ref _toastText, value); }
    private bool _toastOpen;
    public bool ToastOpen { get => _toastOpen; private set => Set(ref _toastOpen, value); }
    private bool _canUndo;
    public bool CanUndo { get => _canUndo; private set => Set(ref _canUndo, value); }

    // ---------- 동작 ----------
    private void SelectDate(DateOnly d)
    {
        _selected = d;
        Refresh();
    }

    private void Shift(int n)
    {
        _selected = CalMode switch
        {
            "week" => _selected.AddDays(7 * n),
            "year" => SafeDate(_selected.Year + n, _selected.Month, _selected.Day),
            _ => _selected.AddMonths(n),
        };
        Refresh();
    }

    private static DateOnly SafeDate(int y, int m, int d) => new(y, m, Math.Min(d, DateTime.DaysInMonth(y, m)));

    private void BeginAdd(DateOnly date)
    {
        NotiOpen = false;
        _editing = null;
        _loading = true;
        EditTitle = ""; EditImportant = false; EditDate = date.ToDateTime(TimeOnly.MinValue); EditHasTime = false;
        var now = DateTime.Now.AddHours(1);
        EditHour = now.Hour; EditMinuteIndex = 0;
        EditRepeat = Recur.None; _editRepeatDays = []; EditAlarm = Alarm.None;
        _loading = false;
        EditCalOpen = false; RaiseEditDerived(); RaiseRepeat();
        PanelOpen = true;
        RaisePanel();
    }

    private void BeginEdit(TaskRow row)
    {
        var item = row.Item;
        NotiOpen = false;
        _editing = item;
        _editingDate = row.Date;
        _loading = true;
        EditRepeat = item.Repeat; _editRepeatDays = [.. item.RepeatDays]; EditAlarm = item.Alarm;
        EditTitle = item.Title; EditImportant = item.Important; EditDate = item.Date.ToDateTime(TimeOnly.MinValue);
        EditHasTime = item.Time is not null;
        EditHour = item.Time?.Hour ?? 9; EditMinuteIndex = (item.Time?.Minute ?? 0) / 5;
        _loading = false;
        EditCalOpen = false; RaiseEditDerived(); RaiseRepeat();
        PanelOpen = true;
        RaisePanel();
    }

    private void RaisePanel()
    {
        Raise(nameof(IsNew)); Raise(nameof(IsExisting)); Raise(nameof(PanelTitle)); Raise(nameof(EditDoneLabel));
        CommandManager.InvalidateRequerySuggested();
    }

    private TimeOnly? EditTime => EditHasTime ? new TimeOnly(EditHour, EditMinuteIndex * 5) : null;

    /// <summary>기존 일정은 고치는 즉시 저장한다. 제목을 비우면 제목만 그대로 둔다.</summary>
    private void ApplyEdit()
    {
        if (_loading || _editing is null) return;
        if (!string.IsNullOrWhiteSpace(EditTitle)) _editing.Title = EditTitle.Trim();
        _editing.Important = EditImportant;
        _editing.Date = DateOnly.FromDateTime(EditDate!.Value);
        _editing.Time = EditTime;
        _editing.Repeat = EditRepeat; _editing.RepeatDays = [.. _editRepeatDays]; _editing.Alarm = EditTime is null ? Alarm.None : EditAlarm;
        if (!Recur.IsRepeating(_editing)) _editingDate = _editing.Date;
        Commit();
    }

    private void SaveNew()
    {
        if (string.IsNullOrWhiteSpace(EditTitle)) return;
        var item = new TodoItem
        {
            Title = EditTitle.Trim(), Important = EditImportant,
            Date = DateOnly.FromDateTime(EditDate!.Value), Time = EditTime,
            Repeat = EditRepeat, RepeatDays = [.. _editRepeatDays], Alarm = EditTime is null ? Alarm.None : EditAlarm,
        };
        _data.Tasks.Add(item);
        if (Page == "calendar") _selected = item.Date;
        ClosePanel();
        Commit();
        ShowToast($"{Fmt.Short(item.Date)}에 일정을 추가했어요", null);
    }

    private void DeleteEditing()
    {
        if (_editing is null) return;
        var item = _editing;
        var index = _data.Tasks.IndexOf(item);
        _data.Tasks.Remove(item);
        ClosePanel();
        Commit();
        ShowToast($"\"{item.Title}\" 삭제했어요", () => { _data.Tasks.Insert(Math.Min(index, _data.Tasks.Count), item); Commit(); });
    }

    /// <summary>완료 표시를 바꾼다. 반복 일정은 그 날짜의 회차만 바뀐다.</summary>
    private void Toggle(TodoItem item, DateOnly date)
    {
        bool done = !Recur.IsDone(item, date);
        Recur.SetDone(item, date, done);
        _justDone = done ? (item.Id, date) : null;
        Commit();
        _justDone = null;
        if (done) ShowToast("잘했어요!", () => { Recur.SetDone(item, date, false); Commit(); });
    }
    private (Guid, DateOnly)? _justDone;

    private void MoveAllToToday()
    {
        var moved = _data.Tasks.Where(t => !t.Done && t.Date < Today && !Recur.IsRepeating(t)).Select(t => (t, t.Date)).ToList();
        if (moved.Count == 0) return;
        foreach (var (t, _) in moved) t.Date = Today;
        Commit();
        ShowToast($"{moved.Count}개를 오늘 일정으로 옮겼어요", () => { foreach (var (t, from) in moved) t.Date = from; Commit(); });
    }

    private void ClosePanel()
    {
        _editing = null;
        PanelOpen = false;
        RaisePanel();
    }

    private void Commit() { Persist(); Refresh(); }

    private void Persist()
    {
        try { _store.Save(_data); }
        catch (Exception ex) when (ex is System.IO.IOException or UnauthorizedAccessException)
        {
            ShowToast("저장하지 못했어요: " + ex.Message, null);
        }
    }

    private void SaveMode() { _data.CalendarMode = _calMode; Persist(); }

    private void ShowToast(string text, Action? undo)
    {
        _undo = undo;
        ToastText = text; CanUndo = undo is not null; ToastOpen = true;
        _toastTimer.Stop(); _toastTimer.Start();
    }

    private void HideToast() { _toastTimer.Stop(); ToastOpen = false; _undo = null; }

    // ---------- 화면용 목록 다시 만들기 ----------
    private static IOrderedEnumerable<Occ> ByTime(IEnumerable<Occ> list) =>
        list.OrderBy(o => o.Item.Time is null ? 0 : 1).ThenBy(o => o.Item.Time).ThenBy(o => o.Item.Title);

    /// <summary>그 날짜에 나타나는 일정들 (반복 일정의 회차 포함).</summary>
    private List<Occ> On(DateOnly d) => ByTime(_data.Tasks.Where(t => Recur.Occurs(t, d)).Select(t => new Occ(t, d))).ToList();

    private DayCell Cell(DateOnly d, bool other = false)
    {
        var list = On(d);
        var first = list.FirstOrDefault(o => !o.Done);
        return new DayCell
        {
            Date = d, IsToday = d == Today, IsSelected = d == _selected, IsOther = other, IsPast = d < Today,
            Total = list.Count, DoneCount = list.Count(o => o.Done),
            HasImportant = list.Any(o => o.Item.Important && !o.Done),
            FirstTitle = first.Item?.Title ?? "", FirstImportant = first.Item?.Important ?? false,
        };
    }

    public void Refresh()
    {
        var now = TimeOnly.FromDateTime(DateTime.Now);
        TaskRow Row(Occ o) => new(o, Today, now, _justDone == (o.Item.Id, o.Date));

        // 홈
        var today = On(Today);
        Greeting = now.Hour switch { < 5 => "늦은 밤이에요", < 12 => "좋은 아침이에요", < 18 => "좋은 오후예요", _ => "좋은 저녁이에요" };
        if (_data.Nickname.Length > 0) Greeting += $", {_data.Nickname}님";
        TodayText = Fmt.Short(Today);
        TodayTotal = today.Count; TodayDone = today.Count(o => o.Done);
        var next = today.Where(o => !o.Done && o.Item.Time >= now).OrderBy(o => o.Item.Time).FirstOrDefault();
        NextTitle = next.Item?.Title ?? "";
        if (next.Item?.Time is { } nt)
        {
            var mins = (int)(nt.ToTimeSpan() - now.ToTimeSpan()).TotalMinutes;
            NextWhen = mins <= 0 ? $"{Fmt.Time(nt)} · 곧 시작" : $"{Fmt.Time(nt)} · {(mins >= 60 ? $"{mins / 60}시간 " : "")}{(mins % 60 > 0 ? $"{mins % 60}분 " : "")}후";
        }
        else NextWhen = "";
        // 어제 하지 못한 일: 한 번짜리 일정만. 반복 일정의 지난 회차는 쌓이지 않게 넘기지 않는다.
        Carry = _data.Tasks.Where(t => !Recur.IsRepeating(t) && !t.Done && t.Date < Today && Match(t))
            .OrderBy(t => t.Date).ThenBy(t => t.Time).Select(t => Row(new Occ(t, t.Date))).ToList();
        Timed = today.Where(o => o.Item.Time is not null && ShowOnHome(o)).Select(Row).ToList();
        AllDay = today.Where(o => o.Item.Time is null && ShowOnHome(o)).Select(Row).ToList();
        ImportantLeft = today.Count(o => o.Item.Important && !o.Done);

        // 캘린더
        var weekStart = _selected.AddDays(-(int)_selected.DayOfWeek);
        var weekEnd = weekStart.AddDays(6);
        WeekCells = Enumerable.Range(0, 7).Select(i => Cell(weekStart.AddDays(i))).ToList();

        var first = new DateOnly(_selected.Year, _selected.Month, 1);
        var gridStart = first.AddDays(-(int)first.DayOfWeek);
        MonthRows = (int)Math.Ceiling(((int)first.DayOfWeek + DateTime.DaysInMonth(first.Year, first.Month)) / 7.0);
        MonthCells = Enumerable.Range(0, MonthRows * 7).Select(i => gridStart.AddDays(i)).Select(d => Cell(d, d.Month != first.Month)).ToList();

        // 연간은 365일을 모두 계산하므로 연간 보기일 때만 만든다
        if (CalMode == "year" && Page == "calendar")
        {
            YearCards = Enumerable.Range(1, 12).Select(m =>
            {
                var f = new DateOnly(_selected.Year, m, 1);
                var cells = Enumerable.Range(0, (int)f.DayOfWeek).Select(_ => new DayCell { IsBlank = true })
                    .Concat(Enumerable.Range(0, DateTime.DaysInMonth(f.Year, m)).Select(i => Cell(f.AddDays(i)))).ToList();
                return new MonthCard { Year = f.Year, Month = m, IsCurrent = Today.Year == f.Year && Today.Month == m, Total = cells.Sum(c => c.Total), Cells = cells };
            }).ToList();
        }

        PeriodTitle = CalMode switch
        {
            "week" => weekStart.Month == weekEnd.Month
                ? $"{weekStart.Year}년 {weekStart.Month}월 {weekStart.Day}일 – {weekEnd.Day}일"
                : $"{weekStart.Year}년 {weekStart.Month}월 {weekStart.Day}일 – {weekEnd.Month}월 {weekEnd.Day}일",
            "year" => $"{_selected.Year}년",
            _ => $"{_selected.Year}년 {_selected.Month}월",
        };
        CalHint = CalMode switch
        {
            "week" => "위에서 날짜를 누르면 그 날 일정이 보여요",
            "year" => "날짜를 누르면 그 달로 이동해요",
            _ => "날짜를 누르면 그 날 일정이 보여요",
        };
        var day = On(_selected);
        DayTasks = day.Where(o => Match(o.Item)).Select(Row).ToList();
        SelectedLong = Fmt.Long(_selected);
        SelectedStat = Fmt.Stat(day.Count, day.Count(o => o.Done));

        Raise(string.Empty); // 화면용 값이 한꺼번에 바뀌므로 전체 갱신
    }
}
