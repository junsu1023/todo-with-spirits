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
public class ScheduleCreateResponse {

    private Long taskId;
    private String taskType;
    private String title;
    private String memo;
    private String category;

    // 일정 전용
    private Boolean isAllDay;
    private LocalDate endDate;
    private LocalTime endTime;
    private Boolean isImportant;

    // 공통
    private Integer notificationMinutes;
    private LocalDateTime notificationAt;
    private Boolean isPublic;

    // 성장
    private Integer growthValue;
    private String growthType;

    // 완료
    private Boolean isCompleted;
    private LocalDateTime completedAt;

    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleCreateResponse from(Task task) {
        return ScheduleCreateResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .memo(task.getMemo())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .isAllDay(task.isAllDay())
                .endDate(task.getEndDate())
                .endTime(task.getEndTime())
                .isImportant(task.isImportant())
                .notificationMinutes(task.getNotificationMinutes())
                .notificationAt(task.getNotificationAt())
                .isPublic(task.isPublic())
                .growthValue(task.getGrowthValue())
                .growthType(task.getGrowthType().name())
                .isCompleted(task.isCompleted())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}