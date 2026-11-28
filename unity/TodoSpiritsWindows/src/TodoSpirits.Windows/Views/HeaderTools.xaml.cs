using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Animation;
using System.Windows.Threading;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

/// <summary>머리줄 오른쪽 도구: 검색 칸(열리면 230) · 검색 버튼 · 알림 종. 홈과 캘린더가 같이 쓴다.</summary>
public partial class HeaderTools : UserControl
{
    private MainViewModel? _vm;

    public HeaderTools()
    {
        InitializeComponent();
        DataContextChanged += (_, _) =>
        {
            if (_vm is not null) { _vm.SearchFocusRequested -= FocusBox; _vm.PropertyChanged -= OnVmChanged; }
            _vm = DataContext as MainViewModel;
            if (_vm is not null) { _vm.SearchFocusRequested += FocusBox; _vm.PropertyChanged += OnVmChanged; }
            Sync(animate: false);
        };
        IsVisibleChanged += (_, _) => Sync(animate: false);
    }

    private bool _open;

    private void OnVmChanged(object? sender, System.ComponentModel.PropertyChangedEventArgs e)
    {
        if (e.PropertyName is nameof(MainViewModel.SearchOpen) or "" or null) Sync(animate: true);
    }

    // 검색 칸은 너비가 0 ↔ 230 으로 늘었다 줄었다 한다 (시안 .search width .2s)
    private void Sync(bool animate)
    {
        bool open = _vm?.SearchOpen == true;
        if (open == _open && animate) return;
        _open = open;
        double to = open ? 230 : 0;
        if (animate && Motion.Enabled && IsVisible)
            Pill.BeginAnimation(WidthProperty, new DoubleAnimation(Pill.ActualWidth, to, TimeSpan.FromMilliseconds(200)) { EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut } });
        else { Pill.BeginAnimation(WidthProperty, null); Pill.Width = to; }
        if (open && IsVisible && animate) FocusBox();
    }

    private void FocusBox()
    {
        if (IsVisible) Dispatcher.BeginInvoke(DispatcherPriority.Input, () => { Box.Focus(); Box.SelectAll(); });
    }
}
