package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.LocalTime;

// 일정을 위한 전용 응답 DTO
@Getter
public class ScheduleOccurrenceResponse extends TaskOccurrenceResponse {
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Boolean isAllDay;
    private final Integer notificationMinutes;

    public ScheduleOccurrenceResponse(Task task) {
        super(task, task.getStartDate(), task.isCompleted(), task.getCompletedAt());
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
        this.isAllDay = task.isAllDay();
        this.notificationMinutes = task.getNotificationMinutes();
    }
}