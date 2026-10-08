package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritLetter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SpiritLetterResponse {

    private final Long id;
    private final Long spiritId;
    private final String content;
    private final LocalDateTime sentAt;
    private final LocalDateTime readAt;

    public static SpiritLetterResponse from(SpiritLetter letter) {
        return new SpiritLetterResponse(letter.getId(), letter.getSpiritId(), letter.getContent(), letter.getSentAt(), letter.getReadAt());
    }
}