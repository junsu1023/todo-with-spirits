package com.oow.todowithspirit.domain.task;

import com.oow.todowithspirit.domain.spirit.GrowthType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {

    NONE(GrowthType.CONSISTENCY, "미정"),
    WORK_STUDY(GrowthType.FOCUS, "학업/커리어"),
    HEALTH(GrowthType.ENERGY, "건강"),
    LIFE(GrowthType.CONSISTENCY, "생활"),
    RELATIONSHIP(GrowthType.CONSISTENCY, "인관관계/약속"),
    GROWTH(GrowthType.CREATIVITY, "자기계발"),
    HOBBY(GrowthType.CREATIVITY, "취미"),
    REST(GrowthType.ENERGY, "휴식/마인드"),
    FINANCE(GrowthType.CONSISTENCY, "자산/경제");

    private final GrowthType defaultGrowthType;
    private final String description;

    public static GrowthType resolveGrowthType(CategoryType category) {
        if (category == null) {
            return GrowthType.CONSISTENCY;
        }
        return category.getDefaultGrowthType();
    }
}