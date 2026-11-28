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
}
