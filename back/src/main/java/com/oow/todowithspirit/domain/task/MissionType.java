package com.oow.todowithspirit.domain.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionType {
    DAILY("일일 미션", "THUMB_UP"),
    CONSISTENCY("끈기 스코어", "FLAME"),
    HIDDEN("히든 미션", "DIAMOND");

    private final String label;
    private final String defaultIconType;
}