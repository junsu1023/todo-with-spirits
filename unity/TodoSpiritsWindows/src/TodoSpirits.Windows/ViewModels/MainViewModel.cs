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
    private readonly AppData _localData;
    private AppData _data;
    private readonly DispatcherTimer _toastTimer = new() { Interval = TimeSpan.FromSeconds(4) };
    private readonly DispatcherTimer _clock = new() { Interval = TimeSpan.FromSeconds(30) };
    private Action? _undo;

    public MainViewModel() : this(new TaskStore()) { }

    public MainViewModel(TaskStore store)
    {
        _store = store;
        _localData = _data = store.Load(out var warning);
        _calMode = _data.CalendarMode is "week" or "month" or "year" ? _data.CalendarMode : "month";
        _isDark = _data.Theme == "dark";
        Today = DateOnly.FromDateTime(DateTime.Now);
        _selected = Today;
        _view = new DateOnly(Today.Year, Today.Month, 1);

        NavigateCommand = new RelayCommand(p => { ClosePanel(); NotiOpen = false; NavPeek = false; Page = (string)p!; });
        SetModeCommand = new RelayCommand(p => SetMode((string)p!));
        SelectDateCommand = new RelayCommand(p => SelectDate(((DayCell)p!).Date));
        YearDayCommand = new RelayCommand(p => { _calMode = "month"; Raise(nameof(CalMode)); SaveMode(); SelectDate(((DayCell)p!).Date); CalMoved?.Invoke(-1); });
        GoMonthCommand = new RelayCommand(p => { var m = (MonthCard)p!; _calMode = "month"; Raise(nameof(CalMode)); SaveMode(); _view = new DateOnly(m.Year, m.Month, 1); Refresh(); CalMoved?.Invoke(-1); });
        PrevCommand = new RelayCommand(_ => Shift(-1));
        NextCommand = new RelayCommand(_ => Shift(1));
        GoTodayCommand = new RelayCommand(_ => SelectDate(Today));
        ToggleThemeCommand = new RelayCommand(_ => IsDark = !IsDark);
        AddCommand = new RelayCommand(p => BeginAdd(p is DayCell c ? c.Date : Page == "calendar" ? SelectedDate : Today));
        OpenCommand = new RelayCommand(p => BeginEdit((TaskRow)p!));
        ToggleCommand = new RelayCommand(p => { var row = (TaskRow)p!; Toggle(row.Item, row.Date); }, _ => !_serverBusy);
        MoveToTodayCommand = new RelayCommand(p => MoveOne(((TaskRow)p!).Item, Today, "오늘 일정으로 옮겼어요"), _ => !_serverBusy);
        MoveAllToTodayCommand = new RelayCommand(_ => MoveAllToToday(), _ => !_serverBusy);
        SaveCommand = new RelayCommand(async _ =>
        {
            if (IsSignedIn) await SaveServerTask();
            else SaveNew();
        }, _ => !_serverBusy && ShowTaskSave && !string.IsNullOrWhiteSpace(EditTitle) && EditTitle.Trim().Length <= 255);
        PickDateForCommand = new RelayCommand(p => { BeginEdit((TaskRow)p!); EditOpen = "date"; });
        DeleteCommand = new RelayCommand(_ => DeleteEditing(), _ => _editing is not null && !_serverBusy);
        ToggleEditingCommand = new RelayCommand(_ => { if (_editing is not null) { Toggle(_editing, _editingDate); Raise(nameof(EditDoneLabel)); } }, _ => _editing is not null && !_serverBusy);
        // Esc: 가장 위에 떠 있는 것부터 하나씩 닫는다
        ClosePanelCommand = new RelayCommand(_ =>
        {
            if (HelpOpen) HelpOpen = false;
            else if (NotiOpen) NotiOpen = false;
            else if (EditOpen is "repeat" or "alarm") EditOpen = "";
            else if (PanelOpen) ClosePanel();
            else if (NavPeek) NavPeek = false;
            else if (SearchOpen) CloseSearchCommand.Execute(null);
        });
        UndoCommand = new RelayCommand(_ => { _undo?.Invoke(); HideToast(); });

        _toastTimer.Tick += (_, _) => HideToast();
        _clock.Tick += (_, _) =>
        {
            var day = DateOnly.FromDateTime(DateTime.Now);
            if (day != Today) { Today = day; Raise(nameof(Today)); }
            Refresh();
            CheckAlarms();
        };
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
    /// <summary>이월 카드의 "다른 날": 상세를 열고 날짜 고르기를 펼친다.</summary>
    public ICommand PickDateForCommand { get; }
    public ICommand DeleteCommand { get; }
    public ICommand ToggleEditingCommand { get; }
    public ICommand ClosePanelCommand { get; }
    public ICommand UndoCommand { get; }

    // ---------- 공통 상태 ----------
    public DateOnly Today { get; private set; }

    private string _page = "home";
    public string Page { get => _page; set { if (Set(ref _page, value)) Refresh(); } }

    private string _calMode;
    public string CalMode { get => _calMode; set => SetMode(value); }

    private static int ModeOrder(string m) => m switch { "week" => 0, "month" => 1, _ => 2 };
    private void SetMode(string mode)
    {
        if (mode == _calMode || mode is not ("week" or "month" or "year")) return;
        int dir = ModeOrder(mode) > ModeOrder(_calMode) ? 1 : -1;
        _calMode = mode;
        SaveMode(); SyncView(); ClosePanel(); Refresh();
        CalMoved?.Invoke(dir);
    }

    /// <summary>캘린더에서 보는 기간이나 보기 방식이 바뀔 때 (1 = 앞으로, -1 = 뒤로). 창 쪽에서 그 방향으로 밀어 넣는다.</summary>
    public event Action<int>? CalMoved;
    /// <summary>고른 날짜가 바뀔 때. 그 날 목록이 옆에서 밀려 들어온다.</summary>
    public event Action<int>? DayChanged;

    private bool _isDark;
    public bool IsDark
    {
        get => _isDark;
        set
        {
            if (_isDark == value) return;
            if (IsSignedIn && !_applyingSystem)
            {
                _ = SaveAccountDisplay(new DisplayThemeRequest(value));
                return;
            }
            Set(ref _isDark, value);
            ThemeService.Apply(value, _data.ContrastMore);
            if (!IsSignedIn) _data.Theme = value ? "dark" : "light";
            if (!_applyingSystem) _data.FollowSystem = false; // 직접 바꾸면 Windows 따라가기를 끈다
            Persist(); Raise(nameof(ThemeLabel)); Raise(nameof(FollowSystem));
        }
    }
    public string ThemeLabel => IsDark ? "라이트 모드로" : "다크 모드로";

    private DateOnly _selected;
    public DateOnly SelectedDate => _selected;
    /// <summary>월간 · 연간에서 보고 있는 달(1일). 이전/다음은 이것만 옮기고 고른 날짜는 그대로 둔다.</summary>
    private DateOnly _view;
    private void SyncView() => _view = new DateOnly(_selected.Year, _selected.Month, 1);

    public string DataPath => _store.FilePath;

    // ---------- 홈 ----------
    public string Greeting { get; private set; } = "";
    public string TodayText { get; private set; } = "";
    public int TodayTotal { get; private set; }
    public int TodayDone { get; private set; }
    public string NextTitle { get; private set; } = "";
    public string NextWhen { get; private set; } = "";
    public bool HasNext => NextTitle.Length > 0;
    public bool NoNext => !HasNext;
    /// <summary>오늘 남은(끝내지 않은) 일정 수. 요약 카드의 큰 숫자.</summary>
    public int TodayLeft => TodayTotal - TodayDone;
    public IReadOnlyList<TaskRow> DoneRows { get; private set; } = [];
    public bool HasDoneRows => DoneRows.Count > 0;
    public string DoneRowsCount => DoneRows.Count.ToString();
    public IReadOnlyList<TaskRow> Carry { get; private set; } = [];
    public IReadOnlyList<TaskRow> Timed { get; private set; } = [];
    public IReadOnlyList<TaskRow> AllDay { get; private set; } = [];
    public bool HasCarry => Carry.Count > 0;
    public bool HasTimed => Timed.Count > 0;
    public bool HasAllDay => AllDay.Count > 0;
    public bool HomeEmpty => TodayTotal == 0 && !HasCarry && !IsFiltering;
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
    /// <summary>월간 오른쪽 패널의 제목: "10월 13일 화요일 · 오늘"</summary>
    public string DayPanelTitle => SelectedLong + (SelectedIsToday ? " · 오늘" : "");
    /// <summary>주간 목록의 머리: "13일 화요일"</summary>
    public string WeekTitle => $"{_selected.Day}일 {Fmt.Dow(_selected)}요일";
    public string WeekStat { get; private set; } = "";
    public string DayEmptyText { get; private set; } = "일정 없음";

    private bool _calHideDone;
    /// <summary>월간 패널의 "완료 제외".</summary>
    public bool CalHideDone { get => _calHideDone; set { if (Set(ref _calHideDone, value)) Refresh(); } }

    public IReadOnlyList<string> DaySortOptions { get; } = ["시간 순", "중요 순", "완료 순"];
    private string _daySort = "시간 순";
    public string DaySort { get => _daySort; set { if (value is not null && Set(ref _daySort, value)) { DaySortMenuOpen = false; Refresh(); } } }
    private bool _daySortMenuOpen;
    public bool DaySortMenuOpen { get => _daySortMenuOpen; set => Set(ref _daySortMenuOpen, value); }
    public ICommand PickDaySortCommand => new RelayCommand(p => DaySort = (string)p!);
    public ICommand ToggleDaySortMenuCommand => new RelayCommand(_ => DaySortMenuOpen = !DaySortMenuOpen);
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
    public string PanelTitle => _editing is null ? "일정 추가"
        : !Recur.IsRepeating(_editing) && !_editing.Done && _editing.Date < Today ? "어제 하지 못한 일" : "일정 상세";
    public string EditDoneLabel => _editing is not null && Recur.IsDone(_editing, _editingDate) ? "완료 취소" : "완료";

    private string _editTitle = "";
    public string EditTitle { get => _editTitle; set { if (Set(ref _editTitle, value)) { ApplyEdit(); CommandManager.InvalidateRequerySuggested(); } } }

    private bool _editImportant;
    public bool EditImportant { get => _editImportant; set { if (Set(ref _editImportant, value)) ApplyEdit(); } }

    private DateTime? _editDate;
    public DateTime? EditDate { get => _editDate; set { if (value is not null && Set(ref _editDate, value)) { ApplyEdit(); RaiseEditDerived(); } } }

    private bool _editHasTime;
    public bool EditHasTime { get => _editHasTime; set
        {
            if (!Set(ref _editHasTime, value)) return;
            // 시간을 켜면 바로 고를 수 있게 휠을 펼치고, 끄면 접는다
            if (!_loading) EditOpen = value ? "time" : EditOpen == "time" ? "" : EditOpen;
            ApplyEdit(); RaiseEditDerived(); Raise(nameof(EditTimeOpen));
        }
    }

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
        int dir = d > _selected ? 1 : d < _selected ? -1 : 0;
        bool viewMoves = CalMode == "week" ? d.AddDays(-(int)d.DayOfWeek) != _selected.AddDays(-(int)_selected.DayOfWeek) : (d.Year, d.Month) != (_view.Year, _view.Month);
        _selected = d;
        SyncView();
        Refresh();
        if (dir != 0) { DayChanged?.Invoke(dir); if (viewMoves) CalMoved?.Invoke(dir); }
    }

    /// <summary>이전 · 다음. 주간은 고른 날짜가 한 주 옮겨 가고, 월간 · 연간은 보는 달(해)만 바뀐다.</summary>
    private void Shift(int n)
    {
        if (CalMode == "week") { _selected = _selected.AddDays(7 * n); SyncView(); }
        else _view = CalMode == "year" ? _view.AddYears(n) : _view.AddMonths(n);
        Refresh();
        CalMoved?.Invoke(n);
        if (CalMode == "week") DayChanged?.Invoke(n);
    }

    /// <summary>방향키: 고른 날짜를 며칠 옮긴다.</summary>
    public void ShiftDay(int days) => SelectDate(_selected.AddDays(days));
    public void ShiftPeriod(int n) => Shift(n);

    private static DateOnly SafeDate(int y, int m, int d) => new(y, m, Math.Min(d, DateTime.DaysInMonth(y, m)));

    private void BeginAdd(DateOnly date)
    {
        NotiOpen = false;
        _editing = null;
        _loading = true;
        EditTitle = ""; EditImportant = false; EditDate = date.ToDateTime(TimeOnly.MinValue); EditHasTime = false;
        // 다음 정각을 기본 시간으로. 밤 11시대에는 날짜가 넘어가지 않게 11시로 둔다
        EditHour = Math.Min(DateTime.Now.Hour + 1, 23); EditMinuteIndex = 0;
        EditRepeat = Recur.None; _editRepeatDays = []; EditAlarm = Alarm.None;
        _loading = false;
        EditOpen = ""; RaiseEditDerived(); RaiseRepeat();
        bool swap = PanelOpen;
        PanelOpen = true;
        RaisePanel();
        Refresh();
        if (swap) PanelSwapped?.Invoke();
    }

    private void BeginEdit(TaskRow row, bool fetchServer = true)
    {
        var item = row.Item;
        NotiOpen = false;
        _editing = item;
        _editingDate = row.Date;
        _loading = true;
        EditRepeat = item.Repeat; _editRepeatDays = [.. item.RepeatDays]; EditAlarm = item.Alarm;
        EditTitle = item.Title; EditImportant = item.Important; EditDate = (item.ServerStartDate ?? item.Date).ToDateTime(TimeOnly.MinValue);
        EditHasTime = item.Time is not null;
        EditHour = item.Time?.Hour ?? 9; EditMinuteIndex = (item.Time?.Minute ?? 0) / 5;
        _loading = false;
        EditOpen = ""; RaiseEditDerived(); RaiseRepeat();
        bool swap = PanelOpen;
        PanelOpen = true;
        RaisePanel();
        Refresh(); // 고른 줄 표시를 옮긴다
        if (swap && fetchServer) PanelSwapped?.Invoke();
        if (fetchServer && CanUseAccount && item.ServerId is not null) _ = LoadServerTaskDetail(item);
    }

    /// <summary>패널이 열린 채 다른 일정으로 바뀔 때: 창 쪽에서 내용을 옆으로 밀어 넣는다.</summary>
    public event Action? PanelSwapped;

    private void RaisePanel()
    {
        Raise(nameof(IsNew)); Raise(nameof(IsExisting)); Raise(nameof(PanelTitle)); Raise(nameof(EditDoneLabel));
        Raise(nameof(ShowTaskSave));
        Raise(nameof(TaskSaveLabel));
        Raise(nameof(TaskEditHelp));
        CommandManager.InvalidateRequerySuggested();
    }

    private TimeOnly? EditTime => EditHasTime ? new TimeOnly(EditHour, EditMinuteIndex * 5) : null;

    /// <summary>기존 일정은 고치는 즉시 저장한다. 제목을 비우면 제목만 그대로 둔다.</summary>
    private void ApplyEdit()
    {
        if (_loading || _editing is null || IsSignedIn) return;
        bool wasRepeating = Recur.IsRepeating(_editing);
        var (oldTime, oldAlarm) = (_editing.Time, _editing.Alarm);
        if (!string.IsNullOrWhiteSpace(EditTitle)) _editing.Title = EditTitle.Trim();
        _editing.Important = EditImportant;
        _editing.Date = DateOnly.FromDateTime(EditDate!.Value);
        _editing.Time = EditTime;
        _editing.Repeat = EditRepeat; _editing.RepeatDays = [.. _editRepeatDays]; _editing.Alarm = EditTime is null ? Alarm.None : EditAlarm;
        bool isRepeating = Recur.IsRepeating(_editing);
        if (wasRepeating && !isRepeating)
        {
            // 반복을 끄면 지금 보고 있던 회차 하루만 남긴다 (시작 날짜로 돌아가 "어제 하지 못한 일"에 뜨지 않게)
            _editing.Done = _editing.DoneDates.Contains(_editingDate);
            _editing.DoneDates.Clear();
            _editing.Date = _editingDate;
            _editDate = _editingDate.ToDateTime(TimeOnly.MinValue);
            RaiseEditDerived();
        }
        else if (!wasRepeating && isRepeating && _editing.Done)
        {
            // 끝낸 일정을 반복으로 바꾸면 그 날 회차만 완료로 남긴다
            _editing.Done = false;
            if (!_editing.DoneDates.Contains(_editing.Date)) _editing.DoneDates.Add(_editing.Date);
        }
        if (!isRepeating) _editingDate = _editing.Date;
        // 시간이나 알림을 바꾸면 오늘 이미 울린 기록을 지워, 새 시각에 다시 울리게 한다
        if (oldTime != _editing.Time || oldAlarm != _editing.Alarm)
        {
            var id = _editing.Id.ToString();
            _data.FiredAlarms.RemoveAll(k => k.StartsWith(id, StringComparison.Ordinal));
        }
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
        if (Page == "calendar") { _selected = item.Date; SyncView(); }
        Persist();
        // 추가한 뒤에는 패널을 닫지 않고 방금 만든 일정의 상세로 바꾼다 (시안과 같음)
        _justNew = item.Id;
        BeginEdit(new TaskRow(new Occ(item, item.Date), Today, TimeOnly.FromDateTime(DateTime.Now)));
        _justNew = null;
        ShowToast(item.Date == Today ? "일정을 추가했어요" : $"{Fmt.Short(item.Date)}에 일정을 추가했어요", null);
    }

    private void DeleteEditing()
    {
        if (_editing is null) return;
        DeleteItem(_editing);
    }

    private void DeleteItem(TodoItem item)
    {
        if (IsSignedIn) { _ = DeleteServerTask(item); return; }
        var index = _data.Tasks.IndexOf(item);
        if (index < 0) return;
        _data.Tasks.Remove(item);
        if (_editing == item) ClosePanel();
        Commit();
        ShowToast(Recur.IsRepeating(item) ? $"\"{item.Title}\" 반복 일정을 모두 삭제했어요" : $"\"{item.Title}\" 삭제했어요",
            () => { _data.Tasks.Insert(Math.Min(index, _data.Tasks.Count), item); Commit(); });
    }

    /// <summary>완료 표시를 바꾼다. 반복 일정은 그 날짜의 회차만 바뀐다.</summary>
    private void Toggle(TodoItem item, DateOnly date)
    {
        if (IsSignedIn) { _ = ToggleServerTask(item, date); return; }
        bool done = !Recur.IsDone(item, date);
        Recur.SetDone(item, date, done);
        _justDone = done ? (item.Id, date) : null;
        Commit();
        _justDone = null;
        Raise(nameof(EditDoneLabel)); Raise(nameof(PanelTitle));
        if (!done) return;
        int left = On(Today).Count(o => !o.Done);
        ShowToast(date != Today ? "잘했어요!" : left > 0 ? $"잘했어요! 이제 {left}개 남았어요" : "오늘 일정을 모두 끝냈어요!", null);
        SummaryBumped?.Invoke();
    }
    private (Guid, DateOnly)? _justDone;
    private Guid? _justNew;

    /// <summary>오늘 남은 수가 바뀔 때: 창 쪽에서 숫자를 살짝 튀게 한다.</summary>
    public event Action? SummaryBumped;

    private void MoveAllToToday()
    {
        var tasks = CarryTasks().ToList();
        if (IsSignedIn) { _ = MoveServerTasks(tasks, Today); return; }
        var moved = tasks.Select(t => (t, t.Date)).ToList();
        if (moved.Count == 0) return;
        foreach (var (t, _) in moved) t.Date = Today;
        Commit();
        ShowToast($"{moved.Count}개를 오늘 일정으로 옮겼어요", () => { foreach (var (t, from) in moved) t.Date = from; Commit(); });
    }

    private IEnumerable<TodoItem> CarryTasks() => _data.Tasks.Where(t => !t.Done && t.Date < Today &&
        (!IsSignedIn || t.Date >= Today.AddDays(-30)) && !Recur.IsRepeating(t));

    private void MoveOne(TodoItem item, DateOnly date, string message)
    {
        if (IsSignedIn) { _ = MoveServerTasks([item], date); return; }
        var from = item.Date;
        item.Date = date;
        Commit();
        ShowToast(message, () => { item.Date = from; Commit(); });
    }

    private void ClosePanel()
    {
        bool hadSelection = _editing is not null;
        _editing = null;
        PanelOpen = false;
        RaisePanel();
        if (hadSelection) Refresh(); // 고른 줄 표시를 지운다
    }

    private void Commit() { Persist(); Refresh(); }

    private void Persist()
    {
        try
        {
            if (IsSignedIn) CopyDeviceSettings(_data, _localData);
            _store.Save(_localData);
        }
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
        ToastShown?.Invoke();
    }

    /// <summary>토스트가 뜨거나, 떠 있는 채 글이 바뀔 때. 창 쪽에서 다시 튀어 오르게 한다.</summary>
    public event Action? ToastShown;

    private void HideToast() { _toastTimer.Stop(); ToastOpen = false; _undo = null; }

    // ---------- 화면용 목록 다시 만들기 ----------
    private static IOrderedEnumerable<Occ> ByTime(IEnumerable<Occ> list) =>
        list.OrderBy(o => o.Item.Time is null ? 0 : 1).ThenBy(o => o.Item.Time).ThenBy(o => o.Item.Title);

    /// <summary>그 날짜에 나타나는 일정들 (반복 일정의 회차 포함).</summary>
    private List<Occ> On(DateOnly d)
    {
        IEnumerable<TodoItem> items = IsSignedIn ? _serverDays.GetValueOrDefault(d) ?? [] : _data.Tasks.Where(t => Recur.Occurs(t, d));
        return ByTime(items.Select(t => new Occ(t, d))).ToList();
    }

    private DayCell Cell(DateOnly d, bool other = false)
    {
        // 검색 중이면 달력 칸의 개수와 제목도 검색 결과만 센다
        var list = IsSearching ? On(d).Where(o => Match(o.Item)).ToList() : On(d);
        var first = list.FirstOrDefault(o => !o.Done);
        return new DayCell
        {
            Date = d, IsToday = d == Today, IsSelected = d == _selected, IsOther = other, IsPast = d < Today,
            Total = list.Count, DoneCount = list.Count(o => o.Done),
            HasImportant = list.Any(o => o.Item.Important && !o.Done),
            FirstTitle = first.Item?.Title ?? "", FirstImportant = first.Item?.Important ?? false,
            RestImportant = list.Any(o => o.Item.Important && !o.Done && o.Item != first.Item),
        };
    }

    public void Refresh()
    {
        var now = TimeOnly.FromDateTime(DateTime.Now);
        TaskRow Row(Occ o) => new(o, Today, now, _justDone == (o.Item.Id, o.Date), _justNew == o.Item.Id,
            PanelOpen && _editing?.Id == o.Item.Id && _editingDate == o.Date)
        {
            // 설정 › 디스플레이 › 플랜 디데이 표시: 앞날의 일정에 "D-3"처럼 남은 날을 붙인다
            DdayText = _data.ShowDday && o.Date > Today ? $"D-{o.Date.DayNumber - Today.DayNumber}" : "",
        };

        // 홈
        var today = On(Today);
        Greeting = now.Hour switch { < 6 => "늦은 밤이에요", < 12 => "좋은 아침이에요", < 18 => "좋은 오후예요", _ => "편안한 저녁이에요" };
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
        Carry = CarryTasks().Where(ShowOnHome)
            .OrderBy(t => t.Date).ThenBy(t => t.Time).Select(t => Row(new Occ(t, t.Date))).ToList();
        // 시안의 네 구역: 어제 하지 못한 일 / 시간 일정 / 할 일 / 완료. 앞의 셋은 끝내지 않은 것만.
        var shown = today.Where(o => ShowOnHome(o.Item)).ToList();
        Timed = shown.Where(o => !o.Done && o.Item.Time is not null).Select(Row).ToList();
        AllDay = shown.Where(o => !o.Done && o.Item.Time is null).Select(Row).ToList();
        DoneRows = _homeHideDone ? [] : shown.Where(o => o.Done).Select(Row).ToList();
        ImportantLeft = today.Count(o => o.Item.Important && !o.Done);

        // 캘린더
        var weekStart = _selected.AddDays(-(int)_selected.DayOfWeek);
        var weekEnd = weekStart.AddDays(6);
        WeekCells = Enumerable.Range(0, 7).Select(i => Cell(weekStart.AddDays(i))).ToList();

        var first = _view;
        var gridStart = first.AddDays(-(int)first.DayOfWeek);
        MonthRows = (int)Math.Ceiling(((int)first.DayOfWeek + DateTime.DaysInMonth(first.Year, first.Month)) / 7.0);
        MonthCells = Enumerable.Range(0, MonthRows * 7).Select(i => gridStart.AddDays(i)).Select(d => Cell(d, d.Month != first.Month)).ToList();

        // 연간은 365일을 모두 계산하므로 연간 보기일 때만 만든다
        if (CalMode == "year" && Page == "calendar")
        {
            YearCards = Enumerable.Range(1, 12).Select(m =>
            {
                var f = new DateOnly(_view.Year, m, 1);
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
            "year" => $"{_view.Year}년",
            _ => $"{_view.Year}년 {_view.Month}월",
        };
        CalHint = CalMode switch
        {
            "week" => "날짜를 누르면 그 날 일정이 보여요 · ← → 하루, PageUp/Down 한 주",
            "year" => "날짜를 누르면 그 달로 이동해요",
            _ => "날짜를 누르면 그 날 일정이 보여요 · 두 번 누르면 추가",
        };
        var day = On(_selected).Where(o => Match(o.Item)).ToList();
        IEnumerable<Occ> dayShown = day.Where(o => !(_calHideDone && o.Done));
        dayShown = _daySort switch
        {
            "중요 순" => dayShown.OrderByDescending(o => o.Item.Important).ThenBy(o => o.Done),
            "완료 순" => dayShown.OrderByDescending(o => o.Done),
            _ => dayShown, // On() 이 이미 시간 순
        };
        DayTasks = dayShown.Select(Row).ToList();
        SelectedLong = Fmt.Long(_selected);
        int dayDone = day.Count(o => o.Done);
        SelectedStat = Fmt.Stat(day.Count, dayDone);
        WeekStat = day.Count == 0 ? "" : dayDone > 0 ? $"{day.Count}개 중 {dayDone}개 완료" : $"{day.Count}개";
        DayEmptyText = day.Count > 0 ? "완료한 일정만 있어요" : IsSearching ? "검색한 일정이 없어요" : "일정 없음";

        Raise(string.Empty); // 화면용 값이 한꺼번에 바뀌므로 전체 갱신
        QueueCalendarLoad();
    }
}
