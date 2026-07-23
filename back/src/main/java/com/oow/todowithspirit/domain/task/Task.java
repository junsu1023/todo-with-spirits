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

    // 일정 일시
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
    private Set<DayOfWeek> repeatDaysOfWeek = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_repeat_days_of_month", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "day_of_month")
    private Set<Integer> repeatDaysOfMonth = new HashSet<>();

    // 알림
    @Column(name = "notification_minutes")
    private Integer notificationMinutes;

    // 공개 상태
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    // 성장 (서버 계산)
    @Column(nullable = false)
    private int growthValue = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "growth_type", nullable = true)  // todo: nullable false로 변경
    private GrowthType growthType;  // Task 완료 시 서버가 계산하여 설정; 생성 시점엔 null

    // 완료
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static Task createSchedule(User user, String title, String memo, CategoryType category,
                                      LocalDate startDate, LocalTime startTime,
                                      LocalDate endDate, LocalTime endTime,
                                      boolean isAllDay, boolean isImportant,
                                      RepeatType repeatType, LocalDate repeatEndDate,
                                      Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                                      Integer notificationMinutes, boolean isPublic) {
        Task task = new Task();
        task.user = user;
        task.taskType = TaskType.SCHEDULE;
        task.title = title;
        task.memo = memo;
        task.category = category != null ? category : CategoryType.NONE;
        task.startDate = startDate;
        task.startTime = isAllDay ? null : startTime;
        task.endDate = endDate;
        task.endTime = isAllDay ? null : endTime;
        task.isAllDay = isAllDay;
        task.isImportant = isImportant;
        task.repeatType = repeatType;
        task.repeatEndDate = repeatEndDate;
        task.repeatDaysOfWeek = repeatDaysOfWeek != null ? repeatDaysOfWeek : new HashSet<>();
        task.repeatDaysOfMonth = repeatDaysOfMonth != null ? repeatDaysOfMonth : new HashSet<>();
        task.notificationMinutes = notificationMinutes;
        task.isPublic = isPublic;
        return task;
    }

    public static Task createRoutine(User user, String title, String memo,
                                     RepeatType repeatType, LocalDate repeatEndDate,
                                     Set<DayOfWeek> repeatDaysOfWeek, Set<Integer> repeatDaysOfMonth,
                                     Integer notificationMinutes, boolean isPublic) {
        Task task = new Task();
        task.user = user;
        task.taskType = TaskType.HABIT;
        task.title = title;
        task.memo = memo;
        task.category = CategoryType.NONE;
        task.startDate = LocalDate.now();
        task.repeatType = repeatType;
        task.repeatEndDate = repeatEndDate;
        task.repeatDaysOfWeek = repeatDaysOfWeek != null ? repeatDaysOfWeek : new HashSet<>();
        task.repeatDaysOfMonth = repeatDaysOfMonth != null ? repeatDaysOfMonth : new HashSet<>();
        task.notificationMinutes = notificationMinutes;
        task.isPublic = isPublic;
        return task;
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