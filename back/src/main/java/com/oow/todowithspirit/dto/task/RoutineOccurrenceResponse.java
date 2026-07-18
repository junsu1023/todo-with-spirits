package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.RoutineCompletion;
import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class RoutineOccurrenceResponse extends TaskOccurrenceResponse {

    // 생성 결과와 매칭되는 루틴 고유 필드
    private final Boolean excludeHoliday;
    private final String repeatType;
    private final LocalDate repeatEndDate;
    private final Set<DayOfWeek> repeatDaysOfWeek;
    private final Set<Integer> repeatDaysOfMonth; // or List<Integer>

    public RoutineOccurrenceResponse(Task task, LocalDate occurrenceDate, RoutineCompletion completion) {
        // 루틴은 전개된 날짜(occurrenceDate)마다 완료 여부가 별도 맵으로 관리됨
        super(
                task,
                occurrenceDate,
                completion != null,
                completion != null ? completion.getCompletedAt() : null // 혹은 별도 완료일시 매핑
        );
        if (task.getNotificationMinutes() != null && task.getEndTime() != null) {
            this.notificationAt = LocalDateTime.of(occurrenceDate, task.getEndTime())
                    .minusMinutes(task.getNotificationMinutes());
        }
        this.excludeHoliday = task.isExcludeHoliday();
        this.repeatType = task.getRepeatType().name();
        this.repeatEndDate = task.getRepeatEndDate();
        this.repeatDaysOfWeek = task.getRepeatDaysOfWeek();
        this.repeatDaysOfMonth = task.getRepeatDaysOfMonth();
    }
}