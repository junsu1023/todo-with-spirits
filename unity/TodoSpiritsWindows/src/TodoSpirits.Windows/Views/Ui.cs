using System.Windows;

namespace TodoSpirits.Windows.Views;

/// <summary>
/// 공용 모양 값. 버튼 템플릿 하나를 쓰면서 스타일마다 다른 값만 붙여 준다.
/// Radius = 모서리 둥글기(시안의 r-ctl 14, 알약은 높이의 절반), IconSize = 버튼 안 아이콘 크기, Selected = 선택된 카드.
/// </summary>
public static class Ui
{
    public static readonly DependencyProperty RadiusProperty = DependencyProperty.RegisterAttached(
        "Radius", typeof(CornerRadius), typeof(Ui), new FrameworkPropertyMetadata(new CornerRadius(14)));
    public static void SetRadius(DependencyObject element, CornerRadius value) => element.SetValue(RadiusProperty, value);
    public static CornerRadius GetRadius(DependencyObject element) => (CornerRadius)element.GetValue(RadiusProperty);

    public static readonly DependencyProperty IconSizeProperty = DependencyProperty.RegisterAttached(
        "IconSize", typeof(double), typeof(Ui), new FrameworkPropertyMetadata(18.0));
    public static void SetIconSize(DependencyObject element, double value) => element.SetValue(IconSizeProperty, value);
    public static double GetIconSize(DependencyObject element) => (double)element.GetValue(IconSizeProperty);

    public static readonly DependencyProperty SelectedProperty = DependencyProperty.RegisterAttached(
        "Selected", typeof(bool), typeof(Ui), new FrameworkPropertyMetadata(false));
    public static void SetSelected(DependencyObject element, bool value) => element.SetValue(SelectedProperty, value);
    public static bool GetSelected(DependencyObject element) => (bool)element.GetValue(SelectedProperty);
}
