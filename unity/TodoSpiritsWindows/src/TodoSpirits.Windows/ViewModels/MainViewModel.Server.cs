using System.IO;
using System.Net;
using System.Net.Http;
using System.Text.Json;
using System.Windows.Input;
using TodoSpirits.Windows.Models;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

public sealed partial class MainViewModel
{
    private SpiritsApi? _api;
    private ServerProfile? _account;
    private bool _serverBusy;
    private string _serverStatus = "이 PC에 저장";
    private CalendarWindow? _calendarRequested;
    private DateTimeOffset _lastCalendarLoad;
    private Dictionary<DateOnly, TodoItem[]> _serverDays = [];

    public bool IsSignedIn => _account is not null;
    public bool CanSignIn => !IsSignedIn && !_serverBusy;
    public bool CanUseAccount => IsSignedIn && !_serverBusy;
    public string AccountText => IsSignedIn ? _account!.Email ?? "로그인됨" : "로그인 전";
    public string ServerStatus { get => _serverStatus; private set => Set(ref _serverStatus, value); }
    public ICommand SyncCommand => field ??= new RelayCommand(async _ => await SyncAccount(), _ => CanUseAccount);
    public ICommand SignOutCommand => field ??= new RelayCommand(async _ => await SignOut(), _ => CanUseAccount);

    private void ServerBusy(bool busy)
    {
        _serverBusy = busy;
        Raise(nameof(CanSignIn));
        Raise(nameof(CanUseAccount));
        Raise(nameof(CanEditSettings));
        CommandManager.InvalidateRequerySuggested();
    }

    public async Task SignIn(string email, string password)
    {
        if (!CanSignIn || string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password)) return;
        await RunServer(async () =>
        {
            var api = new SpiritsApi();
            try
            {
                await api.SignIn(email.Trim(), password);
                var account = await api.Get<ServerProfile>("api/user/me");
                if (account.UserId <= 0) throw new InvalidDataException("서버에서 계정을 확인하지 못했어요.");
                _api = api;
                _account = account;
            }
            finally
            {
                if (_api != api) api.Dispose();
            }
            ClosePanel();
            _data = new AppData { Theme = _localData.Theme, Nickname = _account.Nickname ?? "" };
            CopyDeviceSettings(_localData, _data);
            SetDexRows([]);
            RaiseAccount();
            await SyncAccountData(refreshProfile: false);
        }, "로그인하지 못했어요");
    }

    private Task SignOut() => RunServer(async () =>
    {
        await _api!.SignOut();
        LeaveAccount();
        ShowToast("로그아웃했어요", null);
    }, "로그아웃하지 못했어요");

    public Task WithdrawAccount() => RunServer(async () =>
    {
        await _api!.Delete("api/user/me");
        LeaveAccount();
        SettingsSection = "profile";
        ShowToast("회원탈퇴를 완료했어요", null);
    }, "회원탈퇴하지 못했어요");

    private void LeaveAccount()
    {
        ClosePanel();
        HideToast();
        _api?.Dispose();
        _api = null;
        _account = null;
        SubscriptionText = null;
        _calendarRequested = null;
        _serverDays = [];
        _nextNotificationCursor = null;
        _hiddenServerNotices.Clear();
        _data = _localData;
        ApplyAccountTheme();
        SetDexRows(SpiritCatalog.Build(_data.RepresentativeId, _data.RepresentativeId));
        ServerStatus = "이 PC에 저장";
        RaiseAccount();
    }

    private void RaiseAccount()
    {
        _nickDraft = null;
        Raise(nameof(IsSignedIn));
        Raise(nameof(CanSignIn));
        Raise(nameof(CanUseAccount));
        Raise(nameof(AccountText));
        Raise(nameof(ShowTaskSave));
        Raise(nameof(AlarmOptions));
        Raise(nameof(DexNote));
        Raise(nameof(NicknameLimit));
        Raise(nameof(MoreNotifications));
        RaiseAccountSettings();
        RaiseNoti();
        Refresh();
    }

    private static void CopyDeviceSettings(AppData from, AppData to)
    {
        to.CalendarMode = from.CalendarMode;
        to.FollowSystem = from.FollowSystem;
        to.ContrastMore = from.ContrastMore;
        to.SpiritEnabled = from.SpiritEnabled;
        to.NavHidden = from.NavHidden;
        to.CloseToTray = from.CloseToTray;
    }

    public void DisposeServer() => _api?.Dispose();

    public void RefreshServerOnActivation()
    {
        if (CanUseAccount && DateTimeOffset.UtcNow - _lastCalendarLoad > TimeSpan.FromSeconds(15))
            _ = RunServer(LoadCalendar, "일정을 불러오지 못했어요");
    }

    private async Task RunServer(Func<Task> work, string failure)
    {
        if (_serverBusy) return;
        ServerBusy(true);
        ServerStatus = "서버에 연결 중…";
        try
        {
            await work();
            if (IsSignedIn && ServerStatus == "서버에 연결 중…") ServerStatus = "계정에 연결됨";
        }
        catch (SessionExpiredException)
        {
            LeaveAccount();
            ShowToast("로그인이 만료됐어요. 다시 로그인해 주세요.", null);
        }
        catch (Exception ex) when (IsServerError(ex))
        {
            ServerStatus = failure;
            ShowToast(failure + ". " + ServerErrorMessage(ex), null);
        }
        finally
        {
            ServerBusy(false);
            QueueCalendarLoad();
        }
    }

    private static bool IsServerError(Exception ex) => ex is HttpRequestException or OperationCanceledException
        or SpiritsApiException or JsonException or IOException or InvalidDataException or InvalidOperationException;

    private static string ServerErrorMessage(Exception ex) => ex switch
    {
        SpiritsApiException { Code: "DUPLICATE_EMAIL" } => "이미 가입된 이메일이에요.",
        SpiritsApiException { Code: "EMAIL_NOT_VERIFIED" } => "이메일 인증번호를 다시 받아 주세요.",
        SpiritsApiException { Code: "EXPIRED_TOKEN" } => "인증 유효 시간이 지났어요.",
        SpiritsApiException { Status: HttpStatusCode.Unauthorized } => "계정 정보나 로그인 상태를 확인해 주세요.",
        SpiritsApiException { Status: HttpStatusCode.Forbidden } => "이 작업을 할 권한이 없어요.",
        SpiritsApiException { Status: HttpStatusCode.NotFound } => "해당 정보가 서버에 없어요.",
        SpiritsApiException { Status: HttpStatusCode.Conflict } => "다른 곳에서 이미 바뀌었어요. 동기화 후 확인해 주세요.",
        SpiritsApiException { Status: HttpStatusCode.BadRequest } => "입력값이나 서버의 일정 규칙을 확인해 주세요.",
        SpiritsApiException => "서버에서 요청을 처리하지 못했어요.",
        InvalidOperationException or InvalidDataException => "서버 주소나 API 응답을 확인해 주세요.",
        JsonException => "서버 응답 형식이 달라요.",
        _ => "네트워크 상태를 확인하고 다시 시도해 주세요.",
    };

    private Task SyncAccount() => RunServer(() => SyncAccountData(), "동기화하지 못했어요");

    private async Task SyncAccountData(bool refreshProfile = true)
    {
        var missing = new List<string>();
        await TryLoad(LoadCalendar, "일정");
        if (refreshProfile) await TryLoad(async () =>
        {
            _account = await _api!.Get<ServerProfile>("api/user/me");
            _data.Nickname = _account.Nickname ?? "";
            Raise(nameof(AccountText));
            RaiseAccountSettings();
            Refresh();
        }, "프로필");
        await TryLoad(LoadAccountDisplay, "디스플레이 설정");
        await TryLoad(LoadAccountNotificationSettings, "알림 설정");
        await TryLoad(() => LoadNotices(), "알림");
        await TryLoad(LoadDex, "도감");
        ServerStatus = missing.Count == 0 ? "동기화했어요" : $"동기화 실패 · {string.Join(", ", missing)}";

        async Task TryLoad(Func<Task> load, string name)
        {
            try { await load(); }
            catch (Exception ex) when (IsServerError(ex)) { missing.Add(name); }
        }
    }

    // 홈에는 최근 30일의 미완료 단발 일정까지 표시한다. 연간 보기는 서버 날짜 한도에 맞춰 나눠 읽는다.
    private readonly record struct CalendarWindow(DateOnly Today, DateOnly From, DateOnly To, DateOnly Selected);

    private CalendarWindow VisibleCalendar()
    {
        if (Page != "calendar") return new(Today, Today, Today, Today);
        var weekStart = _selected.AddDays(-(int)_selected.DayOfWeek);
        var gridStart = _view.AddDays(-(int)_view.DayOfWeek);
        return CalMode switch
        {
            "week" => new(Today, weekStart, weekStart.AddDays(6), _selected),
            "year" => new(Today, new(_view.Year, 1, 1), new(_view.Year, 12, 31), _selected),
            _ => new(Today, gridStart, gridStart.AddDays(MonthRows * 7 - 1), _selected),
        };
    }

    private void QueueCalendarLoad()
    {
        if (!CanUseAccount || _calendarRequested == VisibleCalendar()) return;
        _ = RunServer(LoadCalendar, "일정을 불러오지 못했어요");
    }

    private async Task LoadCalendar()
    {
        var window = VisibleCalendar();
        _calendarRequested = window;
        var days = new SortedSet<DateOnly>();
        AddDays(window.Today.AddDays(-30), window.Today);
        AddDays(window.From, window.To);
        days.Add(window.Selected);
        var ranges = new List<(DateOnly From, DateOnly To)>();
        foreach (var day in days)
        {
            if (ranges.Count > 0 && day.DayNumber == ranges[^1].To.DayNumber + 1 &&
                day.DayNumber - ranges[^1].From.DayNumber < 90)
                ranges[^1] = (ranges[^1].From, day);
            else
                ranges.Add((day, day));
        }

        var tasks = new List<TodoItem>();
        foreach (var (from, to) in ranges)
        {
            var response = await _api!.Get<ServerCalendar>($"api/task?from={SpiritsApi.Day(from)}&to={SpiritsApi.Day(to)}");
            if (response.Items is null) throw new InvalidDataException("일정 목록이 없어요.");
            tasks.AddRange(response.Items
                .Where(item => item.OccurrenceDate >= from && item.OccurrenceDate <= to)
                .Select(MapTask));
        }
        _data.Tasks = tasks.DistinctBy(t => (t.ServerId, t.Date)).ToList();
        _serverDays = _data.Tasks.GroupBy(t => t.Date).ToDictionary(g => g.Key, g => g.ToArray());
        _lastCalendarLoad = DateTimeOffset.UtcNow;
        Refresh();
        CheckAlarms();

        void AddDays(DateOnly from, DateOnly to)
        {
            for (int n = from.DayNumber; n <= to.DayNumber; n++) days.Add(DateOnly.FromDayNumber(n));
        }
    }

    private static TodoItem MapTask(ServerOccurrence task)
    {
        var item = new TodoItem
        {
            Id = ServerGuid(task.TaskId),
            ServerId = task.TaskId,
            ServerStartDate = task.StartDate,
            Title = task.Title,
            Date = task.OccurrenceDate,
            Time = task.IsAllDay ? null : task.EndTime,
            Important = task.IsImportant,
            Repeat = task.RepeatType switch { "DAILY" => Recur.Daily, "WEEKLY" => Recur.Weekly, "MONTHLY" => Recur.Monthly, _ => Recur.None },
            RepeatDays = task.RepeatType == "WEEKLY"
                ? (task.RepeatDaysOfWeek ?? []).Select(day => (int)Enum.Parse<DayOfWeek>(day, true)).ToList()
                : (task.RepeatDaysOfMonth ?? []).ToList(),
            RepeatEndDate = task.RepeatEndDate,
            ExcludeHoliday = task.ExcludeHoliday,
            Alarm = task.NotificationMinutes switch { 0 => "정각", 10 => "10분 전", 30 => "30분 전", 60 => "1시간 전", _ => Alarm.None },
        };
        Recur.SetDone(item, item.Date, task.IsCompleted);
        return item;
    }

    private static Guid ServerGuid(long id) => Guid.ParseExact($"0000000000000000{id:x16}", "N");

    private Task LoadServerTaskDetail(TodoItem item) => RunServer(async () =>
    {
        var detail = await _api!.GetTask(item.ServerId!.Value, item.Date);
        if (!ReferenceEquals(item, _editing)) return;
        if (detail is null)
        {
            ClosePanel();
            await LoadCalendar();
            ShowToast("이 날짜에 예정된 일정이 없어요.", null);
            return;
        }
        BeginEdit(new TaskRow(new Occ(MapTask(detail), detail.OccurrenceDate), Today, TimeOnly.FromDateTime(DateTime.Now)),
            fetchServer: false);
    }, "일정 상세를 불러오지 못했어요");

    private static ServerTaskRequest TaskRequest(TodoItem item, bool sendDate)
    {
        var days = item.RepeatDays;
        return new ServerTaskRequest(item.Title, sendDate ? item.Date.ToDateTime(item.Time ?? TimeOnly.MinValue) : null,
            item.Time is null, item.Important,
            Alarm.MinutesBefore(item.Alarm) switch { 10 => "TEN_MINUTES", 30 => "THIRTY_MINUTES", 60 => "ONE_HOUR", _ => "NONE" },
            item.Repeat switch { Recur.Daily => "DAILY", Recur.Weekly => "WEEKLY", Recur.Monthly => "MONTHLY", _ => "NONE" },
            item.Repeat == Recur.None ? null : item.RepeatEndDate,
            item.Repeat == Recur.Weekly ? (days.Count == 0 ? [(int)item.Date.DayOfWeek] : days)
                .Select(day => ((DayOfWeek)day).ToString().ToUpperInvariant()).ToArray() : null,
            item.Repeat == Recur.Monthly ? (days.Count == 0 ? [item.Date.Day] : days.ToArray()) : null,
            item.Repeat != Recur.None && item.ExcludeHoliday);
    }

    public bool ShowTaskSave => IsNew || (IsSignedIn && IsExisting);
    public string TaskSaveLabel => IsNew ? "확인" : "저장";

    private Task SaveServerTask()
    {
        var original = _editing;
        var date = DateOnly.FromDateTime(EditDate!.Value);
        var originalStart = original?.ServerStartDate ?? original?.Date;
        var displayedTime = EditTime;
        // The editor rounds the displayed minute to five minutes. An unrelated edit must preserve the actual server time.
        var time = original?.Time is { } prior && displayedTime is { } shown &&
            prior.Hour == shown.Hour && prior.Minute / 5 == shown.Minute / 5 ? prior : displayedTime;
        var draft = new TodoItem
        {
            Title = EditTitle.Trim(), Important = EditImportant, Date = date, Time = time,
            Repeat = EditRepeat, RepeatDays = [.. _editRepeatDays], RepeatEndDate = original?.RepeatEndDate,
            ExcludeHoliday = original?.ExcludeHoliday ?? false,
            Alarm = time is null ? Alarm.None : EditAlarm,
        };
        if (draft.Alarm == "정각")
        {
            ShowToast("계정 일정은 정각 알림을 아직 지원하지 않아요.", null);
            return Task.CompletedTask;
        }
        if (original is not null && Recur.IsRepeating(original) && !Recur.IsRepeating(draft))
            draft.Date = date == originalStart ? _editingDate : date;
        if (original is { ServerStartDate: null } && Recur.IsRepeating(original) && Recur.IsRepeating(draft) &&
            original.Time != draft.Time && date == original.Date)
        {
            ShowToast("반복 일정의 시작일을 확인할 수 없어요. 시작일을 바꾸려는 경우에만 새 날짜와 시간을 함께 선택해 주세요.", null);
            return Task.CompletedTask;
        }
        var request = TaskRequest(draft, original is null || originalStart != draft.Date ||
            original.Time != draft.Time || Recur.IsRepeating(original) != Recur.IsRepeating(draft));
        return RunServer(async () =>
        {
            if (original?.ServerId is { } id)
                await _api!.Patch<CreatedTask>(SpiritsApi.TaskPath(id), request);
            else
                await _api!.Post<CreatedTask>("api/task", request);
            ClosePanel();
            if (Page == "calendar" && original is null) SelectDate(date);
            await RefreshAfterWrite(original is null ? "일정을 추가했어요" : "일정을 저장했어요");
        }, "일정을 저장하지 못했어요");
    }

    private Task ToggleServerTask(TodoItem item, DateOnly date, bool? restoreDone = null)
    {
        if (item.ServerId is not { } id) return Task.CompletedTask;
        var done = restoreDone ?? !Recur.IsDone(item, date);
        return RunServer(async () =>
        {
            var path = $"{SpiritsApi.TaskPath(id)}/complete?date={SpiritsApi.Day(date)}";
            TaskReward? reward = null;
            try
            {
                if (done) reward = await _api!.Post<TaskReward>(path);
                else await _api!.Delete(path);
            }
            catch (SpiritsApiException ex) when (ex.Status is HttpStatusCode.Conflict or HttpStatusCode.NotFound)
            {
                await LoadCalendar();
                throw;
            }
            if (_editing?.ServerId == id && _editingDate == date)
            {
                Recur.SetDone(_editing, date, done);
                Raise(nameof(EditDoneLabel));
            }
            var message = done ? reward is { CurrencyEarned: > 0 } ? $"일정을 끝냈어요 · 정수 +{reward.CurrencyEarned}" : "일정을 끝냈어요" : "완료를 취소했어요";
            await RefreshAfterWrite(message, restoreDone is null ? () => _ = ToggleServerTask(item, date, !done) : null);
            if (done) SummaryBumped?.Invoke();
        }, "완료 상태를 바꾸지 못했어요");
    }

    private Task DeleteServerTask(TodoItem item)
    {
        if (item.ServerId is not { } id) return Task.CompletedTask;
        return RunServer(async () =>
        {
            await _api!.Delete("api/task", new { taskIds = new[] { id } });
            if (_editing?.ServerId == id) ClosePanel();
            await RefreshAfterWrite("일정을 삭제했어요");
        }, "일정을 삭제하지 못했어요");
    }

    private Task ToggleServerImportant(TodoItem item)
    {
        if (item.ServerId is not { } id) return Task.CompletedTask;
        var request = TaskRequest(item, sendDate: false) with { IsImportant = !item.Important };
        return RunServer(async () =>
        {
            await _api!.Patch<CreatedTask>(SpiritsApi.TaskPath(id), request);
            if (_editing?.ServerId == id) ClosePanel();
            await RefreshAfterWrite("중요 표시를 바꿨어요");
        }, "중요 표시를 바꾸지 못했어요");
    }

    private Task MoveServerTasks(IReadOnlyList<TodoItem> items, DateOnly date) =>
        MoveServerTasks(items.Select(item => (item, date)).ToList(), allowUndo: true);

    private Task MoveServerTasks(IReadOnlyList<(TodoItem Item, DateOnly Date)> moves, bool allowUndo)
    {
        if (moves.Count == 0) return Task.CompletedTask;
        return RunServer(async () =>
        {
            var completed = new List<(TodoItem Item, DateOnly Date)>();
            try
            {
                foreach (var (item, date) in moves)
                {
                    if (item.ServerId is not { } id) continue;
                    await _api!.Patch(SpiritsApi.TaskPath(id) + "/postpone", new { newDate = date });
                    completed.Add((item, item.Date));
                }
            }
            catch (Exception ex) when (IsServerError(ex) && completed.Count > 0)
            {
                // One server request succeeded per item. Refresh the actual dates even when a later request fails.
            }
            if (_editing?.ServerId is { } editingId && completed.Any(move => move.Item.ServerId == editingId)) ClosePanel();
            var message = completed.Count < moves.Count
                ? $"{completed.Count}개를 옮겼어요. 나머지는 다시 시도해 주세요"
                : allowUndo ? $"{completed.Count}개를 {Fmt.Short(moves[0].Date)}로 옮겼어요" : "이전 날짜로 돌렸어요";
            await RefreshAfterWrite(message, allowUndo ? () => _ = MoveServerTasks(completed, allowUndo: false) : null);
        }, "일정을 옮기지 못했어요");
    }

    private async Task RefreshAfterWrite(string message, Action? undo = null)
    {
        try { await LoadCalendar(); }
        catch (Exception ex) when (IsServerError(ex))
        {
            ServerStatus = "목록 갱신 실패";
            message += ". 동기화를 눌러 목록을 다시 불러와 주세요.";
        }
        ShowToast(message, undo);
    }
}
