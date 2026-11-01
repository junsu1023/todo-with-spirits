package com.oow.todowithspirit.dto.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskCompleteResponse {

    private final long currencyEarned;
    private final long balance;
}
