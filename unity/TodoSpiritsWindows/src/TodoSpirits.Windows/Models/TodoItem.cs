namespace TodoSpirits.Windows.Models;

/// <summary>저장되는 일정 한 건. 시간은 시각만 있고 길이는 없다.</summary>
public sealed class TodoItem
{
    public Guid Id { get; set; } = Guid.NewGuid();
    public string Title { get; set; } = "";
    public DateOnly Date { get; set; }
    public TimeOnly? Time { get; set; }
    public bool Important { get; set; }
    public bool Done { get; set; }
}

/// <summary>앱 설정과 일정 전체. 한 파일에 저장한다.</summary>
public sealed class AppData
{
    public string Theme { get; set; } = "light";
    public string CalendarMode { get; set; } = "month";
    /// <summary>Windows 의 라이트/다크 설정을 따라간다.</summary>
    public bool FollowSystem { get; set; }
    /// <summary>라이트에서 글자색을 WCAG AA(4.5:1)로 보정한다. 끄면 Figma 원래 색.</summary>
    public bool ContrastMore { get; set; } = true;
    public string Nickname { get; set; } = "";
    public string RepresentativeId { get; set; } = "wind";
    public bool NotifyRemind { get; set; } = true;
    public bool NotifyPostponed { get; set; } = true;
    public bool NotifyRoutine { get; set; } = true;
    public bool NotifyStreak { get; set; } = true;
    public bool NotifyNight { get; set; }
    public List<TodoItem> Tasks { get; set; } = [];
}
