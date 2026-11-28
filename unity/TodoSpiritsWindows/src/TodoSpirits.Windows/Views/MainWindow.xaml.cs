using System.ComponentModel;
using System.IO;
using System.Windows;
using TodoSpirits.Windows.ViewModels;
using Forms = System.Windows.Forms;

namespace TodoSpirits.Windows.Views;

public partial class MainWindow : Window
{
    private readonly MainViewModel _vm;
    private readonly Forms.NotifyIcon _tray;
    private Guid _lastNoti;
    private bool _quitting;

    public MainWindow()
    {
        InitializeComponent();
        _vm = new MainViewModel();
        DataContext = _vm;

        // 바탕화면 정령을 누르면 그 옆에 작은 창을 띄운다 (좌클릭 = 오늘 일정, 우클릭 = 메뉴)
        _vm.SpiritClicked += click => new SpiritPopup(_vm, this, click).Show();

        // 트레이 아이콘: Windows 알림을 띄우고, 창을 닫아도 남아 있을 자리
        _tray = new Forms.NotifyIcon { Text = "Todo with Spirits", Icon = LoadTrayIcon(), Visible = true };
        _tray.DoubleClick += (_, _) => ShowFromTray();
        _tray.BalloonTipClicked += (_, _) => { ShowFromTray(); _vm.OpenNotification(_lastNoti); };
        var menu = new Forms.ContextMenuStrip();
        menu.Items.Add("열기", null, (_, _) => ShowFromTray());
        menu.Items.Add("종료", null, (_, _) => { _quitting = true; Close(); });
        _tray.ContextMenuStrip = menu;
        _vm.AlarmFired += (title, body, id) =>
        {
            _lastNoti = id;
            _tray.ShowBalloonTip(8000, title, body, Forms.ToolTipIcon.None);
        };

        Closed += (_, _) => { _tray.Visible = false; _tray.Dispose(); _vm.ShutdownSpirit(); };
        StateChanged += (_, _) => Root.Padding = WindowState == WindowState.Maximized ? new Thickness(7) : new Thickness(0);
    }

    /// <summary>정령 그림으로 트레이 아이콘을 만든다. 실패하면 Windows 기본 아이콘.</summary>
    private static System.Drawing.Icon LoadTrayIcon()
    {
        try
        {
            var resource = Application.GetResourceStream(new Uri("pack://application:,,,/Assets/spirit_neutral.png"));
            if (resource is null) return System.Drawing.SystemIcons.Application;
            using var stream = resource.Stream;
            using var source = new System.Drawing.Bitmap(stream);
            using var small = new System.Drawing.Bitmap(source, new System.Drawing.Size(32, 32));
            return System.Drawing.Icon.FromHandle(small.GetHicon());
        }
        catch (Exception ex) when (ex is IOException or ArgumentException or System.Runtime.InteropServices.ExternalException)
        {
            return System.Drawing.SystemIcons.Application;
        }
    }

    private void ShowFromTray()
    {
        Show();
        if (WindowState == WindowState.Minimized) WindowState = WindowState.Normal;
        Activate();
    }

    /// <summary>설정에서 "창을 닫으면 트레이로"를 켰으면 닫는 대신 숨긴다. 트레이 메뉴의 종료나 정령 메뉴의 앱 종료는 정말 끝낸다.</summary>
    protected override void OnClosing(CancelEventArgs e)
    {
        if (_vm.CloseToTray && !_quitting)
        {
            e.Cancel = true;
            Hide();
            _tray.ShowBalloonTip(4000, "Todo with Spirits", "트레이에서 계속 실행 중이에요. 알림과 정령은 그대로 있어요.", Forms.ToolTipIcon.None);
        }
        base.OnClosing(e);
    }

    /// <summary>정령 메뉴의 "앱 종료"처럼, 트레이로 숨기지 않고 정말 끝낼 때.</summary>
    public void Quit() { _quitting = true; Close(); }

    private void OnMinimize(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    private void OnMaximize(object sender, RoutedEventArgs e) => WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
    private void OnClose(object sender, RoutedEventArgs e) => Close();
}
