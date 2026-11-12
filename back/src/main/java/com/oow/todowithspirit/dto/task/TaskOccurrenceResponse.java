package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 특정 날짜에 펼쳐진 할 일 1건 (단발성/반복 공통)
 */
@Getter
public class TaskOccurrenceResponse {

    private final Long taskId;
    private final String title;
    private final LocalDate occurrenceDate;
    private final LocalTime endTime;
    private final Boolean isAllDay;
    private final Boolean isImportant;

    private final String repeatType;
    private final LocalDate repeatEndDate;
    private final List<String> repeatDaysOfWeek;
    private final List<Integer> repeatDaysOfMonth;
    private final Boolean excludeHoliday;

    private final Integer notificationMinutes;
    private final LocalDateTime notificationAt;

    private final Integer growthValue;
    private final String growthType;

    // 해당 occurrence 기준 완료 여부
    private final Boolean isCompleted;
    private final LocalDateTime completedAt;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TaskOccurrenceResponse(Task task, LocalDate occurrenceDate, boolean isCompleted, LocalDateTime completedAt) {
        this.taskId = task.getId();
        this.title = task.getTitle();
        this.occurrenceDate = occurrenceDate;
        this.endTime = task.getEndTime();
        this.isAllDay = task.isAllDay();
        this.isImportant = task.isImportant();
        this.repeatType = task.getRepeatType().name();
        this.repeatEndDate = task.getRepeatEndDate();
        this.repeatDaysOfWeek = task.getRepeatDaysOfWeek().stream().sorted().map(DayOfWeek::name).toList();
        this.repeatDaysOfMonth = task.getRepeatDaysOfMonth().stream().sorted().toList();
        this.excludeHoliday = task.isExcludeHoliday();
        this.notificationMinutes = task.getNotificationMinutes();
        this.notificationAt = task.notificationAtOn(occurrenceDate);
        this.growthValue = task.getGrowthValue();
        this.growthType = task.getGrowthType().name();
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }
}
