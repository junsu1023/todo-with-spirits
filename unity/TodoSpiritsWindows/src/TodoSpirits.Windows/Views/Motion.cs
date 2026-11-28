using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace TodoSpirits.Windows.Views;

/// <summary>
/// 시안(app.css)의 움직임을 옮긴 것. Windows 의 "애니메이션 효과"를 끈 사용자에게는 움직이지 않는다.
/// - v:Motion.Enter="…" : 보이게 될 때 한 번
///     Right(패널이 오른쪽에서) · Left(사이드바가 왼쪽에서) · Up(rise) · Fade · SlideIn / SlideBack(좌우 14px, 화면 안 전환)
///     · Pop(popin) · Toast(toastin) · Menu(menuin)
/// - v:Motion.Stagger="True" : 화면에 들어올 때 자식들이 35ms 간격으로 차례로 떠오른다 (목록이 다시 만들어질 때는 움직이지 않는다)
/// - Motion.Play(element, kind) : 코드에서 직접
/// </summary>
public static class Motion
{
    private static readonly IEasingFunction Soft = new CubicEase { EasingMode = EasingMode.EaseOut };
    private static readonly IEasingFunction Springy = new BackEase { EasingMode = EasingMode.EaseOut, Amplitude = 0.5 };

    public static bool Enabled => SystemParameters.ClientAreaAnimation;

    // ---------- Enter ----------
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
        if (e.NewValue is true && sender is FrameworkElement element) Play(element, GetEnter(element));
    }

    // ---------- Stagger ----------
    public static readonly DependencyProperty StaggerProperty = DependencyProperty.RegisterAttached(
        "Stagger", typeof(bool), typeof(Motion), new PropertyMetadata(false, OnStaggerChanged));
    public static void SetStagger(DependencyObject element, bool value) => element.SetValue(StaggerProperty, value);
    public static bool GetStagger(DependencyObject element) => (bool)element.GetValue(StaggerProperty);

    private static void OnStaggerChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is not FrameworkElement element) return;
        element.IsVisibleChanged -= OnStaggerVisible;
        if (e.NewValue is true) element.IsVisibleChanged += OnStaggerVisible;
    }

    private static void OnStaggerVisible(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (e.NewValue is not true || sender is not FrameworkElement element || !Enabled) return;
        // 자식 컨테이너가 만들어진 뒤에 건다
        element.Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Loaded, () => PlayStagger(element));
    }

    /// <summary>자식들을 차례로 떠오르게 한다. ItemsControl 이면 항목 컨테이너, 아니면 Panel 의 자식.</summary>
    public static void PlayStagger(FrameworkElement host, int startDelayMs = 150)
    {
        if (!Enabled) return;
        int i = 0;
        foreach (var child in ChildrenOf(host))
        {
            if (child.Visibility != Visibility.Visible) continue;
            Rise(child, 450, 10, TimeSpan.FromMilliseconds(startDelayMs + 35 * Math.Min(i, 14)));
            i++;
        }
    }

    private static IEnumerable<FrameworkElement> ChildrenOf(FrameworkElement host)
    {
        if (host is ItemsControl items)
        {
            for (int i = 0; i < items.Items.Count; i++)
                if (items.ItemContainerGenerator.ContainerFromIndex(i) is FrameworkElement c) yield return c;
        }
        else if (host is Panel panel)
        {
            foreach (var c in panel.Children) if (c is FrameworkElement f) yield return f;
        }
    }

    // ---------- Float: 보이는 동안 천천히 위아래로 떠 있다 (시안 .float 3.2s) ----------
    public static readonly DependencyProperty FloatProperty = DependencyProperty.RegisterAttached(
        "Float", typeof(bool), typeof(Motion), new PropertyMetadata(false, OnFloatChanged));
    public static void SetFloat(DependencyObject element, bool value) => element.SetValue(FloatProperty, value);
    public static bool GetFloat(DependencyObject element) => (bool)element.GetValue(FloatProperty);

    private static void OnFloatChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is not FrameworkElement element) return;
        element.IsVisibleChanged -= OnFloatVisible;
        if (e.NewValue is true) { element.IsVisibleChanged += OnFloatVisible; if (element.IsVisible) StartFloat(element); }
    }

    private static void OnFloatVisible(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (sender is not FrameworkElement element) return;
        if (e.NewValue is true) StartFloat(element);
        else (element.RenderTransform as TranslateTransform)?.BeginAnimation(TranslateTransform.YProperty, null); // 안 보일 때는 멈춘다
    }

    private static void StartFloat(FrameworkElement element)
    {
        if (!Enabled) return;
        var t = new TranslateTransform();
        element.RenderTransform = t;
        t.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimation(0, -8, TimeSpan.FromMilliseconds(1600))
        {
            AutoReverse = true, RepeatBehavior = RepeatBehavior.Forever, EasingFunction = new SineEase { EasingMode = EasingMode.EaseInOut },
        });
    }

    // ---------- 재생 ----------
    public static void Play(FrameworkElement element, string kind)
    {
        if (!Enabled) return;
        switch (kind)
        {
            case "Fade": Fade(element, 220); break;
            case "Right": Fade(element, 320); Shift(element, 32, 0, 320); break;
            case "Left": Fade(element, 300); Shift(element, -32, 0, 300); break;
            case "SlideIn": Fade(element, 280); Shift(element, 14, 0, 280); break;
            case "SlideBack": Fade(element, 280); Shift(element, -14, 0, 280); break;
            case "Pop": Fade(element, 200); Scale(element, 0.92, 1, 380, Springy); break;
            case "Menu": Fade(element, 160); Scale(element, 0.94, 1, 160, Soft); break;
            case "Toast": Fade(element, 260); Shift(element, 0, 16, 380, Springy); break;
            case "Bump": Bump(element); break;
            default: Rise(element, 420, 10, TimeSpan.Zero); break; // Up
        }
    }

    private static void Fade(UIElement element, int ms) =>
        element.BeginAnimation(UIElement.OpacityProperty, new DoubleAnimation(0, 1, TimeSpan.FromMilliseconds(ms)) { EasingFunction = Soft });

    private static void Shift(UIElement element, double fromX, double fromY, int ms, IEasingFunction? ease = null)
    {
        var t = new TranslateTransform();
        element.RenderTransform = t;
        var d = TimeSpan.FromMilliseconds(ms);
        if (fromX != 0) t.BeginAnimation(TranslateTransform.XProperty, new DoubleAnimation(fromX, 0, d) { EasingFunction = ease ?? Soft });
        if (fromY != 0) t.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimation(fromY, 0, d) { EasingFunction = ease ?? Soft });
    }

    private static void Scale(FrameworkElement element, double from, double to, int ms, IEasingFunction ease)
    {
        var s = new ScaleTransform(1, 1);
        element.RenderTransformOrigin = new Point(0.5, 0.5);
        element.RenderTransform = s;
        var a = new DoubleAnimation(from, to, TimeSpan.FromMilliseconds(ms)) { EasingFunction = ease };
        s.BeginAnimation(ScaleTransform.ScaleXProperty, a);
        s.BeginAnimation(ScaleTransform.ScaleYProperty, a);
    }

    private static void Rise(FrameworkElement element, int ms, double from, TimeSpan delay)
    {
        var d = TimeSpan.FromMilliseconds(ms);
        // 기다리는 동안에는 안 보이게 시작 값을 붙들어 둔다 (FillBehavior 기본 = HoldEnd, BeginTime 전에는 From 을 유지)
        element.BeginAnimation(UIElement.OpacityProperty, new DoubleAnimationUsingKeyFrames
        {
            Duration = delay + d,
            KeyFrames = { new DiscreteDoubleKeyFrame(0, KeyTime.FromTimeSpan(TimeSpan.Zero)), new DiscreteDoubleKeyFrame(0, KeyTime.FromTimeSpan(delay)), new EasingDoubleKeyFrame(1, KeyTime.FromTimeSpan(delay + d), Soft) },
        });
        var t = new TranslateTransform();
        element.RenderTransform = t;
        t.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimationUsingKeyFrames
        {
            Duration = delay + d,
            KeyFrames = { new DiscreteDoubleKeyFrame(from, KeyTime.FromTimeSpan(TimeSpan.Zero)), new DiscreteDoubleKeyFrame(from, KeyTime.FromTimeSpan(delay)), new EasingDoubleKeyFrame(0, KeyTime.FromTimeSpan(delay + d), Soft) },
        });
    }

    /// <summary>값이 바뀔 때 살짝 튀어 오른다 (시안 bump .35s).</summary>
    private static void Bump(UIElement element)
    {
        var t = new TranslateTransform();
        element.RenderTransform = t;
        t.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimationUsingKeyFrames
        {
            Duration = TimeSpan.FromMilliseconds(350),
            KeyFrames = { new EasingDoubleKeyFrame(-5, KeyTime.FromPercent(0.4), Soft), new EasingDoubleKeyFrame(0, KeyTime.FromPercent(1), Soft) },
        });
    }
}
