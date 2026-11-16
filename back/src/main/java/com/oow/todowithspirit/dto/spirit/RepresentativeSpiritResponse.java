package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritTrait;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RepresentativeSpiritResponse {
    private final Long id;
    private final String spiritName;
    private final int exp;
    private final String imageUrl;
    private final List<SpiritTrait> trait;

    public static RepresentativeSpiritResponse from(Spirit spirit, List<SpiritTrait> traits) {
        return new RepresentativeSpiritResponse(
                spirit.getId(),
                spirit.getSpiritName(),
                spirit.getExp(),
                spirit.getImageUrl(),
                traits
        );
    }
}
