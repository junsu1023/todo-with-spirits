using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Text;
using System.Windows.Input;
using System.Windows.Threading;
using Microsoft.Win32;
using TodoSpirits.Windows.Models;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>
/// 설정 화면의 상태. 시안(unity/windows-ui/design/settings-v4.html)의 왼쪽 목록 순서와 창을 그대로 따른다.
/// </summary>
public sealed partial class MainViewModel
{
    // ---------- 왼쪽 목록 ----------
    private static readonly string[] Order =
        ["profile", "notify", "display", "pc", "data", "notice", "guide", "qna", "terms", "password", "logout", "withdraw"];

    /// <summary>왼쪽 목록에서의 순서. 아래로 가면 창이 오른쪽에서, 위로 가면 왼쪽에서 들어온다.</summary>
    public static IReadOnlyList<string> SettingsOrder => Order;
    public static int SectionIndex(string key) => Array.IndexOf(Order, key);

    private string _section = "profile";
    public string SettingsSection
    {
        get => _section;
        set
        {
            if (value is null || SectionIndex(value) < 0 || !Set(ref _section, value)) return;
            if (SavedOn) { _savedTimer?.Stop(); SavedOn = false; Raise(nameof(SavedOn)); }
        }
    }

    public ICommand SelectSectionCommand => field ??= new RelayCommand(p => SettingsSection = (string)p!);

    // ---------- "저장됨" 표시 ----------
    private DispatcherTimer? _savedTimer;
    /// <summary>값을 바꾼 직후 1.4초 동안 켜진다 (창 제목 오른쪽의 "저장됨").</summary>
    public bool SavedOn { get; private set; }

    public void FlashSaved()
    {
        if (_savedTimer is null)
        {
            _savedTimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(1400) };
            _savedTimer.Tick += (_, _) => { _savedTimer.Stop(); SavedOn = false; Raise(nameof(SavedOn)); };
        }
        SavedOn = true; Raise(nameof(SavedOn));
        _savedTimer.Stop(); _savedTimer.Start();
    }

    // ---------- 프로필 ----------
    private static readonly string[] RandomNames = ["구름", "새싹", "별빛", "몽실"];
    private string? _nickDraft;

    public string Nickname
    {
        get => _data.Nickname;
    }

    /// <summary>입력 칸에 적고 있는 닉네임. "확인"을 눌러야 저장된다.</summary>
    public string NicknameDraft
    {
        get => _nickDraft ?? _data.Nickname;
        set
        {
            var v = value ?? ""; if (v.Length > NicknameLimit) v = v[..NicknameLimit];
            _nickDraft = v;
            Raise(); Raise(nameof(NicknameCount)); Raise(nameof(NicknameFull));
        }
    }
    public string NicknameCount => $"{NicknameDraft.Length} / {NicknameLimit}";
    public bool NicknameFull => NicknameDraft.Length >= NicknameLimit;
    public string AvatarText => _data.Nickname.Length > 0 ? _data.Nickname[..1] : "나";
    /// <summary>탈퇴 화면의 "○○님," (닉네임이 없으면 비운다).</summary>
    public string WithdrawHello => _data.Nickname.Length > 0 ? _data.Nickname + "님," : "";

    public ICommand ConfirmNicknameCommand => field ??= new RelayCommand(_ => ConfirmNickname(), _ => !_serverBusy);

    private void ConfirmNickname()
    {
        var v = NicknameDraft.Trim();
        if (v.Length == 0) v = RandomNames[Random.Shared.Next(RandomNames.Length)] + "친구"; // 비워 두면 랜덤으로
        if (IsSignedIn) { _ = SaveAccountNickname(v); return; }
        _nickDraft = null;
        if (v != _data.Nickname) { _data.Nickname = v; Persist(); }
        Refresh();
        ShowToast("닉네임을 바꿨어요", null);
    }

    // ---------- 알림 ----------
    public bool NotifyRemind { get => _data.NotifyRemind; set => SetNotificationOption(value, _data.NotifyRemind, "reminderEnabled", v => _data.NotifyRemind = v); }
    public bool NotifyPostponed { get => _data.NotifyPostponed; set => SetNotificationOption(value, _data.NotifyPostponed, "postponedTaskEnabled", v => _data.NotifyPostponed = v); }
    public bool NotifyRoutine { get => _data.NotifyRoutine; set => SetNotificationOption(value, _data.NotifyRoutine, "routineReminderEnabled", v => _data.NotifyRoutine = v); }
    public bool NotifyStreak { get => _data.NotifyStreak; set => SetNotificationOption(value, _data.NotifyStreak, "streakSaveEnabled", v => _data.NotifyStreak = v); }
    public bool NotifyNight { get => _data.NotifyNight; set => SetNotificationOption(value, _data.NotifyNight, "nightPushEnabled", v => _data.NotifyNight = v); }

    private void SetNotificationOption(bool value, bool current, string key, Action<bool> assign,
        [System.Runtime.CompilerServices.CallerMemberName] string? property = null)
    {
        if (value == current) return;
        if (IsSignedIn) { _ = SaveAccountNotificationSetting(key, value); return; }
        assign(value);
        Persist();
        Raise(property);
        FlashSaved();
    }

    // ---------- 디스플레이 ----------
    /// <summary>일정 카드에 디데이를 보여 준다.</summary>
    public bool ShowDday
    {
        get => _data.ShowDday;
        set
        {
            if (_data.ShowDday == value) return;
            if (IsSignedIn) { _ = SaveAccountDisplay(new { ddayDisplayEnabled = value }); return; }
            _data.ShowDday = value; Persist(); Refresh(); FlashSaved();
        }
    }

    public IReadOnlyList<string> Languages { get; } = ["시스템", "한국어", "English", "日本語", "中文"];

    /// <summary>고른 언어. 아직은 값만 기억하고 화면은 한국어로만 나온다.</summary>
    public string Language
    {
        get => Languages.Contains(_data.Language) ? _data.Language : Languages[0];
        set
        {
            if (value is null || !Languages.Contains(value) || value == Language) return;
            if (IsSignedIn) { _ = SaveAccountDisplay(new { language = LanguageCodes[Languages.ToList().IndexOf(value)] }); return; }
            _data.Language = value; Persist(); Raise(); FlashSaved();
        }
    }
    public ICommand SetLanguageCommand => field ??= new RelayCommand(p => Language = (string)p!);

    // ---------- PC 설정 ----------
    private const string RunKey = @"Software\Microsoft\Windows\CurrentVersion\Run";
    private const string RunName = "TodoWithSpirits";

    private static bool IsRegistryError(Exception ex) =>
        ex is System.Security.SecurityException or UnauthorizedAccessException or IOException or ObjectDisposedException;

    /// <summary>Windows 시작 시 자동 실행. 값을 따로 저장하지 않고 레지스트리(HKCU\…\Run)를 그대로 읽고 쓴다.</summary>
    public bool AutoStart
    {
        get
        {
            try
            {
                using var key = Registry.CurrentUser.OpenSubKey(RunKey);
                return key?.GetValue(RunName) is string s && s.Length > 0;
            }
            catch (Exception ex) when (IsRegistryError(ex)) { return false; }
        }
        set
        {
            try
            {
                using var key = Registry.CurrentUser.CreateSubKey(RunKey, writable: true);
                if (value)
                {
                    var exe = Environment.ProcessPath;
                    if (string.IsNullOrEmpty(exe)) ShowToast("실행 파일 위치를 알 수 없어 자동 실행을 켜지 못했어요", null);
                    else { key.SetValue(RunName, $"\"{exe}\""); FlashSaved(); }
                }
                else { key.DeleteValue(RunName, throwOnMissingValue: false); FlashSaved(); }
            }
            catch (Exception ex) when (IsRegistryError(ex))
            {
                ShowToast("자동 실행 설정을 바꾸지 못했어요: " + ex.Message, null);
            }
            Raise(); // 실패했으면 스위치가 실제 상태로 돌아간다
        }
    }

    // ---------- 데이터 ----------
    private static readonly CultureInfo Ko = CultureInfo.GetCultureInfo("ko-KR");

    public ICommand OpenDataFolderCommand => field ??= new RelayCommand(_ => OpenDataFolder());
    public ICommand ExportCsvCommand => field ??= new RelayCommand(_ => ExportCsv());
    public ICommand BackupCommand => field ??= new RelayCommand(_ => Backup());
    public string TaskCountText => $"{(IsSignedIn ? "조회한 회차" : "일정")} {_data.Tasks.Count}개 (완료 {_data.Tasks.Count(t => Recur.IsDone(t, t.Date))}개)";

    public string LastBackupText
    {
        get
        {
            if (_localData.LastBackup is not { } at) return "아직 백업한 적 없어요";
            var day = DateOnly.FromDateTime(at);
            var when = day == Today ? "오늘 " + at.ToString("tt h:mm", Ko)
                : at.Year == Today.Year ? $"{at.Month}월 {at.Day}일" : $"{at.Year}년 {at.Month}월 {at.Day}일";
            return "마지막 백업 · " + when;
        }
    }

    /// <summary>저장 파일을 옆의 backups 폴더에 시각을 붙인 이름으로 복사한다.</summary>
    private void Backup()
    {
        try
        {
            _store.Save(_localData);
            var dir = Path.Combine(Path.GetDirectoryName(_store.FilePath)!, "backups");
            Directory.CreateDirectory(dir);
            var now = DateTime.Now;
            File.Copy(_store.FilePath, Path.Combine(dir, $"data-{now:yyyyMMdd-HHmmss}.json"), overwrite: true);
            _localData.LastBackup = now;
            Persist(); Raise(nameof(LastBackupText));
            ShowToast("백업했어요", null);
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
        {
            ShowToast("백업하지 못했어요: " + ex.Message, null);
        }
    }

    private void OpenDataFolder()
    {
        try
        {
            var dir = Path.GetDirectoryName(_store.FilePath)!;
            Directory.CreateDirectory(dir);
            Process.Start(new ProcessStartInfo("explorer.exe", File.Exists(_store.FilePath) ? $"/select,\"{_store.FilePath}\"" : $"\"{dir}\"") { UseShellExecute = true });
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException or System.ComponentModel.Win32Exception)
        {
            ShowToast("폴더를 열지 못했어요: " + ex.Message, null);
        }
    }

    private static string Csv(string s) => "\"" + s.Replace("\"", "\"\"") + "\"";

    private void ExportCsv()
    {
        var dlg = new SaveFileDialog
        {
            Title = "일정 내보내기", Filter = "CSV 파일 (*.csv)|*.csv", FileName = $"todo-with-spirits-{DateTime.Now:yyyyMMdd}.csv",
        };
        if (dlg.ShowDialog() != true) return;
        try
        {
            var sb = new StringBuilder("날짜,시간,제목,중요,완료\r\n");
            foreach (var t in _data.Tasks.OrderBy(t => t.Date).ThenBy(t => t.Time))
                sb.Append($"{t.Date:yyyy-MM-dd},{(t.Time is { } tm ? tm.ToString("HH:mm") : "")},{Csv(t.Title)},{(t.Important ? "Y" : "")},{(Recur.IsDone(t, t.Date) ? "Y" : "")}\r\n");
            File.WriteAllText(dlg.FileName, sb.ToString(), new UTF8Encoding(encoderShouldEmitUTF8Identifier: true)); // 엑셀에서 한글이 깨지지 않게 BOM
            ShowToast($"일정 {_data.Tasks.Count}개를 내보냈어요", null);
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
        {
            ShowToast("내보내지 못했어요: " + ex.Message, null);
        }
    }

    // ---------- 버전 · 업데이트 ----------
    public string VersionText => "버전 " + (typeof(MainViewModel).Assembly.GetName().Version?.ToString(3) ?? "0.0.0");

    /// <summary>업데이트를 알려 줄 서버가 아직 없다. 확인한 척하지 않고 그대로 알린다.</summary>
    public ICommand CheckUpdateCommand => field ??= new RelayCommand(_ => ShowToast("업데이트 확인은 아직 준비 중이에요", null));
}
