using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace TodoSpirits.Windows.Views;

/// <summary>
/// 화면에 나타날 때의 움직임. XAML 에서 v:Motion.Enter="Right" 처럼 붙인다.
/// Right = 오른쪽에서 밀려 들어옴(패널), Up = 아래에서 살짝 올라옴(화면·토스트), Fade = 서서히.
/// Windows 의 "애니메이션 효과"를 끈 사용자에게는 움직이지 않는다.
/// </summary>
public static class Motion
{
    public static readonly DependencyProperty EnterProperty = DependencyProperty.RegisterAttached(
        "Enter", typeof(string), typeof(Motion), new PropertyMetadata(null, OnEnterChanged));

    public static void SetEnter(DependencyObject element, string value) => element.SetValue(EnterProperty, value);
    public static string GetEnter(DependencyObject element) => (string)element.GetValue(EnterProperty);

    private static void OnEnterChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is not FrameworkElement element) return;
        element.IsVisibleChanged -= OnVisibleChanged;
        if (e.NewValue is string) element.IsVisibleChanged += OnVisibleChanged;
    }

    private static void OnVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (e.NewValue is not true || sender is not FrameworkElement element) return;
        if (!SystemParameters.ClientAreaAnimation) return;
        Play(element, GetEnter(element));
    }

    public static void Play(FrameworkElement element, string kind)
    {
        var duration = TimeSpan.FromMilliseconds(kind == "Right" ? 260 : 220);
        var ease = new CubicEase { EasingMode = EasingMode.EaseOut };
        element.BeginAnimation(UIElement.OpacityProperty, new DoubleAnimation(0, 1, duration) { EasingFunction = ease });
        if (kind == "Fade") return;
        var shift = new TranslateTransform();
        element.RenderTransform = shift;
        var move = new DoubleAnimation(kind == "Right" ? 28 : 12, 0, duration) { EasingFunction = ease };
        shift.BeginAnimation(kind == "Right" ? TranslateTransform.XProperty : TranslateTransform.YProperty, move);
    }
}
