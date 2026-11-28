using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Threading;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class TaskPanel : UserControl
{
    public TaskPanel() => InitializeComponent();

    // 새 일정을 열면 바로 제목을 칠 수 있게 한다
    private void OnVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (e.NewValue is true && DataContext is MainViewModel { IsNew: true })
            Dispatcher.BeginInvoke(DispatcherPriority.Input, () => TitleBox.Focus());
    }

    private void OnTitleKeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key == Key.Enter && DataContext is MainViewModel vm && vm.SaveCommand.CanExecute(null))
            vm.SaveCommand.Execute(null);
    }

    // 시간 휠: 위로 굴리면 앞의 값, 아래로 굴리면 다음 값. 패널 전체가 같이 스크롤되지 않게 막는다
    private void OnHourWheel(object sender, MouseWheelEventArgs e)
    {
        if (DataContext is not MainViewModel vm) return;
        (e.Delta > 0 ? vm.HourDownCommand : vm.HourUpCommand).Execute(null);
        e.Handled = true;
    }

    private void OnMinuteWheel(object sender, MouseWheelEventArgs e)
    {
        if (DataContext is not MainViewModel vm) return;
        (e.Delta > 0 ? vm.MinuteDownCommand : vm.MinuteUpCommand).Execute(null);
        e.Handled = true;
    }
}
