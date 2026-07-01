package com.oow.todowithspirit.dto.task;

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
public class RoutineCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    // 루틴은 DAILY, WEEKLY, MONTHLY 만 허용 (NONE, YEARLY 불가)
    @NotNull(message = "Repeat type is required")
    private RepeatType repeatType;

    private LocalDate repeatEndDate;

    // 매주 반복 시 선택 요일 (MONDAY ~ SUNDAY)
    private Set<DayOfWeek> repeatDaysOfWeek;

    // 매월 반복 시 선택 일 (1 ~ 31)
    private Set<Integer> repeatDaysOfMonth;

    private NotificationType notification;

    private Boolean isPublic;

    @Size(max = 2000, message = "Memo must be 2000 characters or less")
    private String memo;
}