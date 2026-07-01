package com.oow.todowithspirit.dto.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskDeleteResponse {

    private final int deletedCount;

    public static TaskDeleteResponse of(int deletedCount) {
        return new TaskDeleteResponse(deletedCount);
    }
}
