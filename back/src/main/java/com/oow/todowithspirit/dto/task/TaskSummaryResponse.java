package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class TaskSummaryResponse {

    private Long taskId;
    private String taskType;
    private String title;

    private LocalDate endDate;
    private LocalTime endTime;
    private Boolean isAllDay;
    private Boolean isImportant;

    private String repeatType;
    private LocalDate repeatEndDate;

    private Integer notificationMinutes;
    private Boolean isCompleted;
    private LocalDateTime updatedAt;

    public static TaskSummaryResponse from(Task task) {
        return TaskSummaryResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .endDate(task.getEndDate())
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .repeatType(task.getRepeatType().name())
                .repeatEndDate(task.getRepeatEndDate())
                .notificationMinutes(task.getNotificationMinutes())
                .isCompleted(task.isCompleted())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public static TaskSummaryResponse fromRoutineWithDateContext(Task task, boolean isCompleted, LocalDate targetDate) {
        return TaskSummaryResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .endDate(targetDate)
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .repeatType(task.getRepeatType().name())
                .repeatEndDate(task.getRepeatEndDate())
                .notificationMinutes(task.getNotificationMinutes())
                .isCompleted(isCompleted) // 테이블 원본 값 x / 특정일 완료 상태 o
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}