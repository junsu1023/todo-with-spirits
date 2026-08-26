package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    public static RepresentativeSpiritResponse from(Spirit spirit) {
        return new RepresentativeSpiritResponse(
                spirit.getId(),
                spirit.getSpiritName(),
                spirit.getStage(),
                spirit.getExp(),
                spirit.getFocusExp(),
                spirit.getEnergyExp(),
                spirit.getConsistencyExp(),
                spirit.getCreativityExp(),
                spirit.getImageUrl()
        );
    }
}