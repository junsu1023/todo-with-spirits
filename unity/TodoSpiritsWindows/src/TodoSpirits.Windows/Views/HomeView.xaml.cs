using System.Windows.Controls;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

public partial class HomeView : UserControl
{
    private MainViewModel? _vm;

    public HomeView()
    {
        InitializeComponent();
        DataContextChanged += (_, _) =>
        {
            if (_vm is not null) _vm.SummaryBumped -= OnBump;
            _vm = DataContext as MainViewModel;
            if (_vm is not null) _vm.SummaryBumped += OnBump;
        };
    }

    // 일정을 끝내면 남은 수가 살짝 튄다 (시안 bump)
    private void OnBump() { if (IsVisible) Motion.Play(LeftNum, "Bump"); }
}
