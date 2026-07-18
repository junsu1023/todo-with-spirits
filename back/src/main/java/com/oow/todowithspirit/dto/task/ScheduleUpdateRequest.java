package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.CategoryType;
import com.oow.todowithspirit.domain.task.NotificationType;
import com.oow.todowithspirit.domain.task.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class ScheduleUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    @NotBlank(message = "Repeat type is required")
    private RepeatType repeatType;

    private LocalDate repeatEndDate;

    private Set<DayOfWeek> repeatDaysOfWeek; // repeatType이 WEEKLY일 때 필수

    private Set<Integer> repeatDaysOfMonth;  // repeatType이 MONTHLY일 때 필수

    private Boolean isImportant;

    @NotNull(message = "endDateTime is required")
    private LocalDateTime endDateTime;

    @NotNull(message = "isAllDay is required")
    private Boolean isAllDay;

    private NotificationType notificationType;

    private CategoryType category;

    private Boolean isPublic;

    @Size(max = 2000, message = "Memo must be 2000 characters or less")
    private String memo;
}