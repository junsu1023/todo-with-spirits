package com.oow.todowithspirit.dto.decoration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DecorationPurchaseResponse {

    private final Long decorationId;
    private final long balance;
}
