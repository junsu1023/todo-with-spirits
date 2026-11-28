using System.Windows;
using System.Windows.Input;
using System.Windows.Interop;
using System.Windows.Media;
using TodoSpirits.Windows.Services;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class SpiritPopup : Window
{
    private readonly MainViewModel _vm;
    private readonly Window _main;
    private bool _closing;

    public SpiritPopup(MainViewModel vm, Window main, SpiritClick click)
    {
        InitializeComponent();
        _vm = vm; _main = main;
        DataContext = vm;
        TodayPane.Visibility = click.IsMenu ? Visibility.Collapsed : Visibility.Visible;
        MenuPane.Visibility = click.IsMenu ? Visibility.Visible : Visibility.Collapsed;
        if (click.IsMenu) Width = 220;
        Loaded += (_, _) => Place(click);
    }

    /// <summary>정령 옆(기본은 위쪽)에 놓되 화면 밖으로 나가지 않게 한다. 정령 좌표는 실제 픽셀이라 DIP 로 바꾼다.</summary>
    private void Place(SpiritClick click)
    {
        var dpi = VisualTreeHelper.GetDpi(this);
        double sx = click.X / dpi.DpiScaleX, sy = click.Y / dpi.DpiScaleY;
        double sw = click.Width / dpi.DpiScaleX, sh = click.Height / dpi.DpiScaleY;
        var area = SystemParameters.WorkArea; // 주 모니터 기준. 다른 모니터에서는 아래 보정만 적용된다.
        double left = sx + sw / 2 - ActualWidth / 2;
        double top = sy - ActualHeight + 8;
        if (top < area.Top && sy + sh + ActualHeight <= area.Bottom) top = sy + sh - 8;
        if (sx >= area.Left && sx <= area.Right)
        {
            left = Math.Max(area.Left, Math.Min(left, area.Right - ActualWidth));
            top = Math.Max(area.Top, Math.Min(top, area.Bottom - ActualHeight));
        }
        Left = left; Top = top;
        Activate();
    }

    private void CloseOnce()
    {
        if (_closing) return;
        _closing = true;
        Close();
    }

    private void OnDeactivated(object? sender, EventArgs e) => CloseOnce();

    private void OnKeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key == Key.Escape) CloseOnce();
    }

    private void ShowMain()
    {
        if (_main.WindowState == WindowState.Minimized) _main.WindowState = WindowState.Normal;
        _main.Show();
        _main.Activate();
    }

    private void OnOpenApp(object sender, RoutedEventArgs e) { ShowMain(); CloseOnce(); }

    private void OnAdd(object sender, RoutedEventArgs e)
    {
        ShowMain();
        _vm.NavigateCommand.Execute("home");
        _vm.AddCommand.Execute(null);
        CloseOnce();
    }

    private void OnShowToday(object sender, RoutedEventArgs e)
    {
        MenuPane.Visibility = Visibility.Collapsed;
        TodayPane.Visibility = Visibility.Visible;
        Width = 300;
    }

    private void OnRest(object sender, RoutedEventArgs e) { _vm.SpiritEnabled = false; CloseOnce(); }

    private void OnQuit(object sender, RoutedEventArgs e) { CloseOnce(); if (_main is MainWindow main) main.Quit(); else _main.Close(); }

    protected override void OnSourceInitialized(EventArgs e)
    {
        base.OnSourceInitialized(e);
        _ = new WindowInteropHelper(this).Handle; // 핸들을 먼저 만들어 Activate 가 바로 듣게 한다
    }
}
