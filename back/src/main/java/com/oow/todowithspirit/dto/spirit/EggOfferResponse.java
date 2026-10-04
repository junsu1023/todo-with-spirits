package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritEggOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EggOfferResponse {

    private final Long offerId;
    private final String code;
    private final String eggName;
    private final String description;
    private final String imageUrl;

    public static EggOfferResponse of(Long offerId, SpiritEggOption egg) {
        return new EggOfferResponse(offerId, egg.getCode(), egg.getEggName(), egg.getDescription(), egg.getImageUrl());
    }
}