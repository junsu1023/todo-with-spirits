package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    private Boolean isImportant;

    @NotNull(message = "endDateTime is required")
    private LocalDateTime endDateTime;

    @NotNull(message = "isAllDay is required")
    private Boolean isAllDay;

    private NotificationType notificationType;
}