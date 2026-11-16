package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritSpecies;
import com.oow.todowithspirit.domain.spirit.SpiritTrait;
import com.oow.todowithspirit.dto.common.AssetResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RepresentativeSpiritResponse {
    private final Long id;
    private final String spiritName;
    private final int exp;
    private final AssetResponse image; // 정령 모습 (종류 마스터 이미지)
    private final List<SpiritTrait> trait;

    public static RepresentativeSpiritResponse from(Spirit spirit, SpiritSpecies species, List<SpiritTrait> traits) {
        return new RepresentativeSpiritResponse(
                spirit.getId(),
                spirit.getSpiritName(),
                spirit.getExp(),
                AssetResponse.of(species.getAssetKey(), species.getImageUrl(), species.getImageFileName()),
                traits
        );
    }
}
