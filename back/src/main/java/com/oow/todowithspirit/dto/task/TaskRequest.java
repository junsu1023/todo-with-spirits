package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.NotificationType;
import com.oow.todowithspirit.domain.task.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 할 일 생성/수정 공통 요청. repeatType이 없거나 NONE이면 단발성, DAILY/WEEKLY/MONTHLY면 반복.
 */
@Getter
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    // 단발성: 마감 일시 / 반복: 반복 시작일 + 매 회차 시각
    // 생성 시 null이면 오늘 하루 종일, 수정 시 null이면 기존 날짜·시각 유지
    private LocalDateTime endDateTime;

    // true면 endDateTime의 시각은 무시 (기본값: endDateTime이 없으면 true, 있으면 false)
    private Boolean isAllDay;

    private Boolean isImportant;

    private NotificationType notificationType;

    // 반복 (기본값: NONE)
    private RepeatType repeatType;

    private LocalDate repeatEndDate;

    // WEEKLY 필수 (MONDAY ~ SUNDAY)
    private Set<DayOfWeek> repeatDaysOfWeek;

    // MONTHLY 필수 (1 ~ 31)
    private Set<Integer> repeatDaysOfMonth;

    // 반복일 때만 적용
    private Boolean excludeHoliday;
}
