package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritVariant;
import com.oow.todowithspirit.dto.common.AssetResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpiritVariantResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final AssetResponse image;
    private final boolean unlocked;

    public static SpiritVariantResponse of(SpiritVariant variant, boolean unlocked) {
        return new SpiritVariantResponse(
                variant.getId(),
                variant.getCode(),
                variant.getVariantName(),
                AssetResponse.of(variant.getAssetKey(), variant.getImageUrl(), variant.getImageFileName()),
                unlocked
        );
    }
}
