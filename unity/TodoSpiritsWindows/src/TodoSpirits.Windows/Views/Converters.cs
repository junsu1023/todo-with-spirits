using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace TodoSpirits.Windows.Views;

/// <summary>값의 문자열이 매개변수와 같으면 보이게. 매개변수 앞에 !를 붙이면 반대.</summary>
public sealed class EqVis : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        var p = parameter?.ToString() ?? "";
        var invert = p.StartsWith('!');
        if (invert) p = p[1..];
        var eq = string.Equals(value?.ToString(), p, StringComparison.OrdinalIgnoreCase);
        return eq != invert ? Visibility.Visible : Visibility.Collapsed;
    }

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture) => Binding.DoNothing;
}

/// <summary>true 면 보이게. 매개변수가 "inv" 면 반대, "hide" 면 숨길 때 자리를 남긴다.</summary>
public sealed class BoolVis : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        var p = parameter?.ToString() ?? "";
        var on = value is true;
        if (p.Contains("inv")) on = !on;
        return on ? Visibility.Visible : p.Contains("hide") ? Visibility.Hidden : Visibility.Collapsed;
    }

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture) => Binding.DoNothing;
}
