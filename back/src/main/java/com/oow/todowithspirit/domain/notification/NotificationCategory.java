package com.oow.todowithspirit.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

    SYSTEM("시스템 알림"),
    EVENT("이벤트"),
    SPIRIT("정령의 숲");

    private final String description;
}