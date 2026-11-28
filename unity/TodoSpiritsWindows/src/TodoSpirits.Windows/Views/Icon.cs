using System.Windows;
using System.Windows.Documents;
using System.Windows.Media;

namespace TodoSpirits.Windows.Views;

/// <summary>
/// 시안의 선 아이콘(24×24 기준, 굵기 1.6, 둥근 끝)을 그린다.
/// 색은 주변 글자색(Foreground)을 그대로 따라가서, 버튼 색이 바뀌면 아이콘도 함께 바뀐다.
/// </summary>
public sealed class Icon : FrameworkElement
{
    public static readonly DependencyProperty DataProperty = DependencyProperty.Register(
        nameof(Data), typeof(Geometry), typeof(Icon), new FrameworkPropertyMetadata(null, FrameworkPropertyMetadataOptions.AffectsRender));

    public static readonly DependencyProperty ForegroundProperty = TextElement.ForegroundProperty.AddOwner(
        typeof(Icon), new FrameworkPropertyMetadata(Brushes.Black, FrameworkPropertyMetadataOptions.Inherits | FrameworkPropertyMetadataOptions.AffectsRender));

    public static readonly DependencyProperty ThicknessProperty = DependencyProperty.Register(
        nameof(Thickness), typeof(double), typeof(Icon), new FrameworkPropertyMetadata(1.6, FrameworkPropertyMetadataOptions.AffectsRender));

    public static readonly DependencyProperty FilledProperty = DependencyProperty.Register(
        nameof(Filled), typeof(bool), typeof(Icon), new FrameworkPropertyMetadata(false, FrameworkPropertyMetadataOptions.AffectsRender));

    public Geometry? Data { get => (Geometry?)GetValue(DataProperty); set => SetValue(DataProperty, value); }
    public Brush Foreground { get => (Brush)GetValue(ForegroundProperty); set => SetValue(ForegroundProperty, value); }
    public double Thickness { get => (double)GetValue(ThicknessProperty); set => SetValue(ThicknessProperty, value); }
    public bool Filled { get => (bool)GetValue(FilledProperty); set => SetValue(FilledProperty, value); }

    protected override Size MeasureOverride(Size availableSize) =>
        new(double.IsNaN(Width) ? 20 : Width, double.IsNaN(Height) ? 20 : Height);

    protected override void OnRender(DrawingContext dc)
    {
        if (Data is null) return;
        double scale = Math.Min(ActualWidth, ActualHeight) / 24.0;
        if (scale <= 0) return;
        var pen = new Pen(Foreground, Thickness)
        {
            StartLineCap = PenLineCap.Round, EndLineCap = PenLineCap.Round, LineJoin = PenLineJoin.Round,
        };
        dc.PushTransform(new TranslateTransform((ActualWidth - 24 * scale) / 2, (ActualHeight - 24 * scale) / 2));
        dc.PushTransform(new ScaleTransform(scale, scale));
        dc.DrawGeometry(Filled ? Foreground : null, pen, Data);
        dc.Pop(); dc.Pop();
    }
}
