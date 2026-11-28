namespace TodoSpirits.Windows.Models;

/// <summary>
/// 반복 규칙. 반복 일정은 한 건으로 저장하고, 날짜마다 "그 날 나타나는가"를 계산한다.
/// - 매일: 시작 날짜부터 매일
/// - 매주: 고른 요일마다 (고르지 않았으면 시작 날짜의 요일)
/// - 매월: 고른 날짜마다 (고르지 않았으면 시작 날짜의 일). 29~31일은 짧은 달에서 그 달의 마지막 날로 옮긴다
/// 완료는 회차(날짜)별로 따로 기록한다.
/// </summary>
public static class Recur
{
    public const string None = "없음", Daily = "매일", Weekly = "매주", Monthly = "매월";
    public static readonly string[] Options = [None, Daily, Weekly, Monthly];
    private const string Days = "일월화수목금토";

    public static bool IsRepeating(TodoItem item) => item.Repeat is Daily or Weekly or Monthly;

    /// <summary>이 일정이 그 날짜에 나타나는가.</summary>
    public static bool Occurs(TodoItem item, DateOnly date)
    {
        if (!IsRepeating(item)) return item.Date == date;
        if (date < item.Date) return false;
        switch (item.Repeat)
        {
            case Daily: return true;
            case Weekly:
                return item.RepeatDays.Count == 0 ? date.DayOfWeek == item.Date.DayOfWeek : item.RepeatDays.Contains((int)date.DayOfWeek);
            default:
                int last = DateTime.DaysInMonth(date.Year, date.Month);
                var wanted = item.RepeatDays.Count == 0 ? [item.Date.Day] : item.RepeatDays;
                return wanted.Any(d => Math.Min(d, last) == date.Day);
        }
    }

    public static bool IsDone(TodoItem item, DateOnly date) => IsRepeating(item) ? item.DoneDates.Contains(date) : item.Done;

    public static void SetDone(TodoItem item, DateOnly date, bool done)
    {
        if (!IsRepeating(item)) { item.Done = done; return; }
        item.DoneDates.Remove(date);
        if (done) item.DoneDates.Add(date);
    }

    /// <summary>"매일" / "매주 월·수" / "매월 15일" / ""</summary>
    public static string Summary(TodoItem item) => item.Repeat switch
    {
        Daily => "매일",
        Weekly => "매주 " + string.Join("·", (item.RepeatDays.Count == 0 ? new List<int> { (int)item.Date.DayOfWeek } : item.RepeatDays.Order().ToList()).Select(d => Days[d])),
        Monthly => "매월 " + string.Join("·", (item.RepeatDays.Count == 0 ? new List<int> { item.Date.Day } : item.RepeatDays.Order().ToList()).Select(d => d + "일")),
        _ => "",
    };
}

/// <summary>알림 시점. 시간이 있는 일정에만 쓸 수 있다.</summary>
public static class Alarm
{
    public const string None = "없음";
    public static readonly string[] Options = [None, "정각", "10분 전", "30분 전", "1시간 전"];

    /// <summary>일정 시각보다 몇 분 먼저 알릴지. 알림이 없으면 null.</summary>
    public static int? MinutesBefore(string alarm) => alarm switch
    {
        "정각" => 0, "10분 전" => 10, "30분 전" => 30, "1시간 전" => 60, _ => null,
    };
}
