package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    public static SpiritResponse of(Spirit spirit, Long representativeSpiritId) {
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
                isRep
        );
    }
}