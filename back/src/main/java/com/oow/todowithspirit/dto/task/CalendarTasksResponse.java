package com.oow.todowithspirit.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CalendarTasksResponse {
    private final int completedCount;
    private final int completedRoutineCount;
    private final int completedScheduleCount;
    private final int inCompleteCount; // 스펠링 가이드라인(inCompleteCount) 반영
    private final int routineCount;
    private final int scheduleCount;
    private final int totalCount;
    private final List<TaskOccurrenceResponse> items;

    public static CalendarTasksResponse of(List<TaskOccurrenceResponse> items) {
        int completedRoutine = 0;
        int completedSchedule = 0;
        int routine = 0;
        int schedule = 0;

        for (TaskOccurrenceResponse item : items) {
            boolean isRoutine = "ROUTINE".equalsIgnoreCase(item.getTaskType());
            boolean isSchedule = "SCHEDULE".equalsIgnoreCase(item.getTaskType());
            boolean isDone = item.getIsCompleted() != null && item.getIsCompleted();

            if (isRoutine) {
                routine++;
                if (isDone) completedRoutine++;
            } else if (isSchedule) {
                schedule++;
                if (isDone) completedSchedule++;
            }
        }

        int total = routine + schedule;
        int totalCompleted = completedRoutine + completedSchedule;
        int incomplete = total - totalCompleted;

        return CalendarTasksResponse.builder()
                .completedCount(totalCompleted)
                .completedRoutineCount(completedRoutine)
                .completedScheduleCount(completedSchedule)
                .inCompleteCount(incomplete)
                .routineCount(routine)
                .scheduleCount(schedule)
                .totalCount(total)
                .items(items)
                .build();
    }
}