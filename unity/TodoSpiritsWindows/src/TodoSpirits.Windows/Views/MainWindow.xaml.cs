using System.ComponentModel;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Threading;
using TodoSpirits.Windows.ViewModels;
using Forms = System.Windows.Forms;

namespace TodoSpirits.Windows.Views;

public partial class MainWindow : Window
{
    private const double NavWidth = 212;
    private static readonly IEasingFunction Ease = new CubicEase { EasingMode = EasingMode.EaseOut };
    private static readonly Duration Slide = TimeSpan.FromMilliseconds(320);

    private readonly MainViewModel _vm;
    private readonly Forms.NotifyIcon _tray;
    private readonly DispatcherTimer _peekTimer = new() { Interval = TimeSpan.FromMilliseconds(220) };
    private Guid _lastNoti;
    private bool _quitting;

    // 지금 화면에 반영해 둔 배치 값. 값이 달라질 때만 움직인다
    private double _navSpace = NavWidth, _panelSpace;
    private bool _navShown = true, _rightShown, _notiShown, _layoutReady;

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
        menu.Items.Add("종료", null, (_, _) => Quit());
        _tray.ContextMenuStrip = menu;
        _vm.AlarmFired += (title, body, id) =>
        {
            _lastNoti = id;
            _tray.ShowBalloonTip(8000, title, body, Forms.ToolTipIcon.None);
        };

        _vm.PropertyChanged += (_, _) => SyncLayout(animate: true);
        _vm.ToastShown += () => { if (Toast.IsVisible) Motion.Play(Toast, "Toast"); };
        _peekTimer.Tick += (_, _) => { _peekTimer.Stop(); if (!_vm.NavPeekSticky && !NavHost.IsMouseOver && !NavEdge.IsMouseOver) _vm.NavPeek = false; };
        SizeChanged += (_, _) => _vm.SetWindowWidth(ActualWidth);
        Loaded += (_, _) => { _vm.SetWindowWidth(ActualWidth); _layoutReady = true; SyncLayout(animate: false); };
        PreviewKeyDown += OnPreviewKeyDown;

        Activated += (_, _) => _vm.RefreshServerOnActivation();
        Closed += (_, _) => { _tray.Visible = false; _tray.Dispose(); _vm.ShutdownSpirit(); _vm.DisposeServer(); };
        StateChanged += (_, _) => Root.Padding = WindowState == WindowState.Maximized ? new Thickness(7) : new Thickness(0);
    }

    // ---------- 배치: 사이드바 · 오른쪽 패널 · 알림이 밀려 나오고 들어가는 움직임 (시안 .32s cubic-bezier) ----------
    private void SyncLayout(bool animate)
    {
        if (!_layoutReady) return;
        animate &= Motion.Enabled;

        Main.Margin = _vm.IsNarrow ? new Thickness(22, 0, 22, 0) : new Thickness(28, 0, 28, 0);

        // 사이드바: 고정이면 자리를 차지하고, 숨김이면 자리 0. 잠깐 보기는 자리를 차지하지 않고 위에 뜬다
        double navSpace = _vm.NavPinned ? NavWidth : 0;
        if (navSpace != _navSpace) { _navSpace = navSpace; AnimateWidth(NavSpace, navSpace, animate); }
        if (_vm.NavShown != _navShown)
        {
            _navShown = _vm.NavShown;
            Move(NavShift, _navShown ? 0 : -NavWidth - 40, animate);
        }
        bool floating = _vm.NavPeek && !_vm.NavPinned;
        NavHost.SetResourceReference(Border.BackgroundProperty, floating ? "PanelBg" : "Back01");
        NavHost.CornerRadius = floating ? new CornerRadius(0, 22, 22, 0) : new CornerRadius(0);
        if (floating) NavHost.SetResourceReference(EffectProperty, "SideShadow"); else NavHost.Effect = null;

        // 오른쪽 패널
        double width = _vm.PanelWidth;
        if (RightHost.Width != width) RightHost.Width = width;
        bool overlay = _vm.PanelOverlay;
        RightHost.CornerRadius = overlay ? new CornerRadius(22, 0, 0, 22) : new CornerRadius(0);
        RightHost.ClipToBounds = overlay;
        if (overlay) RightHost.SetResourceReference(EffectProperty, "SideShadow"); else RightHost.Effect = null;
        double panelSpace = _vm.PanelSpace;
        if (panelSpace != _panelSpace) { _panelSpace = panelSpace; AnimateWidth(PanelSpace, panelSpace, animate); }
        if (_vm.RightShown != _rightShown)
        {
            _rightShown = _vm.RightShown;
            ShowSide(RightHost, RightShift, _rightShown, width + 40, animate);
        }

        // 알림
        if (_vm.NotiOpen != _notiShown)
        {
            _notiShown = _vm.NotiOpen;
            ShowSide(NotiHost, NotiShift, _notiShown, 440, animate);
            NotiScrim.Visibility = _notiShown ? Visibility.Visible : Visibility.Collapsed;
            if (_notiShown && animate) NotiScrim.BeginAnimation(OpacityProperty, new DoubleAnimation(0, 1, TimeSpan.FromMilliseconds(200)));
        }
    }

    private static void AnimateWidth(FrameworkElement element, double to, bool animate)
    {
        if (!animate) { element.BeginAnimation(WidthProperty, null); element.Width = to; return; }
        element.BeginAnimation(WidthProperty, new DoubleAnimation(element.ActualWidth, to, Slide) { EasingFunction = Ease });
    }

    private static void Move(TranslateTransform shift, double to, bool animate)
    {
        if (!animate) { shift.BeginAnimation(TranslateTransform.XProperty, null); shift.X = to; return; }
        shift.BeginAnimation(TranslateTransform.XProperty, new DoubleAnimation(to, Slide) { EasingFunction = Ease });
    }

    /// <summary>오른쪽에서 밀려 나오는 것(패널 · 알림)을 보이거나 숨긴다. 숨길 때는 다 밀려 나간 뒤에 치운다.</summary>
    private static void ShowSide(FrameworkElement host, TranslateTransform shift, bool show, double distance, bool animate)
    {
        if (show)
        {
            host.Visibility = Visibility.Visible;
            if (!animate) { shift.BeginAnimation(TranslateTransform.XProperty, null); shift.X = 0; return; }
            shift.BeginAnimation(TranslateTransform.XProperty, new DoubleAnimation(distance, 0, Slide) { EasingFunction = Ease });
            return;
        }
        if (!animate) { shift.BeginAnimation(TranslateTransform.XProperty, null); host.Visibility = Visibility.Collapsed; return; }
        var slide = new DoubleAnimation(distance, TimeSpan.FromMilliseconds(240)) { EasingFunction = Ease };
        slide.Completed += (_, _) => { if (shift.X >= distance - 1) host.Visibility = Visibility.Collapsed; };
        shift.BeginAnimation(TranslateTransform.XProperty, slide);
    }

    // ---------- 사이드바 잠깐 보기 ----------
    private void OnNavEdgeEnter(object sender, MouseEventArgs e) { _peekTimer.Stop(); _vm.NavPeek = true; }
    private void OnNavEnter(object sender, MouseEventArgs e) => _peekTimer.Stop();
    private void OnNavLeave(object sender, MouseEventArgs e) { if (_vm.NavPeek && !_vm.NavPeekSticky) { _peekTimer.Stop(); _peekTimer.Start(); } }

    /// <summary>메뉴 버튼으로 띄운 사이드바는 바깥을 누르면 닫힌다.</summary>
    private void OnShellMouseDown(object sender, MouseButtonEventArgs e)
    {
        if (_vm.NavPeek && !NavHost.IsMouseOver) _vm.NavPeek = false;
    }

    private void OnNotiScrimDown(object sender, MouseButtonEventArgs e) => _vm.NotiOpen = false;

    // ---------- 키보드 ----------
    /// <summary>
    /// 글자 키와 방향키로 하는 단축키. 입력 칸에 글자를 치는 중에는 가로채지 않는다.
    /// (Ctrl+N/F/B, F1, Esc 는 XAML 의 KeyBinding)
    /// </summary>
    private void OnPreviewKeyDown(object sender, KeyEventArgs e)
    {
        var focus = Keyboard.FocusedElement as DependencyObject;
        if (focus is TextBoxBase or PasswordBox) return;
        if (Keyboard.Modifiers is not (ModifierKeys.None or ModifierKeys.Shift)) return;
        bool shift = Keyboard.Modifiers == ModifierKeys.Shift;

        if (e.Key == Key.OemQuestion && shift) { _vm.ToggleHelpCommand.Execute(null); e.Handled = true; return; }
        if (_vm.HelpOpen || shift) return;

        // 일정 카드에 초점이 있을 때: Space = 완료, Delete = 삭제 (Enter 는 버튼의 기본 동작 = 상세)
        if (focus is Button { DataContext: TaskRow row, Command: not null } card && ReferenceEquals(card.Tag, _vm))
        {
            if (e.Key == Key.Space) { _vm.ToggleCommand.Execute(row); e.Handled = true; return; }
            if (e.Key == Key.Delete) { _vm.DeleteRowCommand.Execute(row); e.Handled = true; return; }
        }

        if (_vm.Page != "calendar" || _vm.NotiOpen) return;
        // 패널 안(시간 휠 · 날짜 고르기)이나 목록 · 버튼 묶음에서는 방향키를 그쪽이 쓴다
        bool inPanel = focus is not null && IsInside(focus, RightHost);
        switch (e.Key)
        {
            case Key.W: _vm.SetModeCommand.Execute("week"); break;
            case Key.M: _vm.SetModeCommand.Execute("month"); break;
            case Key.Y: _vm.SetModeCommand.Execute("year"); break;
            case Key.PageUp: _vm.ShiftPeriod(-1); break;
            case Key.PageDown: _vm.ShiftPeriod(1); break;
            case Key.Left when !inPanel && _vm.CalMode != "year": _vm.ShiftDay(-1); break;
            case Key.Right when !inPanel && _vm.CalMode != "year": _vm.ShiftDay(1); break;
            case Key.Up when !inPanel && _vm.CalMode == "month": _vm.ShiftDay(-7); break;
            case Key.Down when !inPanel && _vm.CalMode == "month": _vm.ShiftDay(7); break;
            default: return;
        }
        e.Handled = true;
    }

    private static bool IsInside(DependencyObject element, DependencyObject ancestor)
    {
        for (DependencyObject? d = element; d is not null; d = d is Visual or System.Windows.Media.Media3D.Visual3D ? VisualTreeHelper.GetParent(d) : LogicalTreeHelper.GetParent(d))
            if (ReferenceEquals(d, ancestor)) return true;
        return false;
    }

    // ---------- 트레이 ----------
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

    /// <summary>숨어 있거나 최소화된 창을 앞으로 부른다 (트레이, 알림, 앱을 한 번 더 실행했을 때).</summary>
    public void ShowFromTray()
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
