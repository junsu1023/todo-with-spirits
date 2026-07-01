package com.oow.todowithspirit.dto.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskListResponse<T> {

    private final int totalCount;
    private final List<T> items;

    public static <T> TaskListResponse<T> of(List<T> items) {
        return new TaskListResponse<>(items.size(), items);
    }
}