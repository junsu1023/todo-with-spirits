package com.oow.todowithspirit.dto.task;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CalendarTasksResponse {
    private final int totalCount;
    private final int completedCount;
    private final int inCompleteCount;
    private final List<TaskOccurrenceResponse> items;

    public static CalendarTasksResponse of(List<TaskOccurrenceResponse> items) {
        int completed = (int) items.stream().filter(i -> Boolean.TRUE.equals(i.getIsCompleted())).count();
        return CalendarTasksResponse.builder()
                .totalCount(items.size())
                .completedCount(completed)
                .inCompleteCount(items.size() - completed)
                .items(items)
                .build();
    }
}
