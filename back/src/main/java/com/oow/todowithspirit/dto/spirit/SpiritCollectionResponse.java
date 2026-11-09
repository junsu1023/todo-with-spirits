package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritSpecies;
import com.oow.todowithspirit.dto.common.AssetResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 정령도감의 한 칸 (정령 종류 1개).
 * 보유하지 않은 종류는 isOwned=false 이고 정령 개체 관련 필드는 모두 null.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SpiritCollectionResponse {

    // 정령 종류
    private final Long speciesId;
    private final String code;
    private final String name;
    private final String description;
    private final AssetResponse image;

    // 보유 여부
    private final Boolean isOwned;

    // 보유한 정령 개체 (미보유면 null)
    private final Long spiritId;
    private final String spiritName;
    private final Integer affection;      // 호감도 (0~100%)
    private final Integer affectionLevel; // 호감도 단계 (1~5)
    private final Boolean isImportant;
    private final Boolean isCompleted;    // 다 자람 (성체)
    private final Boolean isRepresentative;

    public static SpiritCollectionResponse notOwned(SpiritSpecies species) {
        return base(species).isOwned(false).build();
    }

    public static SpiritCollectionResponse owned(SpiritSpecies species, Spirit spirit, Long representativeSpiritId) {
        return base(species)
                .isOwned(true)
                .spiritId(spirit.getId())
                .spiritName(spirit.getSpiritName())
                .affection(spirit.getAffection())
                .affectionLevel(spirit.getAffectionLevel())
                .isImportant(spirit.isImportant())
                .isCompleted(spirit.isCompleted())
                .isRepresentative(spirit.getId().equals(representativeSpiritId))
                .build();
    }

    private static SpiritCollectionResponseBuilder base(SpiritSpecies species) {
        return SpiritCollectionResponse.builder()
                .speciesId(species.getId())
                .code(species.getCode())
                .name(species.getSpeciesName())
                .description(species.getDescription())
                .image(AssetResponse.of(species.getAssetKey(), species.getImageUrl(), species.getImageFileName()));
    }
}
