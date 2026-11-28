using System.Windows;
using System.Windows.Controls;
using System.Windows.Threading;

namespace TodoSpirits.Windows.Views;

public partial class HomeView : UserControl
{
    public HomeView() => InitializeComponent();

    // 검색 칸이 열리면 바로 칠 수 있게 한다
    private void OnSearchVisible(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (e.NewValue is true) Dispatcher.BeginInvoke(DispatcherPriority.Input, () => SearchBox.Focus());
    }
}
