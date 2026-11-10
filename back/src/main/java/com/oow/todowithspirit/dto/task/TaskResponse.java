package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class TaskResponse {

    private Long taskId;
    private String title;

    // 단발성: 해당 날짜 / 반복: 반복 시작일
    private LocalDate startDate;
    private LocalTime endTime;
    private Boolean isAllDay;
    private Boolean isImportant;

    // 반복
    private Boolean isRepeating;
    private String repeatType;
    private LocalDate repeatEndDate;
    private List<String> repeatDaysOfWeek;
    private List<Integer> repeatDaysOfMonth;
    private Boolean excludeHoliday;

    // 알림 (반복은 다음 회차 기준이 아니라 occurrence 조회 시 계산되므로 null)
    private Integer notificationMinutes;
    private LocalDateTime notificationAt;

    // 성장
    private Integer growthValue;
    private String growthType;

    // 완료 (단발성 전용. 반복은 occurrence 조회로 확인)
    private Boolean isCompleted;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .startDate(task.getStartDate())
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .isRepeating(task.isRepeating())
                .repeatType(task.getRepeatType().name())
                .repeatEndDate(task.getRepeatEndDate())
                .repeatDaysOfWeek(task.getRepeatDaysOfWeek().stream().sorted().map(DayOfWeek::name).toList())
                .repeatDaysOfMonth(task.getRepeatDaysOfMonth().stream().sorted().toList())
                .excludeHoliday(task.isExcludeHoliday())
                .notificationMinutes(task.getNotificationMinutes())
                .notificationAt(task.getNotificationAt())
                .growthValue(task.getGrowthValue())
                .growthType(task.getGrowthType().name())
                .isCompleted(task.isCompleted())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
