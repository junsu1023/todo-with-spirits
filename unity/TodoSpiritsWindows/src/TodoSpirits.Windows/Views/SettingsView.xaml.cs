using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;

namespace TodoSpirits.Windows.Views;

public partial class SettingsView : UserControl
{
    public SettingsView() => InitializeComponent();

    // Enter 를 누르면 입력한 값을 바로 저장한다
    private void OnCommitOnEnter(object sender, KeyEventArgs e)
    {
        if (e.Key == Key.Enter && sender is TextBox box)
            BindingOperations.GetBindingExpression(box, TextBox.TextProperty)?.UpdateSource();
    }
}
