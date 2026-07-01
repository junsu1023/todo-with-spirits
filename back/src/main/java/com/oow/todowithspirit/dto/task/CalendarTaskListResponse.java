package com.oow.todowithspirit.dto.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CalendarTaskListResponse {

    private final int totalCount;
    private final int completedCount;
    private final int incompleteCount;

    private final int scheduleCount;
    private final int completedScheduleCount;

    private final int routineCount;
    private final int completedRoutineCount;

    private final List<TaskSummaryResponse> items;

    public static CalendarTaskListResponse of(List<TaskSummaryResponse> items) {
        List<TaskSummaryResponse> schedules = items.stream()
                .filter(t -> "SCHEDULE".equals(t.getTaskType()))
                .toList();
        List<TaskSummaryResponse> routines = items.stream()
                .filter(t -> "HABIT".equals(t.getTaskType()))
                .toList();

        int completedScheduleCount = (int) schedules.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        int completedRoutineCount  = (int) routines.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        int completedCount = completedScheduleCount + completedRoutineCount;

        return new CalendarTaskListResponse(
                items.size(),
                completedCount,
                items.size() - completedCount,
                schedules.size(),
                completedScheduleCount,
                routines.size(),
                completedRoutineCount,
                items
        );
    }
}
