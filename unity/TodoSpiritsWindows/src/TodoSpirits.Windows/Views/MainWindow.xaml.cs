using System.Windows;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
        var vm = new MainViewModel();
        DataContext = vm;
        // 바탕화면 정령을 누르면 그 옆에 작은 창을 띄운다 (좌클릭 = 오늘 일정, 우클릭 = 메뉴)
        vm.SpiritClicked += click => new SpiritPopup(vm, this, click).Show();
        Closed += (_, _) => vm.ShutdownSpirit();
        StateChanged += (_, _) => Root.Padding = WindowState == WindowState.Maximized ? new Thickness(7) : new Thickness(0);
    }

    private void OnMinimize(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    private void OnMaximize(object sender, RoutedEventArgs e) => WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
    private void OnClose(object sender, RoutedEventArgs e) => Close();
}
