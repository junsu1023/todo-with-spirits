package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritStatus;
import com.oow.todowithspirit.domain.spirit.SpiritTemperament;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RepresentativeSpiritResponse {
    private final Long id;
    private final String spiritName;
    private final int stage;
    private final int exp;
    private final int focusExp;
    private final int vitalityExp;
    private final int consistencyExp;
    private final int creativityExp;
    private final String imageUrl;
    private final SpiritStatus status;
    private final int activeDaysCount;
    private final int targetGrowthDays;
    private final String careerGrowthType;
    private final List<SpiritTemperament> temperaments;

    public static RepresentativeSpiritResponse from(Spirit spirit, List<SpiritTemperament> temperaments) {
        return new RepresentativeSpiritResponse(
                spirit.getId(),
                spirit.getSpiritName(),
                spirit.getStage(),
                spirit.getExp(),
                spirit.getFocusExp(),
                spirit.getEnergyExp(),
                spirit.getConsistencyExp(),
                spirit.getCreativityExp(),
                spirit.getImageUrl(),
                spirit.getStatus(),
                spirit.getActiveDaysCount(),
                spirit.getTargetGrowthDays(),
                spirit.getCareerGrowthType() != null ? spirit.getCareerGrowthType().name() : null,
                temperaments
        );
    }
}