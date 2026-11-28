using System.Security.Cryptography;
using System.Text;
using System.Windows;
using TodoSpirits.Windows.Services;
using TodoSpirits.Windows.Views;

namespace TodoSpirits.Windows;

public partial class App : Application
{
    private Mutex? _single;
    private EventWaitHandle? _wake;

    /// <summary>
    /// 앱은 한 번만 띄운다. 두 개가 같은 저장 파일에 번갈아 쓰면 한쪽의 변경이 사라지기 때문이다.
    /// 이미 떠 있으면(트레이에 숨어 있는 경우 포함) 그 창을 앞으로 부르고 끝낸다.
    /// 저장 위치가 다르면(시험용 TODOSPIRITS_DATA) 따로 띄울 수 있다.
    /// </summary>
    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);
        var path = new TaskStore().FilePath.ToUpperInvariant();
        var id = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(path)))[..16];
        _single = new Mutex(true, $"TodoWithSpirits.{id}", out bool first);
        _wake = new EventWaitHandle(false, EventResetMode.AutoReset, $"TodoWithSpirits.{id}.wake");
        if (!first)
        {
            _wake.Set();
            _single.Dispose(); _single = null;
            Shutdown();
            return;
        }

        var window = new MainWindow();
        MainWindow = window;
        window.Show();

        var wake = _wake;
        var listener = new Thread(() =>
        {
            try { while (wake.WaitOne()) Dispatcher.BeginInvoke(window.ShowFromTray); }
            catch (ObjectDisposedException) { }
            catch (InvalidOperationException) { }
        }) { IsBackground = true, Name = "single-instance-wake" };
        listener.Start();
    }

    protected override void OnExit(ExitEventArgs e)
    {
        _wake?.Dispose();
        if (_single is not null) { try { _single.ReleaseMutex(); } catch (ApplicationException) { } _single.Dispose(); }
        base.OnExit(e);
    }
}
