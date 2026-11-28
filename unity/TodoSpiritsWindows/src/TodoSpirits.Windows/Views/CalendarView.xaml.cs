using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class CalendarView : UserControl
{
    private MainViewModel? _vm;

    public CalendarView()
    {
        InitializeComponent();
        Tag = 4; // 연간 열 수 (XAML 의 UniformGrid 가 읽는다)
        SizeChanged += (_, _) => { int columns = ActualWidth <= 820 ? 3 : 4; if (!Equals(Tag, columns)) Tag = columns; };
        DataContextChanged += (_, _) =>
        {
            if (_vm is not null) { _vm.CalMoved -= OnMoved; _vm.DayChanged -= OnDayChanged; }
            _vm = DataContext as MainViewModel;
            if (_vm is not null) { _vm.CalMoved += OnMoved; _vm.DayChanged += OnDayChanged; }
        };
    }

    // 이전/다음 · 보기 전환: 간 방향에서 밀려 들어온다 (시안 .swap / .swap-back)
    private void OnMoved(int direction)
    {
        if (!IsVisible || _vm is null) return;
        FrameworkElement view = _vm.CalMode switch { "week" => WeekView, "year" => YearView, _ => MonthView };
        Motion.Play(view, direction >= 0 ? "SlideIn" : "SlideBack");
    }

    // 주간에서 날짜를 바꾸면 아래 목록이 밀려 들어온다
    private void OnDayChanged(int direction)
    {
        if (IsVisible && _vm?.CalMode == "week") Motion.Play(WeekList, direction >= 0 ? "SlideIn" : "SlideBack");
    }

    // 월간에서 날짜를 두 번 누르면 그 날 일정 추가
    private void OnDayDoubleClick(object sender, MouseButtonEventArgs e)
    {
        if (sender is FrameworkElement { DataContext: DayCell cell } && _vm is not null)
        {
            _vm.AddCommand.Execute(cell);
            e.Handled = true;
        }
    }

    // 주간 목록의 빈 곳을 두 번 누르면 그 날 일정 추가 (카드 위에서는 카드가 먼저 받는다)
    private void OnListDoubleClick(object sender, MouseButtonEventArgs e)
    {
        if (e.OriginalSource is DependencyObject d && FindCard(d)) return;
        _vm?.AddCommand.Execute(null);
    }

    private static bool FindCard(DependencyObject d)
    {
        for (DependencyObject? x = d; x is not null; x = System.Windows.Media.VisualTreeHelper.GetParent(x))
            if (x is Button) return true;
        return false;
    }
}
