using System.Text.Json;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

public sealed partial class MainViewModel
{
    private static readonly string[] LanguageCodes = ["system", "ko", "en", "ja", "zh"];
    public bool CanEditSettings => !_serverBusy;
    public bool NeedsEmailVerification => _account?.EmailVerificationStatus is "UNVERIFIED" or "NEEDS_REVERIFICATION";
    public string? SubscriptionText { get => field; private set => Set(ref field, value); }

    public Task LoadSubscription() => !CanUseAccount ? Task.CompletedTask : RunServer(async () =>
    {
        var plan = await _api!.Get<ServerSubscription>("api/user/me/subscription");
        var label = plan.PlanType switch { "FREE" => "무료", "MONTHLY" => "월간", "YEARLY" => "연간", _ => plan.PlanType };
        SubscriptionText = plan.ExpiresAt is { } end ? $"{label} · 만료일 {end:yyyy.MM.dd}" : label;
    }, "구독 정보를 불러오지 못했어요");

    public Task ChangeAccountEmail(string email) => !CanUseAccount ? Task.CompletedTask : RunServer(async () =>
    {
        _account = await _api!.Patch<ServerProfile>("api/user/me/email", new { email });
        Raise(nameof(AccountText));
        Raise(nameof(NeedsEmailVerification));
        ShowToast("이메일을 변경했어요. 새 주소로 인증번호를 받아 주세요.", null);
    }, "이메일을 바꾸지 못했어요");

    public Task SendAccountVerification() => !CanUseAccount || !NeedsEmailVerification ? Task.CompletedTask : RunServer(async () =>
    {
        await _api!.Post<JsonElement>("api/user/me/email/verify/send");
        ShowToast("연동 계정 이메일로 인증번호를 보냈어요", null);
    }, "인증번호를 보내지 못했어요");

    public Task VerifyAccountEmail(string code) => !CanUseAccount || !NeedsEmailVerification ? Task.CompletedTask : RunServer(async () =>
    {
        await _api!.PublicPost<JsonElement>("api/user/email/verify", new { email = _account!.Email, code });
        _account = _account with { EmailVerificationStatus = "VERIFIED" };
        Raise(nameof(NeedsEmailVerification));
        ShowToast("이메일을 인증했어요", null);
    }, "이메일을 인증하지 못했어요");
    public int NicknameLimit => IsSignedIn ? 12 : 10;
    public string TaskEditHelp => !IsSignedIn ? "변경 내용은 바로 저장돼요"
        : _editing is { ServerStartDate: null } item && Models.Recur.IsRepeating(item)
            ? "이 날짜는 선택한 회차예요. 날짜를 변경하면 반복 시작일이 바뀌어요."
            : "저장을 눌러 계정에 반영해요";

    private void RaiseAccountSettings()
    {
        Raise(nameof(NeedsEmailVerification));
        Raise(nameof(Nickname));
        Raise(nameof(NicknameDraft));
        Raise(nameof(NicknameCount));
        Raise(nameof(NicknameFull));
        Raise(nameof(ProfileName));
        Raise(nameof(AvatarText));
        Raise(nameof(WithdrawHello));
        Raise(nameof(IsDark));
        Raise(nameof(ThemeLabel));
        Raise(nameof(ShowDday));
        Raise(nameof(Language));
        Raise(nameof(NotifyRemind));
        Raise(nameof(NotifyPostponed));
        Raise(nameof(NotifyRoutine));
        Raise(nameof(NotifyStreak));
        Raise(nameof(NotifyNight));
        Raise(nameof(TaskEditHelp));
    }

    private Task SaveAccountNickname(string nickname)
    {
        if (nickname.Length < 2)
        {
            ShowToast("닉네임은 2글자 이상으로 입력해 주세요.", null);
            return Task.CompletedTask;
        }
        return RunServer(async () =>
        {
            _account = await _api!.Patch<ServerProfile>("api/user/me", new { nickname });
            _data.Nickname = _account.Nickname ?? "";
            _nickDraft = null;
            RaiseAccountSettings();
            Refresh();
            ShowToast("닉네임을 바꿨어요", null);
        }, "닉네임을 바꾸지 못했어요");
    }

    private async Task LoadAccountDisplay() =>
        ApplyAccountDisplay(await _api!.Get<ServerDisplay>("api/setting/display"));

    private async Task SaveAccountDisplay(object body)
    {
        await RunServer(async () =>
        {
            var settings = await _api!.Patch<ServerDisplay>("api/setting/display", body);
            if (body is DisplayThemeRequest && _data.FollowSystem)
            {
                _data.FollowSystem = false;
                Persist();
                Raise(nameof(FollowSystem));
            }
            ApplyAccountDisplay(settings);
            FlashSaved();
        }, "디스플레이 설정을 저장하지 못했어요");
        RaiseAccountSettings();
    }

    private sealed record DisplayThemeRequest(bool DarkMode);

    private void ApplyAccountDisplay(ServerDisplay settings)
    {
        _data.Theme = settings.DarkMode ? "dark" : "light";
        _data.ShowDday = settings.DdayDisplayEnabled;
        var language = Array.IndexOf(LanguageCodes, settings.Language);
        _data.Language = Languages[Math.Max(0, language)];
        ApplyAccountTheme();
        RaiseAccountSettings();
        Refresh();
    }

    private void ApplyAccountTheme()
    {
        if (_data.FollowSystem) ApplySystemTheme();
        else
        {
            _isDark = _data.Theme == "dark";
            ThemeService.Apply(_isDark, _data.ContrastMore);
            Raise(nameof(IsDark));
            Raise(nameof(ThemeLabel));
        }
    }

    private async Task LoadAccountNotificationSettings() =>
        ApplyNotificationSettings(await _api!.Get<ServerNotificationSettings>("api/setting/notification"));

    private async Task SaveAccountNotificationSetting(string key, bool value)
    {
        await RunServer(async () =>
        {
            var settings = await _api!.Patch<ServerNotificationSettings>("api/setting/notification",
                new Dictionary<string, bool> { [key] = value });
            ApplyNotificationSettings(settings);
            FlashSaved();
        }, "알림 설정을 저장하지 못했어요");
        RaiseAccountSettings();
    }

    private void ApplyNotificationSettings(ServerNotificationSettings settings)
    {
        _data.NotifyRemind = settings.ReminderEnabled;
        _data.NotifyPostponed = settings.PostponedTaskEnabled;
        _data.NotifyRoutine = settings.RoutineReminderEnabled;
        _data.NotifyStreak = settings.StreakSaveEnabled;
        _data.NotifyNight = settings.NightPushEnabled;
        RaiseAccountSettings();
        RaiseNoti();
    }

    public Task SendSignupCode(string email) => RunServer(async () =>
    {
        using var api = new SpiritsApi();
        email = email.Trim();
        var check = await api.PublicGet<EmailAvailability>("api/auth/check-email?email=" + Uri.EscapeDataString(email));
        if (check.Registered)
        {
            ShowToast("이미 가입된 이메일이에요. 로그인해 주세요.", null);
            return;
        }
        await api.PublicPost<JsonElement>("api/user/email/verify/send", new { email });
        ShowToast("이메일로 인증번호를 보냈어요", null);
    }, "인증번호를 보내지 못했어요");

    public async Task<bool> RegisterAccount(string email, string password, string code)
    {
        var created = false;
        await RunServer(async () =>
        {
            using var api = new SpiritsApi();
            email = email.Trim();
            code = code.Trim();
            try { await api.PublicPost<JsonElement>("api/user/email/verify", new { email, code }); }
            catch (SpiritsApiException ex) when (ex.Code == "ALREADY_COMPLETED") { }
            await api.PublicPost<ServerProfile>("api/auth/signup", new { email, password });
            created = true;
            ShowToast("가입했어요. 이메일로 로그인해 주세요.", null);
        }, "회원가입하지 못했어요");
        return created;
    }
}
