package com.oow.todowithspirit.dto.decoration;

import com.oow.todowithspirit.domain.decoration.Decoration;
import com.oow.todowithspirit.dto.common.AssetResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DecorationResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final long price;
    private final AssetResponse image;
    private final boolean purchased;
    private final boolean installed;

    public static DecorationResponse of(Decoration decoration, boolean purchased, boolean installed) {
        return new DecorationResponse(
                decoration.getId(),
                decoration.getCode(),
                decoration.getDecorationName(),
                decoration.getPrice(),
                AssetResponse.of(decoration.getAssetKey(), decoration.getImageUrl(), decoration.getImageFileName()),
                purchased,
                installed
        );
    }
}
