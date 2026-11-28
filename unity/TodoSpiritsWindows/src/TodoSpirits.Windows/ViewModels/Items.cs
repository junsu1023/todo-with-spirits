using TodoSpirits.Windows.Models;

namespace TodoSpirits.Windows.ViewModels;

public static class Fmt
{
    private const string Days = "일월화수목금토";

    public static string Dow(DateOnly d) => Days[(int)d.DayOfWeek].ToString();

    /// <summary>"오후 2:30"</summary>
    public static string Time(TimeOnly t) => $"{(t.Hour < 12 ? "오전" : "오후")} {(t.Hour % 12 == 0 ? 12 : t.Hour % 12)}:{t.Minute:00}";

    /// <summary>"10월 13일 (화)"</summary>
    public static string Short(DateOnly d) => $"{d.Month}월 {d.Day}일 ({Dow(d)})";

    /// <summary>"10월 13일 화요일"</summary>
    public static string Long(DateOnly d) => $"{d.Month}월 {d.Day}일 {Dow(d)}요일";

    /// <summary>받침이 있으면 "을", 없으면 "를" 같은 조사를 고른다.</summary>
    public static string Josa(string word, string withFinal, string withoutFinal)
    {
        if (word.Length == 0) return withFinal;
        var c = word[^1];
        if (c < 0xAC00 || c > 0xD7A3) return withFinal;
        return (c - 0xAC00) % 28 != 0 ? withFinal : withoutFinal;
    }

    /// <summary>"5개 중 1개 완료" / "일정 5개" / "모두 완료" / ""</summary>
    public static string Stat(int total, int done) =>
        total == 0 ? "" : done == total ? "모두 완료" : done > 0 ? $"{total}개 중 {done}개 완료" : $"일정 {total}개";
}

/// <summary>일정의 한 회차: 어느 일정이 어느 날짜에 나타나는가.</summary>
public readonly record struct Occ(TodoItem Item, DateOnly Date)
{
    public bool Done => Recur.IsDone(Item, Date);
}

/// <summary>화면에 그리는 일정 한 줄. Refresh 때마다 새로 만든다.</summary>
public sealed class TaskRow(Occ occ, DateOnly today, TimeOnly now, bool justDone = false)
{
    public TodoItem Item { get; } = occ.Item;
    /// <summary>이 줄이 나타내는 날짜 (반복 일정이면 그 회차의 날짜).</summary>
    public DateOnly Date { get; } = occ.Date;
    public string Title => Item.Title;
    public bool Done { get; } = occ.Done;
    public bool Important => Item.Important;
    public bool IsOverdue => !Done && Date == today && Item.Time is { } t && t < now;
    public string Sub => (Item.Time is { } t ? Fmt.Time(t) : "하루 종일") + (IsOverdue ? " · 시간이 지났어요" : "");
    public string CarrySub => Fmt.Short(Date) + (Item.Time is { } t ? " " + Fmt.Time(t) : "");
    public string RepeatText => Recur.Summary(Item);
    public bool HasRepeat => Recur.IsRepeating(Item);
    public string AlarmText => Item.Alarm;
    public bool HasAlarm => Item.Time is not null && Alarm.MinutesBefore(Item.Alarm) is not null;
    /// <summary>방금 완료한 줄: 체크가 톡 튀어나온다.</summary>
    public bool JustDone { get; } = justDone;
}

/// <summary>달력의 하루 칸 (주간 띠·월간 칸·연간 숫자 공용).</summary>
public sealed class DayCell
{
    public DateOnly Date { get; init; }
    public bool IsBlank { get; init; }
    public bool IsToday { get; init; }
    public bool IsSelected { get; init; }
    public bool IsOther { get; init; }
    public bool IsPast { get; init; }
    public int Total { get; init; }
    public int DoneCount { get; init; }
    public bool HasImportant { get; init; }
    public string FirstTitle { get; init; } = "";
    public bool FirstImportant { get; init; }

    public string Num => IsBlank ? "" : Date.Day.ToString();
    public string Dow => Fmt.Dow(Date);
    public string DowLabel => IsToday ? "오늘 · " + Dow : Dow;
    public bool AllDone => Total > 0 && DoneCount == Total;
    public bool HasTasks => Total > 0;
    public bool ShowFirst => !IsOther && Total > 0 && !AllDone;

    /// <summary>주간 띠: "5개" / "없음" / "모두 완료"</summary>
    public string CountText => Total == 0 ? "없음" : AllDone ? "모두 완료" : $"{Total}개";

    /// <summary>월간 칸 둘째 줄: "외 4개" / "모두 완료" / ""</summary>
    public string RestText => IsOther || Total == 0 ? "" : AllDone ? "모두 완료" : Total > 1 ? $"외 {Total - 1}개" : "";

    public int Heat => IsBlank ? 0 : Total >= 2 ? 2 : Total;
    public string Tip => IsBlank ? "" : $"{Fmt.Short(Date)}{(IsToday ? " 오늘" : "")} · 일정 {Total}개{(HasImportant ? " · 중요 있음" : "")}";
}

public sealed class MonthCard
{
    public int Year { get; init; }
    public int Month { get; init; }
    public bool IsCurrent { get; init; }
    public int Total { get; init; }
    public IReadOnlyList<DayCell> Cells { get; init; } = [];
    public string Name => $"{Month}월";
    public string CountText => Total == 0 ? "일정 없음" : $"일정 {Total}개";
}
