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
    private String category;

    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private Boolean isAllDay;
    private Boolean isImportant;

    private String repeatType;
    private LocalDate repeatEndDate;

    private Integer notificationMinutes;
    private Boolean isPublic;
    private Boolean isCompleted;
    private String memo;
    private LocalDateTime updatedAt;

    public static TaskSummaryResponse from(Task task) {
        return TaskSummaryResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .startDate(task.getStartDate())
                .startTime(task.getStartTime())
                .endDate(task.getEndDate())
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .repeatType(task.getRepeatType().name())
                .repeatEndDate(task.getRepeatEndDate())
                .notificationMinutes(task.getNotificationMinutes())
                .isPublic(task.isPublic())
                .isCompleted(task.isCompleted())
                .memo(task.getMemo())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public static TaskSummaryResponse fromRoutineWithDateContext(Task task, boolean isCompleted, LocalDate targetDate) {
        return TaskSummaryResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .startDate(targetDate) // 조회 날짜
                .startTime(task.getStartTime()) // 루틴에 수행 시각이 설정되어 있다면 내려감
                .endDate(targetDate)
                .endTime(task.getEndTime())
                .isAllDay(task.isAllDay())
                .isImportant(task.isImportant())
                .repeatType(task.getRepeatType().name())
                .repeatEndDate(task.getRepeatEndDate())
                .notificationMinutes(task.getNotificationMinutes())
                .isPublic(task.isPublic())
                .isCompleted(isCompleted) // 테이블 원본 값 x / 특정일 완료 상태 o
                .memo(task.getMemo())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}