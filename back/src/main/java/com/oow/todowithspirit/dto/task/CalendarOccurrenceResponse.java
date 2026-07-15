package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.RoutineCompletion;
import com.oow.todowithspirit.domain.task.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CalendarOccurrenceResponse {

    private Long taskId;
    private String taskType;
    private String title;
    private String category;
    private String memo;

    private LocalDate occurrenceDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAllDay;
    private Boolean isImportant;
    private Integer notificationMinutes;
    private Boolean isPublic;

    private Boolean isCompleted;
    private LocalDateTime completedAt;

    public static CalendarOccurrenceResponse fromSchedule(Task task) {
        return CalendarOccurrenceResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .memo(task.getMemo())
                .occurrenceDate(task.getStartDate())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .notificationMinutes(task.getNotificationMinutes())
                .isPublic(task.isPublic())
                .isCompleted(task.isCompleted())
                .completedAt(task.getCompletedAt())
                .build();
    }

    public static CalendarOccurrenceResponse fromRoutineOccurrence(Task task, LocalDate occurrenceDate, RoutineCompletion completion) {
        return CalendarOccurrenceResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .memo(task.getMemo())
                .occurrenceDate(occurrenceDate)
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .notificationMinutes(task.getNotificationMinutes())
                .isPublic(task.isPublic())
                .isCompleted(completion != null)
                .completedAt(completion != null ? completion.getCompletedAt() : null)
                .build();
    }
}