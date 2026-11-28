using System.Windows.Input;
using TodoSpirits.Windows.Models;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>일정 패널에서 고르는 반복 요일·날짜 한 칸.</summary>
public sealed record DayChip(int Value, string Label, bool IsOn, bool IsBlank = false);

/// <summary>알림 패널 위쪽의 분류 탭 하나.</summary>
public sealed record NotiTab(string Key, string Label, bool IsOn, bool HasNew);

/// <summary>알림 패널의 한 줄.</summary>
public sealed class NotiRow(NotiItem item, DateTime now)
{
    public NotiItem Item { get; } = item;
    public string Title => Item.Title;
    public string Body => Item.Body;
    public bool Unread => !Item.Read;
    public string Ago
    {
        get
        {
            var span = now - Item.When;
            return span.TotalMinutes < 1 ? "방금" : span.TotalMinutes < 60 ? $"{(int)span.TotalMinutes}분 전"
                : span.TotalHours < 24 ? $"{(int)span.TotalHours}시간 전" : $"{(int)span.TotalDays}일 전";
        }
    }
}

/// <summary>반복·알림 편집, 알림 울리기, 알림 패널, 단축키 안내, 트레이 설정.</summary>
public sealed partial class MainViewModel
{
    /// <summary>알림이 울릴 때 (UI 스레드). 창 쪽에서 Windows 알림을 띄운다. (제목, 내용, 알림 id)</summary>
    public event Action<string, string, Guid>? AlarmFired;

    private void InitAlarms()
    {
        ToggleRepeatDayCommand = new RelayCommand(p =>
        {
            int v = ((DayChip)p!).Value;
            // 아무것도 고르지 않은 상태는 "시작 날짜를 따른다"는 뜻이라, 처음 누를 때 그 기본값을 실제 선택으로 바꿔 둔다
            if (_editRepeatDays.Count == 0) _editRepeatDays.Add(_editRepeat == Recur.Weekly ? (int)EditDateOnly.DayOfWeek : EditDateOnly.Day);
            if (!_editRepeatDays.Remove(v)) _editRepeatDays.Add(v);
            ApplyEdit(); RaiseRepeat();
        });
        ToggleNotiCommand = new RelayCommand(_ => { NotiOpen = !NotiOpen; });
        ReadAllNotiCommand = new RelayCommand(_ =>
        {
            if (IsSignedIn) { _ = MarkNoticesRead(); return; }
            foreach (var notice in _data.Notifications) notice.Read = true;
            Persist();
            RaiseNoti();
        }, _ => !_serverBusy && UnreadCount > 0);
        ClearNotiCommand = new RelayCommand(_ =>
        {
            var removed = _data.Notifications.ToList();
            if (removed.Count == 0) return;
            foreach (var notice in removed)
                if (notice.ServerId is { } id) _hiddenServerNotices.Add(id);
            _data.Notifications.Clear(); Persist(); RaiseNoti();
            ShowToast(IsSignedIn ? "이 화면에서 알림을 모두 숨겼어요" : "알림을 모두 지웠어요", () =>
            {
                foreach (var notice in removed)
                {
                    if (notice.ServerId is { } id) _hiddenServerNotices.Remove(id);
                    if (!_data.Notifications.Any(n => n.Id == notice.Id)) _data.Notifications.Add(notice);
                }
                Persist();
                RaiseNoti();
            });
        }, _ => _data.Notifications.Count > 0);
        DeleteNotiCommand = new RelayCommand(p =>
        {
            var notice = ((NotiRow)p!).Item;
            if (notice.ServerId is { } id) _hiddenServerNotices.Add(id);
            _data.Notifications.Remove(notice);
            Persist();
            RaiseNoti();
        });
        OpenNotiCommand = new RelayCommand(p => OpenNotification(((NotiRow)p!).Item.Id));
        ToggleHelpCommand = new RelayCommand(_ => HelpOpen = !HelpOpen);
        SetNotiFilterCommand = new RelayCommand(p => { _notiFilter = (string)p!; RaiseNoti(); });
        CloseNotiBannerCommand = new RelayCommand(_ => { _notiBannerOff = true; RaiseNoti(); });
        OpenNotiSettingsCommand = new RelayCommand(_ => { NotiOpen = false; NavigateCommand.Execute("settings"); SettingsSection = "notify"; });
        CheckAlarms();
    }

    // ---------- 일정 패널: 반복 ----------
    public ICommand ToggleRepeatDayCommand { get; private set; } = null!;
    public IReadOnlyList<string> RepeatOptions => Recur.Options;
    public IReadOnlyList<string> AlarmOptions => IsSignedIn ? Alarm.Options.Where(option => option != "정각").ToArray() : Alarm.Options;

    private string _editRepeat = Recur.None;
    public string EditRepeat
    {
        get => _editRepeat;
        set
        {
            if (value is null || !Set(ref _editRepeat, value)) return;
            if (!_loading) { _editRepeatDays = []; if (value == Recur.Monthly) _editCalMonth = FirstOfMonth(EditDateOnly); }
            ApplyEdit(); RaiseRepeat(); Raise(nameof(EditCalTitle));
        }
    }
    private List<int> _editRepeatDays = [];

    private string _editAlarm = Alarm.None;
    public string EditAlarm { get => _editAlarm; set { if (value is not null && Set(ref _editAlarm, value)) ApplyEdit(); } }
    public string EditAlarmText => EditHasTime ? _editAlarm : "시간을 정하면 받을 수 있어요";

    public bool EditIsWeekly => _editRepeat == Recur.Weekly;
    public bool EditIsMonthly => _editRepeat == Recur.Monthly;
    public bool EditIsRepeating => _editRepeat != Recur.None;
    public string EditDateLabel => EditIsRepeating ? "시작 날짜" : "날짜";

    public IReadOnlyList<DayChip> WeekdayChips
    {
        get
        {
            var chosen = _editRepeatDays.Count == 0 ? [(int)EditDateOnly.DayOfWeek] : _editRepeatDays;
            return Enumerable.Range(0, 7).Select(i => new DayChip(i, "일월화수목금토"[i].ToString(), chosen.Contains(i))).ToList();
        }
    }
    public IReadOnlyList<DayChip> MonthDayChips
    {
        get
        {
            // 달력 모양으로 고른다: 지금 보는 달의 요일 자리에 맞춰 빈칸을 두고 1~말일을 놓는다
            var chosen = _editRepeatDays.Count == 0 ? [EditDateOnly.Day] : _editRepeatDays;
            return Enumerable.Range(0, (int)_editCalMonth.DayOfWeek).Select(_ => new DayChip(0, "", false, true))
                .Concat(Enumerable.Range(1, DateTime.DaysInMonth(_editCalMonth.Year, _editCalMonth.Month)).Select(i => new DayChip(i, i.ToString(), chosen.Contains(i)))).ToList();
        }
    }
    public string EditRepeatSummary => _editRepeat switch
    {
        Recur.None => "없음",
        _ => Recur.Summary(new TodoItem { Date = EditDateOnly, Repeat = _editRepeat, RepeatDays = _editRepeatDays }),
    };

    private void RaiseRepeat()
    {
        Raise(nameof(EditIsWeekly)); Raise(nameof(EditIsMonthly)); Raise(nameof(EditIsRepeating)); Raise(nameof(EditDateLabel));
        Raise(nameof(WeekdayChips)); Raise(nameof(MonthDayChips)); Raise(nameof(EditRepeatSummary));
    }

    // ---------- 알림 울리기 ----------
    /// <summary>
    /// 오늘 일정 중 알림 시각이 된 것을 울린다. 30초마다, 그리고 앱을 켤 때 확인한다.
    /// 앱이 꺼져 있던 사이에 지난 알림은 일정 시각 뒤 15분까지만 뒤늦게 울린다.
    /// </summary>
    private void CheckAlarms()
    {
        var now = DateTime.Now;
        var today = DateOnly.FromDateTime(now);
        bool changed = false;
        foreach (var occurrence in On(today))
        {
            var task = occurrence.Item;
            if (task.Time is not { } time || Alarm.MinutesBefore(task.Alarm) is not { } before) continue;
            if (occurrence.Done) continue;
            var at = today.ToDateTime(time);
            if (now < at.AddMinutes(-before) || now > at.AddMinutes(15)) continue;
            string key = $"{task.Id}|{today:yyyy-MM-dd}";
            if (_data.FiredAlarms.Contains(key)) continue;
            _data.FiredAlarms.Add(key);
            changed = true;

            int left = (int)Math.Ceiling((at - now).TotalMinutes);
            string body = left > 0 ? $"{Fmt.Time(time)} · {left}분 뒤에 시작해요" : $"{Fmt.Time(time)} · 지금 시작할 시간이에요";
            var noti = new NotiItem { When = now, Title = task.Title, Body = body, TaskId = task.Id, TaskDate = today };
            _data.Notifications.Insert(0, noti);
            if (_data.Notifications.Count > 100) _data.Notifications.RemoveRange(100, _data.Notifications.Count - 100);

            // 설정 › 알림: 리마인드를 끄면 Windows 알림은 띄우지 않고 알림 패널에만 남긴다. 야간(22~8시)도 같다.
            bool night = now.Hour >= 22 || now.Hour < 8;
            if (_data.NotifyRemind && (!night || _data.NotifyNight)) AlarmFired?.Invoke(task.Title, body, noti.Id);
            ShowToast($"알림 · {task.Title} ({body})", null);
        }
        // 지난 날의 기록은 지운다
        int removed = _data.FiredAlarms.RemoveAll(k => !k.EndsWith(today.ToString("yyyy-MM-dd")));
        if (changed || removed > 0) { Persist(); RaiseNoti(); }
    }

    // ---------- 알림 패널 ----------
    public ICommand ToggleNotiCommand { get; private set; } = null!;
    public ICommand ReadAllNotiCommand { get; private set; } = null!;
    public ICommand ClearNotiCommand { get; private set; } = null!;
    public ICommand DeleteNotiCommand { get; private set; } = null!;
    public ICommand OpenNotiCommand { get; private set; } = null!;

    private bool _notiOpen;
    public bool NotiOpen
    {
        get => _notiOpen;
        set
        {
            if (!Set(ref _notiOpen, value)) return;
            if (value)
            {
                NavPeek = false;
                if (CanUseAccount) _ = RunServer(() => LoadNotices(), "알림을 불러오지 못했어요");
            }
            RaiseNoti();
        }
    }
    public IReadOnlyList<NotiRow> Notifications { get { var now = DateTime.Now; return _data.Notifications.OrderByDescending(n => n.When).Select(n => new NotiRow(n, now)).ToList(); } }

    // 분류 탭: 지금 앱이 만드는 알림은 일정 알림(시스템)뿐이다. 이벤트 · 정령의숲은 자리만 있고 비어 있다.
    public ICommand SetNotiFilterCommand { get; private set; } = null!;
    public ICommand CloseNotiBannerCommand { get; private set; } = null!;
    public ICommand OpenNotiSettingsCommand { get; private set; } = null!;
    private string _notiFilter = "all";
    private bool _notiBannerOff;
    public IReadOnlyList<NotiTab> NotiTabs =>
    [
        new("all", "전체", _notiFilter == "all", false),
        new("sys", "시스템", _notiFilter == "sys", HasUnreadCategory("sys")),
        new("evt", "이벤트", _notiFilter == "evt", HasUnreadCategory("evt")),
        new("forest", "정령의숲", _notiFilter == "forest", HasUnreadCategory("forest")),
    ];
    private bool HasUnreadCategory(string key) => _data.Notifications.Any(n => !n.Read && n.Category == key);
    private IEnumerable<NotiRow> FilteredNotis => _notiFilter == "all" ? Notifications : Notifications.Where(n => n.Item.Category == _notiFilter);
    public IReadOnlyList<NotiRow> NewNotis => FilteredNotis.Where(n => n.Unread).ToList();
    public IReadOnlyList<NotiRow> OldNotis => FilteredNotis.Where(n => !n.Unread).ToList();
    public bool HasNewNotis => NewNotis.Count > 0;
    public bool HasOldNotis => OldNotis.Count > 0;
    public bool NotiListEmpty => !HasNewNotis && !HasOldNotis;
    public string NotiEmptyText => _notiFilter switch { "sys" => "시스템 알림이 없어요", "evt" => "이벤트 알림이 없어요", "forest" => "정령의숲 알림이 없어요", _ => "알림이 없어요" };
    /// <summary>리마인드 알림을 꺼 둔 사람에게만 보이는 안내 띠. 닫으면 앱을 끌 때까지 다시 뜨지 않는다.</summary>
    public bool NotiBannerOn => !_notiBannerOff && !_data.NotifyRemind;
    public int UnreadCount => _data.Notifications.Count(n => !n.Read);
    public bool HasUnread => UnreadCount > 0;
    public string UnreadBadge => UnreadCount > 99 ? "99+" : UnreadCount.ToString();
    public bool NotiEmpty => _data.Notifications.Count == 0;
    public bool HasNotis => _data.Notifications.Count > 0;

    private void RaiseNoti()
    {
        Raise(nameof(Notifications)); Raise(nameof(UnreadCount)); Raise(nameof(HasUnread)); Raise(nameof(UnreadBadge)); Raise(nameof(NotiEmpty));
        Raise(nameof(NotiTabs)); Raise(nameof(NewNotis)); Raise(nameof(OldNotis)); Raise(nameof(HasNewNotis)); Raise(nameof(HasOldNotis));
        Raise(nameof(NotiListEmpty)); Raise(nameof(NotiEmptyText)); Raise(nameof(NotiBannerOn)); Raise(nameof(HasNotis));
        CommandManager.InvalidateRequerySuggested();
    }

    /// <summary>알림을 누르면 읽음으로 바꾸고 그 일정을 연다.</summary>
    public void OpenNotification(Guid notiId)
    {
        var noti = _data.Notifications.FirstOrDefault(n => n.Id == notiId);
        if (noti is null) return;
        if (noti.ServerId is { } id)
        {
            if (!noti.Read) _ = MarkNoticesRead(id);
            return;
        }
        noti.Read = true; Persist();
        var task = IsSignedIn
            ? _data.Tasks.FirstOrDefault(t => t.Id == noti.TaskId && t.Date == noti.TaskDate)
            : _data.Tasks.FirstOrDefault(t => t.Id == noti.TaskId);
        NotiOpen = false;
        if (task is null) { ShowToast("이미 지운 일정이에요", null); RaiseNoti(); return; }
        Page = "home";
        var date = Recur.Occurs(task, noti.TaskDate) ? noti.TaskDate : task.Date;
        BeginEdit(new TaskRow(new Occ(task, date), Today, TimeOnly.FromDateTime(DateTime.Now)));
        RaiseNoti();
    }

    // ---------- 단축키 안내 ----------
    public ICommand ToggleHelpCommand { get; private set; } = null!;
    private bool _helpOpen;
    public bool HelpOpen { get => _helpOpen; set => Set(ref _helpOpen, value); }

    // ---------- 트레이 ----------
    public bool CloseToTray { get => _data.CloseToTray; set { if (_data.CloseToTray == value) return; _data.CloseToTray = value; Persist(); Raise(); } }
}
