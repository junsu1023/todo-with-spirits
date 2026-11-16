package com.oow.todowithspirit.dto.spirit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpiritMeetResponse {

    private final Long id;
    private final String name;
    private final long balance;
}
