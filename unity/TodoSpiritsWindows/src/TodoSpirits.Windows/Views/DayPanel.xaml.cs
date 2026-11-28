using System.Windows.Controls;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

/// <summary>월간 캘린더 오른쪽의 날짜 패널: 고른 날의 일정 목록.</summary>
public partial class DayPanel : UserControl
{
    private MainViewModel? _vm;

    public DayPanel()
    {
        InitializeComponent();
        DataContextChanged += (_, _) =>
        {
            if (_vm is not null) _vm.DayChanged -= OnDayChanged;
            _vm = DataContext as MainViewModel;
            if (_vm is not null) _vm.DayChanged += OnDayChanged;
        };
    }

    // 날짜를 바꾸면 목록이 간 방향에서 밀려 들어온다
    private void OnDayChanged(int direction) { if (IsVisible) Motion.Play(Body, direction >= 0 ? "SlideIn" : "SlideBack"); }
}
