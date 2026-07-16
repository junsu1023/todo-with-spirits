package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.RepeatType;
import com.oow.todowithspirit.domain.task.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class RoutineCreateResponse {

    private Long taskId;
    private String taskType;
    private String title;
    private String memo;
    private String category;

    // 반복
    private String repeatType;
    private LocalDate repeatEndDate;
    private List<String> repeatDaysOfWeek;
    private List<Integer> repeatDaysOfMonth;

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

    public static RoutineCreateResponse from(Task task) {
        return RoutineCreateResponse.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .memo(task.getMemo())
                .category(task.getCategory() != null ? task.getCategory().name() : null)
                .repeatType(task.getRepeatType() != null ? task.getRepeatType().name() : RepeatType.NONE.name())
                .repeatEndDate(task.getRepeatEndDate())
                .repeatDaysOfWeek(task.getRepeatDaysOfWeek().stream().map(DayOfWeek::name).toList())
                .repeatDaysOfMonth(List.copyOf(task.getRepeatDaysOfMonth()))
                .notificationMinutes(task.getNotificationMinutes())
                .notificationAt(task.getNotificationAt())
                .isPublic(task.isPublic())
                .growthValue(task.getGrowthValue())
                .growthType(task.getGrowthType() != null ? task.getGrowthType().name() : null)
                .isCompleted(task.isCompleted())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}