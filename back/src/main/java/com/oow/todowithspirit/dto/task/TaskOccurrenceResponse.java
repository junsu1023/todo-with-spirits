package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 공통 필드를 정의하는 추상 클래스
@Getter
public abstract class TaskOccurrenceResponse {

    private final Long taskId;
    private final String taskType;
    private final String title;
    private final String category;
    private final String memo;
    private final LocalDate occurrenceDate;
    private final Boolean isImportant;
    private final Boolean isPublic;
    private final Boolean isCompleted;
    private final LocalDateTime completedAt;

    protected TaskOccurrenceResponse(Task task, LocalDate occurrenceDate, Boolean isCompleted, LocalDateTime completedAt) {
        this.taskId = task.getId();
        this.taskType = task.getTaskType().name();
        this.title = task.getTitle();
        this.category = task.getCategory() != null ? task.getCategory().name() : null;
        this.memo = task.getMemo();
        this.occurrenceDate = occurrenceDate;
        this.isImportant = task.isImportant();
        this.isPublic = task.isPublic();
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }
}