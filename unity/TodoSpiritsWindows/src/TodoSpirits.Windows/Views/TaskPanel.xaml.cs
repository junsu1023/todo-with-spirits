using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Threading;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class TaskPanel : UserControl
{
    private MainViewModel? _vm;

    public TaskPanel()
    {
        InitializeComponent();
        DataContextChanged += (_, _) =>
        {
            if (_vm is not null) _vm.PanelSwapped -= OnSwapped;
            _vm = DataContext as MainViewModel;
            if (_vm is not null) _vm.PanelSwapped += OnSwapped;
        };
    }

    // 패널이 열린 채 다른 일정으로 바뀌면 내용이 옆에서 밀려 들어온다 (시안 .swap)
    private void OnSwapped()
    {
        Motion.Play(Body, "SlideIn");
        FocusTitleIfNew();
    }

    // 새 일정을 열면 바로 제목을 칠 수 있게 한다
    private void OnVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (e.NewValue is true) FocusTitleIfNew();
    }

    private void FocusTitleIfNew()
    {
        if (_vm is { IsNew: true }) Dispatcher.BeginInvoke(DispatcherPriority.Input, () => TitleBox.Focus());
    }

    // 새 일정: Enter 로 저장. 기존 일정: Enter 로 입력을 마친다 (값은 이미 저장돼 있다)
    private void OnTitleKeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key != Key.Enter || _vm is null) return;
        if (_vm.ShowTaskSave) { if (_vm.SaveCommand.CanExecute(null)) _vm.SaveCommand.Execute(null); }
        else Keyboard.ClearFocus();
        e.Handled = true;
    }

    // 시간 휠: 위로 굴리면 앞의 값, 아래로 굴리면 다음 값. 패널 전체가 같이 스크롤되지 않게 막는다
    private void OnHourWheel(object sender, MouseWheelEventArgs e)
    {
        if (_vm is null) return;
        (e.Delta > 0 ? _vm.HourDownCommand : _vm.HourUpCommand).Execute(null);
        e.Handled = true;
    }

    private void OnMinuteWheel(object sender, MouseWheelEventArgs e)
    {
        if (_vm is null) return;
        (e.Delta > 0 ? _vm.MinuteDownCommand : _vm.MinuteUpCommand).Execute(null);
        e.Handled = true;
    }

    private void OnWheelClick(object sender, MouseButtonEventArgs e) => ((UIElement)sender).Focus();

    // 휠에 초점이 있을 때 방향키 위/아래
    private void OnHourKey(object sender, KeyEventArgs e)
    {
        if (_vm is null || e.Key is not (Key.Up or Key.Down)) return;
        (e.Key == Key.Up ? _vm.HourDownCommand : _vm.HourUpCommand).Execute(null);
        e.Handled = true;
    }

    private void OnMinuteKey(object sender, KeyEventArgs e)
    {
        if (_vm is null || e.Key is not (Key.Up or Key.Down)) return;
        (e.Key == Key.Up ? _vm.MinuteDownCommand : _vm.MinuteUpCommand).Execute(null);
        e.Handled = true;
    }
}
