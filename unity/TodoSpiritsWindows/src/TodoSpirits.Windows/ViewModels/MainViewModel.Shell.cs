using System.Windows.Input;

namespace TodoSpirits.Windows.ViewModels;

/// <summary>
/// 창 너비에 따른 배치 (시안 app.js 의 w-mid / w-narrow).
/// 1280 이상: 기본 · 1100~1279: 오른쪽 패널 340 · 1100 미만: 사이드바 자동 숨김, 상세 패널은 내용 위에 겹쳐 열림(360), 월간의 날짜 패널은 320.
/// </summary>
public sealed partial class MainViewModel
{
    private string _widthClass = "wide";
    public string WidthClass => _widthClass;
    public bool IsNarrow => _widthClass == "narrow";

    /// <summary>창 너비가 바뀔 때 창이 알려 준다.</summary>
    public void SetWindowWidth(double width)
    {
        var next = width < 1100 ? "narrow" : width < 1280 ? "mid" : "wide";
        if (next == _widthClass) return;
        bool wasNarrow = IsNarrow;
        _widthClass = next;
        if (IsNarrow != wasNarrow) { _narrowPin = false; _navPeek = false; NavPeekSticky = false; }
        Raise(string.Empty);
    }

    // ---------- 사이드바: 고정 / 숨김(가장자리에 올리거나 메뉴 버튼으로 잠깐 보기) ----------
    public ICommand NavMenuCommand { get; private set; } = null!;
    private bool _narrowPin;
    /// <summary>사이드바가 자리를 차지하고 고정돼 있다. 좁은 창에서는 기본이 숨김이고, 그때 고정한 것은 저장하지 않는다.</summary>
    public bool NavPinned => IsNarrow ? _narrowPin : !_data.NavHidden;

    private bool _navPeek;
    /// <summary>숨긴 사이드바를 내용 위에 잠깐 띄운 상태.</summary>
    public bool NavPeek { get => _navPeek; set { if (NavPinned) value = false; if (Set(ref _navPeek, value)) { if (!value) NavPeekSticky = false; Raise(nameof(NavShown)); } } }
    /// <summary>메뉴 버튼으로 띄운 것: 마우스가 벗어나도 닫히지 않고, 바깥을 누르거나 Esc 로 닫는다.</summary>
    public bool NavPeekSticky { get; set; }
    public bool NavShown => NavPinned || NavPeek;

    private void TogglePin()
    {
        if (IsNarrow) _narrowPin = !_narrowPin;
        else { _data.NavHidden = !_data.NavHidden; Persist(); }
        _navPeek = false; NavPeekSticky = false;
        Raise(string.Empty);
    }

    // ---------- 오른쪽 패널 ----------
    /// <summary>월간 캘린더는 오른쪽에 날짜 패널이 늘 있다. 상세 · 추가는 그 자리에서 목록을 대신한다.</summary>
    public bool DayPanelVisible => Page == "calendar" && CalMode == "month";
    public bool DayListVisible => DayPanelVisible && !PanelOpen;
    /// <summary>좁은 창에서는 상세 패널이 내용을 밀지 않고 위에 겹쳐 열린다 (월간의 날짜 패널 자리는 예외).</summary>
    public bool PanelOverlay => IsNarrow && !DayPanelVisible;
    public double PanelWidth => DayPanelVisible ? (IsNarrow ? 320 : _widthClass == "mid" ? 340 : 384) : IsNarrow ? 360 : _widthClass == "mid" ? 340 : 384;
    /// <summary>오른쪽 패널이 내용 옆에서 차지하는 너비. 겹쳐 열릴 때는 0.</summary>
    public double PanelSpace => DayPanelVisible ? PanelWidth : PanelOpen && !PanelOverlay ? PanelWidth : 0;
    /// <summary>패널(상세 또는 날짜 목록)이 화면에 나와 있다.</summary>
    public bool RightShown => PanelOpen || DayPanelVisible;
}
