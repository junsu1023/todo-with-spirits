using System.IO;
using System.Windows;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>바탕화면 정령(Unity 오버레이) 연결.</summary>
public sealed partial class MainViewModel
{
    private SpiritHost? _spirit;

    /// <summary>정령을 눌렀을 때 (UI 스레드에서 호출). 창 쪽에서 작은 창을 띄운다.</summary>
    public event Action<SpiritClick>? SpiritClicked;

    /// <summary>
    /// 정령 실행 파일 위치: 환경 변수 TODOSPIRITS_SPIRIT → 앱 폴더의 Spirit\TodoSpirit.exe
    /// → 개발 중에는 위쪽 폴더의 spirit-player\TodoSpirit.exe (Build-Spirit.ps1 의 출력).
    /// </summary>
    private static string FindSpiritExe()
    {
        var env = Environment.GetEnvironmentVariable("TODOSPIRITS_SPIRIT");
        if (!string.IsNullOrWhiteSpace(env)) return env;
        var beside = Path.Combine(AppContext.BaseDirectory, "Spirit", "TodoSpirit.exe");
        if (File.Exists(beside)) return beside;
        for (var dir = new DirectoryInfo(AppContext.BaseDirectory); dir is not null; dir = dir.Parent)
        {
            var candidate = Path.Combine(dir.FullName, "spirit-player", "TodoSpirit.exe");
            if (File.Exists(candidate)) return candidate;
        }
        return beside;
    }

    private void InitSpirit()
    {
        var logs = Path.Combine(Path.GetDirectoryName(_store.FilePath)!, "logs");
        _spirit = new SpiritHost(FindSpiritExe(), logs);
        var ui = Application.Current.Dispatcher;
        _spirit.StatusChanged += s => ui.BeginInvoke(() => { SpiritStatusText = s.Detail; Raise(nameof(SpiritStatusText)); });
        _spirit.Clicked += c => ui.BeginInvoke(() => SpiritClicked?.Invoke(c));
        SpiritStatusText = _spirit.Status.Detail;
        if (_data.SpiritEnabled) _ = _spirit.StartAsync();
    }

    public string SpiritStatusText { get; private set; } = "";

    /// <summary>바탕화면 정령을 부르거나 쉬게 한다.</summary>
    public bool SpiritEnabled
    {
        get => _data.SpiritEnabled;
        set
        {
            if (_data.SpiritEnabled == value) return;
            _data.SpiritEnabled = value; Persist(); Raise();
            if (_spirit is null) return;
            _ = value ? _spirit.StartAsync() : _spirit.StopAsync();
        }
    }

    /// <summary>앱을 닫을 때: 정령에게 종료를 알리고 잠깐 기다린다. 안 꺼져도 정령은 부모가 사라지면 스스로 꺼진다.</summary>
    public void ShutdownSpirit()
    {
        try { _spirit?.StopAsync().Wait(TimeSpan.FromSeconds(4)); }
        catch (AggregateException) { }
    }

    // 정령 옆 작은 창에 보이는 오늘 일정
    public IReadOnlyList<TaskRow> TodayRows => [.. AllDay, .. Timed];
    public bool TodayEmpty => TodayTotal == 0;
    public string TodayStat => TodayTotal == 0 ? Fmt.Short(Today) : $"{Fmt.Short(Today)} · {Fmt.Stat(TodayTotal, TodayDone)}";
}
