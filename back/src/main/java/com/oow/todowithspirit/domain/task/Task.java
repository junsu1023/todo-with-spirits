package com.oow.todowithspirit.domain.task;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import com.oow.todowithspirit.domain.spirit.GrowthType;
import com.oow.todowithspirit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategoryType category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_all_day", nullable = false)
    private boolean isAllDay;

    @Column(name = "is_important", nullable = false)
    private boolean isImportant;

    @Column(name="exclude_holiday", nullable = false)
    private boolean excludeHoliday;

    // 반복
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType = RepeatType.NONE;

    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_repeat_days_of_week", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.BatchSize(size = 100)
    private Set<DayOfWeek> repeatDaysOfWeek = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_repeat_days_of_month", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "day_of_month")
    @org.hibernate.annotations.BatchSize(size = 100)
    private Set<Integer> repeatDaysOfMonth = new HashSet<>();

    // 알림
    @Column(name = "notification_minutes")
    private Integer notificationMinutes;

    // 알림 발송 기준 시각: 알림 서비스가 이 컬럼을 기준으로 발송 대상 조회
    // SCHEDULE(isAllDay): startDate 23:59 - notificationMinutes
    // ROUTINE: 각 occurrence 날짜 23:59 - notificationMinutes (서비스에서 occurrence별 계산)
    @Column(name = "notification_at")
    private LocalDateTime notificationAt;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    // 성장 (서버 계산)
    @Column(nullable = false)
    private int growthValue = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "growth_type", nullable = true)
    private GrowthType growthType;

    // 완료
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static Task createSchedule(User user, String title, String memo, CategoryType category,
                                      LocalDateTime endDateTime, boolean isAllDay,
                                      boolean isImportant, Integer notificationMinutes, boolean isPublic) {
        LocalDate date = endDateTime.toLocalDate();
        Task task = new Task();
        task.user = user;
        task.taskType = TaskType.SCHEDULE;
        task.title = title;
        task.memo = memo;
        CategoryType resolvedCategory = category != null ? category : CategoryType.NONE;
        task.category = resolvedCategory;
        task.growthType = resolveGrowthTypeByCategory(resolvedCategory);
        task.startDate = date;
        task.startTime = null;
        task.endDate = date;
        task.endTime = isAllDay ? LocalTime.of(23, 59, 59) : endDateTime.toLocalTime();
        task.isAllDay = isAllDay;
        task.isImportant = isImportant;
        task.repeatType = RepeatType.NONE;
        task.notificationMinutes = notificationMinutes;
        task.notificationAt = computeScheduleNotificationAt(endDateTime, isAllDay, notificationMinutes);
        task.isPublic = isPublic;
        return task;
    }

    public static Task createRoutine(User user, String title, String memo, CategoryType category,
                                     RepeatType repeatType, LocalDate repeatEndDate,
                                     Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                                     Integer notificationMinutes, boolean isPublic, boolean excludeHoliday) {
        Task task = new Task();
        task.user = user;
        task.taskType = TaskType.ROUTINE;
        task.title = title;
        task.memo = memo;
        task.category = category != null ? category : CategoryType.NONE;
        task.growthType = null;
        task.startDate = LocalDate.now();
        task.repeatType = repeatType;
        task.repeatEndDate = repeatEndDate;
        task.repeatDaysOfWeek = repeatDaysOfWeek != null ? repeatDaysOfWeek : new HashSet<>();
        task.repeatDaysOfMonth = repeatDaysOfMonth != null ? repeatDaysOfMonth : new HashSet<>();
        task.notificationMinutes = notificationMinutes;
        task.isPublic = isPublic;
        task.excludeHoliday = excludeHoliday;
        return task;
    }

    public void updateRoutine(String title, String memo, LocalDate startDate,
                              RepeatType repeatType, LocalDate repeatEndDate,
                              Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                              Integer notificationMinutes, boolean isPublic) {
        this.title = title;
        this.memo = memo;
        if (startDate != null) this.startDate = startDate;
        this.repeatType = repeatType;
        this.repeatEndDate = repeatEndDate;
        this.repeatDaysOfWeek = repeatDaysOfWeek != null ? repeatDaysOfWeek : new HashSet<>();
        this.repeatDaysOfMonth = repeatDaysOfMonth != null ? repeatDaysOfMonth : new HashSet<>();
        this.notificationMinutes = notificationMinutes;
        this.isPublic = isPublic;
    }

    public void updateSchedule(String title, String memo, CategoryType category,
                               LocalDateTime endDateTime, boolean isAllDay,
                               boolean isImportant, Integer notificationMinutes, boolean isPublic) {
        LocalDate date = endDateTime.toLocalDate();
        this.title = title;
        this.memo = memo;
        this.category = category != null ? category : CategoryType.NONE;
        this.startDate = date;
        this.endDate = date;
        this.startTime = null;
        this.endTime = isAllDay ? LocalTime.of(23, 59, 59) : endDateTime.toLocalTime();
        this.isAllDay = isAllDay;
        this.isImportant = isImportant;
        this.notificationMinutes = notificationMinutes;
        this.notificationAt = computeScheduleNotificationAt(endDateTime, isAllDay, notificationMinutes);
        this.isPublic = isPublic;
    }

    public void completeTask() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    public void undoCompleteTask() {
        this.isCompleted = false;
        this.completedAt = null;
    }

    private static LocalDateTime computeScheduleNotificationAt(LocalDateTime endDateTime, boolean isAllDay, Integer notificationMinutes) {
        if (endDateTime == null || notificationMinutes == null) return null;
        LocalDateTime base = isAllDay ? endDateTime.toLocalDate().atTime(23, 59, 59) : endDateTime;
        return base.minusMinutes(notificationMinutes);
    }

    private static GrowthType resolveGrowthTypeByCategory(CategoryType category) {
        if (category == null) return null;
        return switch (category) {
            case WORK_STUDY -> GrowthType.FOCUS;         // 집중
            case HEALTH, LIFE -> GrowthType.VITALITY;    // 활력
            case RELATIONSHIP -> GrowthType.PERSISTENCE; // 꾸준함
            case HOBBY, GROWTH -> GrowthType.CREATIVITY; // 창의
            default -> null; // 해당 안 되는 카테고리는 경험치 지급 제외 등 자유롭게 설계
        };
    }
}