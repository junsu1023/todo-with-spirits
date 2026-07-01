package com.oow.todowithspirit.domain.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    NONE(null),
    TEN_MINUTES(10),
    THIRTY_MINUTES(30),
    ONE_HOUR(60);

    private final Integer minutes;
}