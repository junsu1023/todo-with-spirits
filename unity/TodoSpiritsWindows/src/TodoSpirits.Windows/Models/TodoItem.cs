namespace TodoSpirits.Windows.Models;

/// <summary>저장되는 일정 한 건. 시간은 시각만 있고 길이는 없다.</summary>
public sealed class TodoItem
{
    internal long? ServerId { get; init; }
    internal DateOnly? ServerStartDate { get; init; }
    internal DateOnly? RepeatEndDate { get; init; }
    internal bool ExcludeHoliday { get; init; }
    public Guid Id { get; set; } = Guid.NewGuid();
    public string Title { get; set; } = "";
    public DateOnly Date { get; set; }
    public TimeOnly? Time { get; set; }
    public bool Important { get; set; }
    public bool Done { get; set; }
    /// <summary>반복: 없음 / 매일 / 매주 / 매월 (Recur 참고).</summary>
    public string Repeat { get; set; } = "없음";
    /// <summary>매주면 요일(0=일 … 6=토), 매월이면 날짜(1~31). 비어 있으면 시작 날짜를 따른다.</summary>
    public List<int> RepeatDays { get; set; } = [];
    /// <summary>반복 일정에서 완료한 회차의 날짜들.</summary>
    public List<DateOnly> DoneDates { get; set; } = [];
    /// <summary>알림: 없음 / 정각 / 10분 전 / 30분 전 / 1시간 전.</summary>
    public string Alarm { get; set; } = "없음";
}

/// <summary>알림 패널에 쌓이는 알림 한 건.</summary>
public sealed class NotiItem
{
    internal long? ServerId { get; init; }
    internal string Category { get; init; } = "sys";
    public Guid Id { get; set; } = Guid.NewGuid();
    public DateTime When { get; set; }
    public string Title { get; set; } = "";
    public string Body { get; set; } = "";
    public Guid TaskId { get; set; }
    public DateOnly TaskDate { get; set; }
    public bool Read { get; set; }
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
    /// <summary>바탕화면에 정령(Unity 오버레이)을 띄운다.</summary>
    public bool SpiritEnabled { get; set; } = true;
    public bool NavHidden { get; set; }
    /// <summary>창을 닫아도 트레이에 남아 알림과 정령을 유지한다.</summary>
    public bool CloseToTray { get; set; }
    public List<NotiItem> Notifications { get; set; } = [];
    /// <summary>이미 울린 알림("일정 id|날짜"). 같은 알림이 두 번 울리지 않게 한다.</summary>
    public List<string> FiredAlarms { get; set; } = [];
    public List<TodoItem> Tasks { get; set; } = [];
    /// <summary>일정 카드에 디데이를 보여 준다 (설정 › 디스플레이).</summary>
    public bool ShowDday { get; set; } = true;
    /// <summary>고른 언어: 시스템 / 한국어 / English / 日本語 / 中文. 아직은 값만 기억한다.</summary>
    public string Language { get; set; } = "시스템";
    /// <summary>마지막으로 백업한 시각 (설정 › 데이터). 한 번도 안 했으면 비어 있다.</summary>
    public DateTime? LastBackup { get; set; }
}
