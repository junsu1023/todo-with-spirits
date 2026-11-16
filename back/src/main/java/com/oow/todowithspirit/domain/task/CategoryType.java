package com.oow.todowithspirit.domain.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {

    NONE("미정"),
    WORK_STUDY("학업/커리어"),
    HEALTH("건강"),
    LIFE("생활"),
    RELATIONSHIP("인간관계/약속"),
    GROWTH("자기계발"),
    HOBBY("취미"),
    REST("휴식/마인드"),
    FINANCE("자산/경제");

    private final String description;
}
