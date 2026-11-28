using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Input;
using Microsoft.Win32;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>도감과 설정 화면의 상태.</summary>
public sealed partial class MainViewModel
{
    public sealed record Section(string Key, string Label);

    private bool _applyingSystem;

    private void InitExtras()
    {
        SelectSpiritCommand = new RelayCommand(p => { _selectedSpirit = ((SpiritRow)p!).Id; RebuildSpirits(); Raise(string.Empty); });
        SetRepresentativeCommand = new RelayCommand(_ => SetRepresentative(), _ => SelectedSpirit is { Owned: true, IsRepresentative: false });
        OpenDataFolderCommand = new RelayCommand(_ => OpenDataFolder());
        ExportCsvCommand = new RelayCommand(_ => ExportCsv());

        _selectedSpirit = SpiritCatalog.IsOwned(_data.RepresentativeId) ? _data.RepresentativeId : "wind";
        if (!SpiritCatalog.IsOwned(_data.RepresentativeId)) _data.RepresentativeId = "wind";
        RebuildSpirits();

        SystemEvents.UserPreferenceChanged += (_, e) =>
        {
            if (e.Category == UserPreferenceCategory.General && FollowSystem)
                System.Windows.Application.Current.Dispatcher.Invoke(ApplySystemTheme);
        };
        if (FollowSystem) ApplySystemTheme();
    }

    // ---------- 도감 ----------
    private string _selectedSpirit = "wind";
    public ICommand SelectSpiritCommand { get; private set; } = null!;
    public ICommand SetRepresentativeCommand { get; private set; } = null!;
    public IReadOnlyList<SpiritRow> Spirits { get; private set; } = [];
    public SpiritRow? SelectedSpirit => Spirits.FirstOrDefault(s => s.IsSelected);
    public SpiritRow? Representative => Spirits.FirstOrDefault(s => s.IsRepresentative);
    public int SpiritsOwned => Spirits.Count(s => s.Owned);
    public int SpiritsTotal => Spirits.Count;
    public string SpiritsProgress => $"{SpiritsOwned} / {SpiritsTotal}";
    public string RepresentativeLabel => SelectedSpirit?.IsRepresentative == true ? "지금 대표 정령이에요" : "대표 정령으로 설정";

    private void RebuildSpirits() => Spirits = SpiritCatalog.Build(_data.RepresentativeId, _selectedSpirit);

    private void SetRepresentative()
    {
        if (SelectedSpirit is not { Owned: true } s) return;
        var before = _data.RepresentativeId;
        _data.RepresentativeId = s.Id;
        Persist(); RebuildSpirits(); Raise(string.Empty);
        ShowToast($"{s.Name}{Fmt.Josa(s.Name, "을", "를")} 대표 정령으로 정했어요", () => { _data.RepresentativeId = before; Persist(); RebuildSpirits(); Raise(string.Empty); });
    }

    // ---------- 설정 ----------
    public IReadOnlyList<Section> Sections { get; } =
    [
        new("profile", "프로필"), new("display", "디스플레이"), new("notify", "알림"),
        new("data", "데이터"), new("guide", "이용 안내"), new("about", "앱 정보"),
    ];

    private string _section = "profile";
    public string SettingsSection { get => _section; set { if (value is not null) Set(ref _section, value); } }

    public string Nickname
    {
        get => _data.Nickname;
        set { var v = (value ?? "").Trim(); if (v.Length > 10) v = v[..10]; if (v == _data.Nickname) return; _data.Nickname = v; Persist(); Refresh(); }
    }
    public string NicknameCount => $"{_data.Nickname.Length} / 10";
    public string AvatarText => _data.Nickname.Length > 0 ? _data.Nickname[..1] : "나";

    public bool FollowSystem
    {
        get => _data.FollowSystem;
        set { if (_data.FollowSystem == value) return; _data.FollowSystem = value; Persist(); Raise(); if (value) ApplySystemTheme(); }
    }

    public bool ContrastMore
    {
        get => _data.ContrastMore;
        set { if (_data.ContrastMore == value) return; _data.ContrastMore = value; Persist(); ThemeService.Apply(IsDark, value); Raise(); }
    }

    private void ApplySystemTheme()
    {
        _applyingSystem = true;
        IsDark = ThemeService.SystemIsDark();
        _applyingSystem = false;
    }

    public bool NotifyRemind { get => _data.NotifyRemind; set { _data.NotifyRemind = value; Persist(); Raise(); } }
    public bool NotifyPostponed { get => _data.NotifyPostponed; set { _data.NotifyPostponed = value; Persist(); Raise(); } }
    public bool NotifyRoutine { get => _data.NotifyRoutine; set { _data.NotifyRoutine = value; Persist(); Raise(); } }
    public bool NotifyStreak { get => _data.NotifyStreak; set { _data.NotifyStreak = value; Persist(); Raise(); } }
    public bool NotifyNight { get => _data.NotifyNight; set { _data.NotifyNight = value; Persist(); Raise(); } }

    public ICommand OpenDataFolderCommand { get; private set; } = null!;
    public ICommand ExportCsvCommand { get; private set; } = null!;
    public string TaskCountText => $"일정 {_data.Tasks.Count}개 (완료 {_data.Tasks.Count(t => t.Done)}개)";
    public string VersionText => "버전 " + (typeof(MainViewModel).Assembly.GetName().Version?.ToString(3) ?? "0.0.0");

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
                sb.Append($"{t.Date:yyyy-MM-dd},{(t.Time is { } tm ? tm.ToString("HH:mm") : "")},{Csv(t.Title)},{(t.Important ? "Y" : "")},{(t.Done ? "Y" : "")}\r\n");
            File.WriteAllText(dlg.FileName, sb.ToString(), new UTF8Encoding(encoderShouldEmitUTF8Identifier: true)); // 엑셀에서 한글이 깨지지 않게 BOM
            ShowToast($"일정 {_data.Tasks.Count}개를 내보냈어요", null);
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
        {
            ShowToast("내보내지 못했어요: " + ex.Message, null);
        }
    }
}
