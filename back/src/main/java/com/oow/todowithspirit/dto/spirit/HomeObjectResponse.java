package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritHomeObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeObjectResponse {

    private final Long id;
    private final String code;
    private final String objectName;
    private final Long placeId;
    private final String iconUrl;
    private final boolean installed;

    public static HomeObjectResponse of(SpiritHomeObject object, boolean installed) {
        return new HomeObjectResponse(object.getId(), object.getCode(), object.getObjectName(), object.getPlaceId(), object.getIconUrl(), installed);
    }
}