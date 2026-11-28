using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Input;
using Microsoft.Win32;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>도감과 설정 화면의 상태.</summary>
public sealed partial class MainViewModel
{

    private bool _applyingSystem;

    private void InitExtras()
    {
        InitDex(); // 도감: MainViewModel.Dex.cs

        SystemEvents.UserPreferenceChanged += (_, e) =>
        {
            if (e.Category == UserPreferenceCategory.General && FollowSystem)
                System.Windows.Application.Current.Dispatcher.Invoke(ApplySystemTheme);
        };
        if (FollowSystem) ApplySystemTheme();
    }

    // ---------- 설정 ----------

    public bool FollowSystem
    {
        get => _data.FollowSystem;
        set { if (_data.FollowSystem == value) return; _data.FollowSystem = value; Persist(); Raise(); if (value) ApplySystemTheme(); }
    }

    public bool ContrastMore
    {
        get => _data.ContrastMore;
        set { if (_data.ContrastMore == value) return; _data.ContrastMore = value; Persist(); ThemeService.Apply(IsDark, value); Raise(); }
    }

    private void ApplySystemTheme()
    {
        _applyingSystem = true;
        IsDark = ThemeService.SystemIsDark();
        _applyingSystem = false;
    }
}
