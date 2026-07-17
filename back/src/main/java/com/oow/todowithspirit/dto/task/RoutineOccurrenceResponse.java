package com.oow.todowithspirit.dto.task;

import com.oow.todowithspirit.domain.task.RoutineCompletion;
import com.oow.todowithspirit.domain.task.Task;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RoutineOccurrenceResponse extends TaskOccurrenceResponse {

    public RoutineOccurrenceResponse(Task task, LocalDate occurrenceDate, RoutineCompletion completion) {
        super(
                task,
                occurrenceDate,
                completion != null,
                completion != null ? completion.getCompletedAt() : null
        );
    }
}