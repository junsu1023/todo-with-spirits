package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class TaskCreateResponse {

    private Long taskId;
    private String type;
    private String title;
    private String memo;
    private String category;

    // 일정 전용
    private Boolean isAllDay;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private Boolean isImportant;

    // 반복
    private String repeatType;
    private LocalDate repeatEndDate;
    private List<String> repeatDaysOfWeek;
    private List<Integer> repeatDaysOfMonth;

    // 공통
    private Integer notificationMinutes;
    private Boolean isPublic;
    private LocalDateTime createdAt;

    public static TaskCreateResponse from(Task task) {
        return TaskCreateResponse.builder()
                .taskId(task.getId())
                .type(task.getType().name())
                .title(task.getTitle())
                .memo(task.getMemo())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .isAllDay(task.isAllDay())
                .startDate(task.getTaskDate())
                .startTime(task.getTaskTime())
                .endDate(task.getEndDate())
                .endTime(task.getEndTime())
                .isImportant(task.isImportant())
                .repeatType(task.getRepeatRule().name())
                .repeatEndDate(task.getRepeatEndDate())
                .repeatDaysOfWeek(
                        task.getRepeatDaysOfWeek().stream()
                                .map(DayOfWeek::name)
                                .collect(Collectors.toList())
                )
                .repeatDaysOfMonth(List.copyOf(task.getRepeatDaysOfMonth()))
                .notificationMinutes(task.getNotificationMinutes())
                .isPublic(task.isPublic())
                .createdAt(task.getCreatedAt())
                .build();
    }
}