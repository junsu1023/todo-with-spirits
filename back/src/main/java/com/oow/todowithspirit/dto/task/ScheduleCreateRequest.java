package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.CategoryType;
import com.oow.todowithspirit.domain.task.NotificationOption;
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
public class ScheduleCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    private Boolean isImportant;

    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;

    private Boolean isAllDay;

    @NotNull(message = "Repeat type is required")
    private RepeatType repeatType;

    private LocalDate repeatEndDate;

    // 매주 반복 시 선택 요일 (MONDAY ~ SUNDAY)
    private Set<DayOfWeek> repeatDaysOfWeek;

    // 매월 반복 시 선택 일 (1 ~ 31)
    private Set<Integer> repeatDaysOfMonth;

    private NotificationOption notification;

    private CategoryType category;

    private Boolean isPublic;

    @Size(max = 2000, message = "Memo must be 2000 characters or less")
    private String memo;
}