package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

// 일정을 위한 전용 응답 DTO
@Getter
public class ScheduleOccurrenceResponse extends TaskOccurrenceResponse {

    // 생성 결과와 매칭되는 일정 고유 필드
    private final Boolean isAllDay;
    private final LocalDate endDate;
    private final LocalTime endTime;

    public ScheduleOccurrenceResponse(Task task) {
        // 일정은 도메인 정책상 생성 시 단발성이므로 task 고유의 완료 상태를 그대로 전달
        super(task, task.getStartDate(), task.isCompleted(), task.getCompletedAt());
        this.isAllDay = task.isAllDay();
        this.endDate = task.getEndDate();
        this.endTime = task.getEndTime();
    }
}