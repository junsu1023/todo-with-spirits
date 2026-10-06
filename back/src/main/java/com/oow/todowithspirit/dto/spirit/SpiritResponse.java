package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritStatus;
import com.oow.todowithspirit.domain.spirit.SpiritTemperament;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SpiritResponse {
    private Long id;
    private String spiritName;
    private int stage;
    private int exp;
    private int focusExp;
    private int energyExp;
    private int consistencyExp;
    private int creativityExp;
    private String imageUrl;
    private boolean isRepresentative;
    private SpiritStatus status;
    private int activeDaysCount;
    private int targetGrowthDays;
    private String careerGrowthType;
    private List<SpiritTemperament> temperaments;

    public static SpiritResponse of(Spirit spirit, Long representativeSpiritId, List<SpiritTemperament> temperaments) {
        boolean isRep = representativeSpiritId != null && representativeSpiritId.equals(spirit.getId());
        return new SpiritResponse(
                spirit.getId(),
                spirit.getSpiritName(),
                spirit.getStage(),
                spirit.getExp(),
                spirit.getFocusExp(),
                spirit.getEnergyExp(),
                spirit.getConsistencyExp(),
                spirit.getCreativityExp(),
                spirit.getImageUrl(),
                isRep,
                spirit.getStatus(),
                spirit.getActiveDaysCount(),
                spirit.getTargetGrowthDays(),
                spirit.getCareerGrowthType() != null ? spirit.getCareerGrowthType().name() : null,
                temperaments
        );
    }
}