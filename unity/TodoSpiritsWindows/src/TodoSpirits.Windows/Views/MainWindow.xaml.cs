using System.Windows;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
        DataContext = new MainViewModel();
        StateChanged += (_, _) => Root.Padding = WindowState == WindowState.Maximized ? new Thickness(7) : new Thickness(0);
    }

    private void OnMinimize(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    private void OnMaximize(object sender, RoutedEventArgs e) => WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
    private void OnClose(object sender, RoutedEventArgs e) => Close();
}
