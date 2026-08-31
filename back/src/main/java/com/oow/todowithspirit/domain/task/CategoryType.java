package com.oow.todowithspirit.domain.task;

import com.oow.todowithspirit.domain.spirit.GrowthType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {

    NONE(GrowthType.CONSISTENCY),
    WORK_STUDY(GrowthType.FOCUS),
    HEALTH(GrowthType.ENERGY),
    LIFE(GrowthType.CONSISTENCY),
    RELATIONSHIP(GrowthType.CONSISTENCY),
    GROWTH(GrowthType.CREATIVITY),
    HOBBY(GrowthType.CREATIVITY),
    REST(GrowthType.ENERGY),
    FINANCE(GrowthType.CONSISTENCY);

    private final GrowthType defaultGrowthType;

    public static GrowthType resolveGrowthType(CategoryType category) {
        if (category == null) {
            return GrowthType.CONSISTENCY;
        }
        return category.getDefaultGrowthType();
    }
}