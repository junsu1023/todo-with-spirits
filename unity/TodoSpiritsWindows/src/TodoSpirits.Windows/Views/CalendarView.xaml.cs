using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class CalendarView : UserControl
{
    public CalendarView() => InitializeComponent();

    // 월간에서 날짜를 두 번 누르면 그 날 일정 추가
    private void OnDayDoubleClick(object sender, MouseButtonEventArgs e)
    {
        if (sender is FrameworkElement { DataContext: DayCell cell } && DataContext is MainViewModel vm)
            vm.AddCommand.Execute(cell);
    }
}
