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

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_all_day", nullable = false)
    private boolean isAllDay;

    @Column(name = "is_important", nullable = false)
    private boolean isImportant;

    @Column(name = "exclude_holiday", nullable = false)
    private boolean excludeHoliday;

    // 반복 (NONE이면 단발성 할 일)
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
    // 단발성: startDate endTime(하루 종일이면 23:59:59) - notificationMinutes
    // 반복: null (occurrence별로 notificationAtOn(date)로 계산)
    @Column(name = "notification_at")
    private LocalDateTime notificationAt;

    // 성장 (서버 계산)
    @Column(nullable = false)
    private int growthValue = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "growth_type", nullable = true)
    private GrowthType growthType;

    private static final GrowthType DEFAULT_GROWTH_TYPE = GrowthType.CONSISTENCY;

    // 완료
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static final LocalTime ALL_DAY_END_TIME = LocalTime.of(23, 59, 59);

    /**
     * 할 일 생성. repeatType이 NONE이면 단발성, 그 외(DAILY/WEEKLY/MONTHLY)면 반복.
     * date: 단발성은 해당 날짜, 반복은 반복 시작일
     */
    public static Task create(User user, String title, LocalDate date, LocalTime time, boolean isAllDay,
                              boolean isImportant, Integer notificationMinutes,
                              RepeatType repeatType, LocalDate repeatEndDate,
                              Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                              boolean excludeHoliday) {
        Task task = new Task();
        task.user = user;
        task.growthType = DEFAULT_GROWTH_TYPE;
        task.apply(title, date, time, isAllDay, isImportant, notificationMinutes,
                repeatType, repeatEndDate, repeatDaysOfWeek, repeatDaysOfMonth, excludeHoliday);
        return task;
    }

    public void update(String title, LocalDate date, LocalTime time, boolean isAllDay,
                       boolean isImportant, Integer notificationMinutes,
                       RepeatType repeatType, LocalDate repeatEndDate,
                       Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                       boolean excludeHoliday) {
        apply(title, date, time, isAllDay, isImportant, notificationMinutes,
                repeatType, repeatEndDate, repeatDaysOfWeek, repeatDaysOfMonth, excludeHoliday);
    }

    private void apply(String title, LocalDate date, LocalTime time, boolean isAllDay,
                       boolean isImportant, Integer notificationMinutes,
                       RepeatType repeatType, LocalDate repeatEndDate,
                       Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                       boolean excludeHoliday) {
        boolean repeating = repeatType != null && repeatType != RepeatType.NONE;
        this.title = title;
        this.startDate = date;
        this.endDate = repeating ? null : date;
        this.endTime = (isAllDay || time == null) ? ALL_DAY_END_TIME : time;
        this.isAllDay = isAllDay || time == null;
        this.isImportant = isImportant;
        this.notificationMinutes = notificationMinutes;
        this.repeatType = repeating ? repeatType : RepeatType.NONE;
        this.repeatEndDate = repeating ? repeatEndDate : null;
        replace(this.repeatDaysOfWeek, repeating && repeatType == RepeatType.WEEKLY ? repeatDaysOfWeek : null);
        replace(this.repeatDaysOfMonth, repeating && repeatType == RepeatType.MONTHLY ? repeatDaysOfMonth : null);
        this.excludeHoliday = repeating && excludeHoliday;
        refreshNotificationAt();
    }

    private static <T> void replace(Set<T> target, Set<T> source) {
        target.clear();
        if (source != null) target.addAll(source);
    }

    public boolean isRepeating() {
        return repeatType != null && repeatType != RepeatType.NONE;
    }

    /**
     * 특정 occurrence 날짜 기준 알림 발송 시각
     */
    public LocalDateTime notificationAtOn(LocalDate occurrenceDate) {
        if (notificationMinutes == null || occurrenceDate == null) return null;
        LocalTime time = endTime != null ? endTime : ALL_DAY_END_TIME;
        return LocalDateTime.of(occurrenceDate, time).minusMinutes(notificationMinutes);
    }

    // 단발성은 해당 날짜 기준으로 저장, 반복은 occurrence별로 계산하므로 저장하지 않음
    private void refreshNotificationAt() {
        this.notificationAt = isRepeating() ? null : notificationAtOn(startDate);
    }

    /**
     * 미루기 전용: 단발성 할 일의 날짜/시간만 이동 (다른 필드는 그대로)
     */
    public void rescheduleTo(LocalDate newDate, LocalTime newTime) {
        this.startDate = newDate;
        this.endDate = newDate;
        if (newTime != null && !newTime.equals(this.endTime)) {
            this.endTime = newTime;
            this.isAllDay = false;
        }
        refreshNotificationAt();
    }

    public void completeTask() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    public void undoCompleteTask() {
        this.isCompleted = false;
        this.completedAt = null;
    }

}