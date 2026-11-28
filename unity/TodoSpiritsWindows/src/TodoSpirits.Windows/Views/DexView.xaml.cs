using System.Windows.Controls;
using System.Windows.Input;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class DexView : UserControl
{
    public DexView() => InitializeComponent();

    // 만난 정령을 두 번 누르면 바로 대표 정령으로
    private void OnSpiritDoubleClick(object sender, MouseButtonEventArgs e)
    {
        if (DataContext is MainViewModel vm && vm.SetRepresentativeCommand.CanExecute(null))
            vm.SetRepresentativeCommand.Execute(null);
    }
}
