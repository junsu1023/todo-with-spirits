package com.oow.todowithspirit.dto.task;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "taskType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ScheduleOccurrenceResponse.class, name = "SCHEDULE"),
        @JsonSubTypes.Type(value = RoutineOccurrenceResponse.class, name = "ROUTINE")
})
public abstract class TaskOccurrenceResponse {
    private final Long taskId;
    private final String taskType;
    private final String title;
    private final String memo;
    private final String category;

    // 캘린더 전개를 위한 타겟 날짜 (공통)
    private final LocalDate occurrenceDate;

    // 공통 필드들
    private final Boolean isImportant;
    private final Boolean isPublic;
    private final Integer notificationMinutes;
    protected LocalDateTime notificationAt;

    // 성장 및 완료
    private final Integer growthValue;
    private final String growthType;
    private final Boolean isCompleted;
    private final LocalDateTime completedAt;

    // 타임스탬프
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    protected TaskOccurrenceResponse(Task task, LocalDate occurrenceDate, Boolean isCompleted, LocalDateTime completedAt) {
        this.taskId = task.getId();
        this.taskType = task.getTaskType().name();
        this.title = task.getTitle();
        this.memo = task.getMemo();
        this.category = task.getCategory() != null ? task.getCategory().name() : null;
        this.occurrenceDate = occurrenceDate;
        this.isImportant = task.isImportant();
        this.isPublic = task.isPublic();
        this.notificationMinutes = task.getNotificationMinutes();
        this.notificationAt = task.getNotificationAt();
        this.growthValue = task.getGrowthValue();
        this.growthType = task.getGrowthType() != null ? task.getGrowthType().name() : null;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }
}