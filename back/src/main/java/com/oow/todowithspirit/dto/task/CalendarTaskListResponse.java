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

    private final int habitCount;
    private final int completedHabitCount;

    private final List<CalendarOccurrenceResponse> items;

    public static CalendarTaskListResponse of(List<CalendarOccurrenceResponse> items) {
        List<CalendarOccurrenceResponse> schedules = items.stream()
                .filter(t -> "SCHEDULE".equals(t.getTaskType()))
                .toList();
        List<CalendarOccurrenceResponse> habits = items.stream()
                .filter(t -> "HABIT".equals(t.getTaskType()))
                .toList();

        int completedScheduleCount = (int) schedules.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        int completedHabitCount    = (int) habits.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        int completedCount = completedScheduleCount + completedHabitCount;

        return new CalendarTaskListResponse(
                items.size(),
                completedCount,
                items.size() - completedCount,
                schedules.size(),
                completedScheduleCount,
                habits.size(),
                completedHabitCount,
                items
        );
    }
}
