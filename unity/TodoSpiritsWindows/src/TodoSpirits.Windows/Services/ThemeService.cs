using System.Windows;
using Microsoft.Win32;

namespace TodoSpirits.Windows.Services;

public static class ThemeService
{
    /// <summary>
    /// App.xaml 의 첫 번째 병합 사전을 바꿔 끼운다.
    /// 다크 / 라이트(글자 대비 높임) / 라이트 원색(Figma 색 그대로) 세 가지.
    /// </summary>
    public static void Apply(bool dark, bool contrastMore)
    {
        var name = dark ? "Dark" : contrastMore ? "Light" : "LightOriginal";
        Application.Current.Resources.MergedDictionaries[0] = new ResourceDictionary
        {
            Source = new Uri($"Themes/{name}.xaml", UriKind.Relative),
        };
    }

    /// <summary>Windows 설정의 "앱 모드"가 다크인지. 읽지 못하면 라이트로 본다.</summary>
    public static bool SystemIsDark()
    {
        try
        {
            using var key = Registry.CurrentUser.OpenSubKey(@"Software\Microsoft\Windows\CurrentVersion\Themes\Personalize");
            return key?.GetValue("AppsUseLightTheme") is int v && v == 0;
        }
        catch (Exception ex) when (ex is System.Security.SecurityException or UnauthorizedAccessException or System.IO.IOException)
        {
            return false;
        }
    }
}
