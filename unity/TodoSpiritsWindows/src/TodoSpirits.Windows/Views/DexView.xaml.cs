using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Threading;
using TodoSpirits.Windows.ViewModels;

namespace TodoSpirits.Windows.Views;

/// <summary>너비에 비율을 곱한다 (정사각형 칸, 1.15:1 그림 칸, 칸의 78% 그림).</summary>
public sealed class DexRatio : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture) =>
        value is double width && double.TryParse(parameter?.ToString(), NumberStyles.Float, CultureInfo.InvariantCulture, out var ratio)
            ? Math.Max(0, width * ratio) : 0.0;

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture) => Binding.DoNothing;
}

/// <summary>정령 도감 화면 (시안 dex-v4.html). 칸 수 · 패널 너비 · 키보드 이동 · 움직임을 맡는다.</summary>
public partial class DexView : UserControl
{
    /// <summary>목록의 칸 수: 목록 너비가 560 이하면 3, 420 이하면 2.</summary>
    public static readonly DependencyProperty ColumnsProperty = DependencyProperty.Register(
        nameof(Columns), typeof(int), typeof(DexView), new PropertyMetadata(4));
    public int Columns { get => (int)GetValue(ColumnsProperty); set => SetValue(ColumnsProperty, value); }

    private Window? _window;
    private DateTime _sortClosedAt;

    public DexView()
    {
        InitializeComponent();
        Loaded += (_, _) => WatchWindow(Window.GetWindow(this));
        Unloaded += (_, _) => WatchWindow(null);
        IsVisibleChanged += OnVisibleChanged;
        HeroArt.IsVisibleChanged += (_, e) => Float(HeroArt, e.NewValue is true);
        EmptyArt.IsVisibleChanged += (_, e) => Float(EmptyArt, e.NewValue is true);
        SortPopup.CustomPopupPlacementCallback = PlaceSortMenu;
        SortPopup.Closed += (_, _) => _sortClosedAt = DateTime.UtcNow;
    }

    private MainViewModel? Vm => DataContext as MainViewModel;

    private async void OnRename(object sender, RoutedEventArgs e)
    {
        if (Vm is { } vm) await vm.RenameSelectedSpirit(SpiritNameInput.Text);
    }

    private async void OnFavorite(object sender, RoutedEventArgs e)
    {
        if (Vm is { } vm) await vm.ToggleSelectedSpiritFavorite();
    }

    // ---------- 크기에 따른 배치 ----------
    private void WatchWindow(Window? window)
    {
        if (ReferenceEquals(window, _window)) return;
        if (_window is not null) _window.SizeChanged -= OnWindowSizeChanged;
        _window = window;
        if (window is null) return;
        window.SizeChanged += OnWindowSizeChanged;
        ApplyPanelWidth(window.ActualWidth);
    }

    private void OnWindowSizeChanged(object sender, SizeChangedEventArgs e) => ApplyPanelWidth(e.NewSize.Width);

    /// <summary>
    /// 오른쪽 패널: 창이 1280 이상이면 384, 1100~1279 는 340, 그보다 좁으면 320.
    /// 패널은 창 오른쪽 끝까지 닿아야 해서, 이 화면을 담은 칸의 오른쪽 여백만큼 밖으로 내보내고 칸 너비는 그만큼 뺀다.
    /// </summary>
    private void ApplyPanelWidth(double windowWidth)
    {
        double edge = (Parent as FrameworkElement ?? VisualTreeHelper.GetParent(this) as FrameworkElement)?.Margin.Right ?? 0;
        double width = Math.Max(0, (windowWidth >= 1280 ? 384 : windowWidth >= 1100 ? 340 : 320) - edge);
        if (PanelCol.Width.Value != width) PanelCol.Width = new GridLength(width);
        if (PanelEdge.Margin.Right != -edge) PanelEdge.Margin = new Thickness(0, 0, -edge, 0);
    }

    private void OnLeftSizeChanged(object sender, SizeChangedEventArgs e)
    {
        int columns = e.NewSize.Width <= 420 ? 2 : e.NewSize.Width <= 560 ? 3 : 4;
        if (columns != Columns) Columns = columns;
    }

    // ---------- 움직임 ----------
    /// <summary>화면에 들어올 때 카드가 차례로 떠오른다 (탭을 바꿀 때는 아님).</summary>
    private void OnVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (e.NewValue is not true || !Motion.Enabled) return;
        Dispatcher.BeginInvoke(DispatcherPriority.Loaded, () =>
        {
            foreach (var grid in Descendants<ItemsControl>(DexBody).Where(i => i.IsVisible && i.Tag as string == "cards").ToList())
                Motion.PlayStagger(grid);
        });
    }

    /// <summary>둥실둥실 (시안 float: 3.2초에 위로 8 갔다가 돌아온다). 안 보일 때는 멈춘다.</summary>
    private static void Float(FrameworkElement element, bool on)
    {
        if (element.RenderTransform is not TranslateTransform t || t.IsFrozen) element.RenderTransform = t = new TranslateTransform();
        if (!on || !Motion.Enabled) { t.BeginAnimation(TranslateTransform.YProperty, null); return; }
        t.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimation(0, -8, TimeSpan.FromSeconds(1.6))
        {
            AutoReverse = true, RepeatBehavior = RepeatBehavior.Forever, EasingFunction = new SineEase { EasingMode = EasingMode.EaseInOut },
        });
    }

    // 마우스를 올리면 그림 칸이 2 떠오르고 그림이 좌우로 살짝 흔들린다
    private void OnCardEnter(object sender, MouseEventArgs e)
    {
        if (sender is not Button card) return;
        if (card.Template.FindName("lift", card) is FrameworkElement lift) Lift(lift, -2);
        if (Motion.Enabled && card.Template.FindName("art", card) is FrameworkElement art)
        {
            var tilt = new RotateTransform();
            art.RenderTransform = tilt;
            var ease = new SineEase { EasingMode = EasingMode.EaseInOut };
            tilt.BeginAnimation(RotateTransform.AngleProperty, new DoubleAnimationUsingKeyFrames
            {
                Duration = TimeSpan.FromMilliseconds(500), FillBehavior = FillBehavior.Stop,
                KeyFrames =
                {
                    new EasingDoubleKeyFrame(-5, KeyTime.FromPercent(0.25), ease),
                    new EasingDoubleKeyFrame(5, KeyTime.FromPercent(0.75), ease),
                    new EasingDoubleKeyFrame(0, KeyTime.FromPercent(1), ease),
                },
            });
        }
    }

    private void OnCardLeave(object sender, MouseEventArgs e)
    {
        if (sender is Button card && card.Template.FindName("lift", card) is FrameworkElement lift) Lift(lift, 0);
    }

    private static void Lift(FrameworkElement element, double y)
    {
        if (element.RenderTransform is not TranslateTransform t || t.IsFrozen) element.RenderTransform = t = new TranslateTransform();
        if (Motion.Enabled)
            t.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimation(y, TimeSpan.FromMilliseconds(120)) { EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut } });
        else { t.BeginAnimation(TranslateTransform.YProperty, null); t.Y = y; }
    }

    // ---------- 고르기 · 대표 정령 ----------
    /// <summary>정령을 고른다. 바뀌었으면 패널 내용이 옆에서 밀려 들어온다.</summary>
    private void Select(SpiritRow row)
    {
        if (Vm?.SelectSpirit(row) == true) Motion.Play(PanelBody, "SlideIn");
    }

    private void OnCardClick(object sender, RoutedEventArgs e)
    {
        if (sender is FrameworkElement { DataContext: SpiritRow row }) Select(row);
    }

    // 만난 정령을 두 번 누르면 바로 대표 정령으로
    private void OnCardDoubleClick(object sender, MouseButtonEventArgs e)
    {
        if (e.ChangedButton == MouseButton.Left && sender is FrameworkElement { DataContext: SpiritRow { Owned: true } row }) Vm?.MakeRepresentative(row);
    }

    // 버튼으로 정했을 때는 카드의 별 배지가 톡 튀어나온다 (Click 이 명령보다 먼저 오므로 한 박자 뒤에)
    private void OnSetRepresentativeClick(object sender, RoutedEventArgs e)
    {
        if (Vm?.SelectedSpirit is not { } row) return;
        Dispatcher.BeginInvoke(DispatcherPriority.Loaded, () =>
        {
            if (row.IsRepresentative && Cards().FirstOrDefault(c => ReferenceEquals(c.DataContext, row)) is { } card
                && card.Template.FindName("rep", card) is FrameworkElement badge)
                Motion.Play(badge, "Pop");
        });
    }

    private void OnTabClick(object sender, RoutedEventArgs e)
    {
        if (Vm is not { } vm || sender is not Button { CommandParameter: string mode }) return;
        vm.SetDexModeCommand.Execute(mode);
        Motion.Play(DexBody, mode == "cat" ? "SlideIn" : "SlideBack");
    }

    // ---------- 키보드: 화살표로 옮기고 Enter 로 대표 정령 ----------
    private void OnDexKeyDown(object sender, KeyEventArgs e)
    {
        if (Vm is not { } vm || Keyboard.FocusedElement is not Button { DataContext: SpiritRow row } focused) return;
        if (e.Key == Key.Enter)
        {
            if (row is { Owned: true, IsRepresentative: false }) { vm.MakeRepresentative(row); e.Handled = true; }
            return;
        }
        int step = e.Key switch { Key.Right => 1, Key.Left => -1, Key.Down => Columns, Key.Up => -Columns, _ => 0 };
        if (step == 0) return;
        e.Handled = true;
        var cards = Cards().ToList();
        int index = cards.IndexOf(focused);
        if (index < 0 || index + step < 0 || index + step >= cards.Count) return;
        var next = cards[index + step];
        Select((SpiritRow)next.DataContext);
        next.Focus();
    }

    /// <summary>지금 보이는 카드들, 화면에 놓인 순서대로.</summary>
    private IEnumerable<Button> Cards() => Descendants<Button>(DexBody).Where(b => b.IsVisible && b.DataContext is SpiritRow);

    private static IEnumerable<T> Descendants<T>(DependencyObject root) where T : DependencyObject
    {
        int count = VisualTreeHelper.GetChildrenCount(root);
        for (int i = 0; i < count; i++)
        {
            var child = VisualTreeHelper.GetChild(root, i);
            if (child is T match) yield return match;
            foreach (var deeper in Descendants<T>(child)) yield return deeper;
        }
    }

    // ---------- 정렬 메뉴 ----------
    private void OnSortClick(object sender, RoutedEventArgs e)
    {
        // 열린 메뉴 밖(이 단추 포함)을 누르면 메뉴가 먼저 닫힌다. 그 직후의 누름은 "닫기"로 본다.
        if ((DateTime.UtcNow - _sortClosedAt).TotalMilliseconds < 250) return;
        SortPopup.IsOpen = true;
        Dispatcher.BeginInvoke(DispatcherPriority.Input, () =>
        {
            var items = Descendants<Button>(SortMenu).ToList();
            (items.FirstOrDefault(b => b.DataContext is DexSortOption { IsSelected: true }) ?? items.FirstOrDefault())?.Focus();
        });
    }

    private void OnSortPick(object sender, RoutedEventArgs e)
    {
        if (Vm is { } vm && sender is FrameworkElement { DataContext: DexSortOption option }) vm.DexSort = option.Label;
        SortPopup.IsOpen = false;
        SortBtn.Focus();
    }

    private void OnSortKeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key != Key.Escape) return;
        SortPopup.IsOpen = false;
        SortBtn.Focus();
        e.Handled = true;
    }

    /// <summary>메뉴를 단추 오른쪽 끝에 맞춰 바로 아래(6 띄워서)에 둔다. 크기는 화면 픽셀 단위로 들어온다.</summary>
    private CustomPopupPlacement[] PlaceSortMenu(Size popupSize, Size targetSize, Point offset)
    {
        double scale = VisualTreeHelper.GetDpi(this).DpiScaleX;
        // 메뉴 둘레에 그림자 자리(양옆 16, 위 6)가 있어서 그만큼 보정한다: 위쪽 6 은 띄울 간격 6 과 상쇄된다
        return [new CustomPopupPlacement(new Point(targetSize.Width - popupSize.Width + 16 * scale, targetSize.Height), PopupPrimaryAxis.Vertical)];
    }
}
