using System.ComponentModel;
using System.Net.Mail;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

/// <summary>
/// 설정 화면. 어떤 창을 보여 줄지는 화면 상태(SettingsSection)가 정하고,
/// 여기서는 목록에서 고른 줄 표시 · 창이 좌우로 들어오는 움직임 · 위아래 화살표 이동처럼 화면에만 속한 일을 한다.
/// </summary>
public partial class SettingsView : UserControl
{
    private MainViewModel? _vm;
    private string? _shown;

    public SettingsView()
    {
        InitializeComponent();
        DataContextChanged += (_, _) => Hook();
        Loaded += (_, _) => Hook();
    }

    private void Hook()
    {
        if (ReferenceEquals(_vm, DataContext)) return;
        if (_vm is not null) _vm.PropertyChanged -= OnVmChanged;
        _vm = DataContext as MainViewModel;
        if (_vm is not null) _vm.PropertyChanged += OnVmChanged;
        ShowSection();
    }

    private void OnVmChanged(object? sender, PropertyChangedEventArgs e)
    {
        if (_vm is null) return;
        if (e.PropertyName == nameof(MainViewModel.IsSignedIn) || e.PropertyName == nameof(MainViewModel.AccountText))
        {
            NewAccountEmail.Clear();
            AccountVerificationCode.Clear();
        }
        if (string.IsNullOrEmpty(e.PropertyName) || e.PropertyName == nameof(MainViewModel.SettingsSection))
        {
            ShowSection();
        }
        else if (_vm.Page == "settings" && e.PropertyName is nameof(MainViewModel.IsDark) or nameof(MainViewModel.FollowSystem)
                     or nameof(MainViewModel.ContrastMore) or nameof(MainViewModel.SpiritEnabled) or nameof(MainViewModel.CloseToTray))
        {
            // 다른 곳에서 저장하는 설정도 이 화면에서 바꿨으면 "저장됨"을 띄운다
            _vm.FlashSaved();
        }
    }

    /// <summary>목록에서 고른 줄을 표시하고, 창이 바뀌었으면 목록 순서에 따라 오른쪽/왼쪽에서 들어오게 한다.</summary>
    private void ShowSection()
    {
        if (_vm is null) return;
        var now = _vm.SettingsSection;
        foreach (var button in SectionList.Children.OfType<Button>())
            if (button.CommandParameter is string key) Ui.SetSelected(button, key == now);

        if (now == _shown) return;
        var first = _shown is null;
        var back = !first && MainViewModel.SectionIndex(now) < MainViewModel.SectionIndex(_shown!);
        _shown = now;
        PaneScroll.ScrollToTop();
        if (!first && IsVisible) Motion.Play(PaneHost, back ? "SlideBack" : "SlideIn");
    }

    // 목록에서 ↑ ↓ 로 옆 창으로 옮겨 간다 (시안과 같다)
    private void OnListKeyDown(object sender, KeyEventArgs e)
    {
        if (_vm is null || e.Key is not (Key.Down or Key.Up)) return;
        var order = MainViewModel.SettingsOrder;
        var i = MainViewModel.SectionIndex(_vm.SettingsSection) + (e.Key == Key.Down ? 1 : -1);
        if (i < 0 || i >= order.Count) return;
        _vm.SettingsSection = order[i];
        SectionList.Children.OfType<Button>().FirstOrDefault(b => b.CommandParameter as string == order[i])?.Focus();
        e.Handled = true;
    }

    // 닉네임 칸에서 Enter = "확인"
    private void OnNickKeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key != Key.Enter || _vm is null) return;
        if (_vm.ConfirmNicknameCommand.CanExecute(null)) _vm.ConfirmNicknameCommand.Execute(null);
        e.Handled = true;
    }

    private async void OnLoginClick(object sender, RoutedEventArgs e) => await Login();

    private async void OnAccountEnter(object sender, KeyEventArgs e)
    {
        if (e.Key != Key.Enter) return;
        e.Handled = true;
        await Login();
    }

    private async Task Login()
    {
        if (_vm?.CanSignIn != true || !TryEmail(out var email)) return;
        var password = AccountPassword.Password;
        if (password.Length == 0) { AccountPassword.Focus(); return; }
        AccountPassword.Clear();
        await _vm.SignIn(email, password);
    }

    private async void OnSendSignupCode(object sender, RoutedEventArgs e)
    {
        if (_vm?.CanSignIn == true && TryEmail(out var email)) await _vm.SendSignupCode(email);
    }

    private async void OnRegisterClick(object sender, RoutedEventArgs e)
    {
        if (_vm?.CanSignIn != true || !TryEmail(out var email)) return;
        var password = AccountPassword.Password;
        if (password.Length is < 8 or > 20 || !password.Any(char.IsAsciiLetter) || !password.Any(char.IsAsciiDigit)
            || password.Any(c => !char.IsAsciiLetterOrDigit(c) && !"!@#$%^&*()_+=-".Contains(c)))
        {
            MessageBox.Show(Window.GetWindow(this), "비밀번호는 영문자와 숫자를 포함해 8~20자로 입력해 주세요.", "회원가입");
            AccountPassword.Focus();
            return;
        }
        if (SignupCode.Text.Trim() is not { Length: 6 } code || !code.All(char.IsAsciiDigit))
        {
            SignupCode.Focus();
            return;
        }
        AccountPassword.Clear();
        if (await _vm.RegisterAccount(email, password, code)) SignupCode.Clear();
    }

    private bool TryEmail(out string email)
    {
        return TryEmail(AccountEmail, out email);
    }

    private static bool TryEmail(TextBox input, out string email)
    {
        email = input.Text.Trim();
        if (MailAddress.TryCreate(email, out var address) && address.Address == email) return true;
        input.Focus();
        return false;
    }

    private async void OnChangeAccountEmail(object sender, RoutedEventArgs e)
    {
        if (_vm?.CanUseAccount == true && TryEmail(NewAccountEmail, out var email))
            await _vm.ChangeAccountEmail(email);
    }

    private async void OnSendAccountVerification(object sender, RoutedEventArgs e)
    {
        if (_vm is { } vm) await vm.SendAccountVerification();
    }

    private async void OnVerifyAccountEmail(object sender, RoutedEventArgs e)
    {
        var code = AccountVerificationCode.Text.Trim();
        if (code.Length != 6 || !code.All(char.IsAsciiDigit)) { AccountVerificationCode.Focus(); return; }
        if (_vm is { } vm)
        {
            await vm.VerifyAccountEmail(code);
            if (!vm.NeedsEmailVerification) AccountVerificationCode.Clear();
        }
    }

    private async void OnLoadSubscription(object sender, RoutedEventArgs e)
    {
        if (_vm is { } vm) await vm.LoadSubscription();
    }

    private async void OnWithdrawClick(object sender, RoutedEventArgs e)
    {
        if (_vm?.CanUseAccount != true) return;
        if (MessageBox.Show(Window.GetWindow(this), "계정과 서버에 저장된 데이터가 삭제돼요. 탈퇴할까요?",
            "회원탈퇴", MessageBoxButton.YesNo, MessageBoxImage.Warning, MessageBoxResult.No) == MessageBoxResult.Yes)
            await _vm.WithdrawAccount();
    }

    // 언어: 값 아래로 작은 메뉴가 열린다. 고른 값은 분홍 굵은 글자 + 체크
    private void OnLanguageClick(object sender, RoutedEventArgs e)
    {
        if (_vm is null || sender is not Button button) return;
        var menu = new ContextMenu
        {
            PlacementTarget = button, Placement = PlacementMode.Bottom,
            HorizontalOffset = button.ActualWidth - 212, // 메뉴(최소 196 + 그림자 여백 16)의 오른쪽 끝을 버튼에 맞춘다
        };
        foreach (var language in _vm.Languages)
        {
            var item = new MenuItem { Header = language, Command = _vm.SetLanguageCommand, CommandParameter = language };
            if (language == _vm.Language)
            {
                item.FontWeight = FontWeights.ExtraBold;
                item.SetResourceReference(ForegroundProperty, "PinkB");
                item.Tag = FindResource("IcCheck");
            }
            menu.Items.Add(item);
        }
        menu.IsOpen = true;
    }

    // 로그아웃 · 탈퇴 창의 정령이 둥실 떠 있는다 (시안 float 3.2s). 보일 때만 돌린다
    private void OnArtVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (sender is not Image art) return;
        if (e.NewValue is true && Motion.Enabled)
        {
            var shift = new TranslateTransform();
            art.RenderTransform = shift;
            shift.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimation(0, -8, TimeSpan.FromSeconds(1.6))
            {
                AutoReverse = true, RepeatBehavior = RepeatBehavior.Forever, EasingFunction = new SineEase { EasingMode = EasingMode.EaseInOut },
            });
        }
        else
        {
            art.RenderTransform = Transform.Identity;
        }
    }
}
