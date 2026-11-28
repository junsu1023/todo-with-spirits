using System.Windows.Input;
using TodoSpirits.Windows.Models;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>일정 패널에서 고르는 반복 요일·날짜 한 칸.</summary>
public sealed record DayChip(int Value, string Label, bool IsOn);

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
            if (!_editRepeatDays.Remove(v)) _editRepeatDays.Add(v);
            ApplyEdit(); RaiseRepeat();
        });
        ToggleNotiCommand = new RelayCommand(_ => { NotiOpen = !NotiOpen; });
        ReadAllNotiCommand = new RelayCommand(_ => { foreach (var n in _data.Notifications) n.Read = true; Persist(); RaiseNoti(); }, _ => _data.Notifications.Any(n => !n.Read));
        ClearNotiCommand = new RelayCommand(_ =>
        {
            var removed = _data.Notifications.ToList();
            if (removed.Count == 0) return;
            _data.Notifications.Clear(); Persist(); RaiseNoti();
            ShowToast("알림을 모두 지웠어요", () => { _data.Notifications.AddRange(removed); Persist(); RaiseNoti(); });
        }, _ => _data.Notifications.Count > 0);
        DeleteNotiCommand = new RelayCommand(p => { _data.Notifications.Remove(((NotiRow)p!).Item); Persist(); RaiseNoti(); });
        OpenNotiCommand = new RelayCommand(p => OpenNotification(((NotiRow)p!).Item.Id));
        ToggleHelpCommand = new RelayCommand(_ => HelpOpen = !HelpOpen);
        CheckAlarms();
    }

    // ---------- 일정 패널: 반복 ----------
    public ICommand ToggleRepeatDayCommand { get; private set; } = null!;
    public IReadOnlyList<string> RepeatOptions => Recur.Options;
    public IReadOnlyList<string> AlarmOptions => Alarm.Options;

    private string _editRepeat = Recur.None;
    public string EditRepeat
    {
        get => _editRepeat;
        set { if (value is null || !Set(ref _editRepeat, value)) return; if (!_loading) _editRepeatDays = []; ApplyEdit(); RaiseRepeat(); }
    }
    private List<int> _editRepeatDays = [];

    private string _editAlarm = Alarm.None;
    public string EditAlarm { get => _editAlarm; set { if (value is not null && Set(ref _editAlarm, value)) ApplyEdit(); } }

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
            var chosen = _editRepeatDays.Count == 0 ? [EditDateOnly.Day] : _editRepeatDays;
            return Enumerable.Range(1, 31).Select(i => new DayChip(i, i.ToString(), chosen.Contains(i))).ToList();
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
        foreach (var task in _data.Tasks)
        {
            if (task.Time is not { } time || Alarm.MinutesBefore(task.Alarm) is not { } before) continue;
            if (!Recur.Occurs(task, today) || Recur.IsDone(task, today)) continue;
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
        set { if (!Set(ref _notiOpen, value)) return; if (value) { _editing = null; PanelOpen = false; RaisePanel(); } RaiseNoti(); }
    }
    public IReadOnlyList<NotiRow> Notifications { get { var now = DateTime.Now; return _data.Notifications.Select(n => new NotiRow(n, now)).ToList(); } }
    public int UnreadCount => _data.Notifications.Count(n => !n.Read);
    public bool HasUnread => UnreadCount > 0;
    public string UnreadBadge => UnreadCount > 99 ? "99+" : UnreadCount.ToString();
    public bool NotiEmpty => _data.Notifications.Count == 0;

    private void RaiseNoti()
    {
        Raise(nameof(Notifications)); Raise(nameof(UnreadCount)); Raise(nameof(HasUnread)); Raise(nameof(UnreadBadge)); Raise(nameof(NotiEmpty));
        CommandManager.InvalidateRequerySuggested();
    }

    /// <summary>알림을 누르면 읽음으로 바꾸고 그 일정을 연다.</summary>
    public void OpenNotification(Guid notiId)
    {
        var noti = _data.Notifications.FirstOrDefault(n => n.Id == notiId);
        if (noti is null) return;
        noti.Read = true; Persist();
        var task = _data.Tasks.FirstOrDefault(t => t.Id == noti.TaskId);
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
