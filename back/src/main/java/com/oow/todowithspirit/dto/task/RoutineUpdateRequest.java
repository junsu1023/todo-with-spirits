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
import java.util.Set;

@Getter
public class RoutineUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    @NotNull(message = "Repeat type is required")
    private RepeatType repeatType;

    private LocalDate repeatEndDate;

    private Set<DayOfWeek> repeatDaysOfWeek;

    private Set<Integer> repeatDaysOfMonth;

    private CategoryType category;

    private NotificationType notificationType;

    private Boolean isPublic;

    private Boolean excludeHoliday;

    @Size(max = 2000, message = "Memo must be 2000 characters or less")
    private String memo;
}