package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritDailyLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SpiritDailyLogResponse {

    private final LocalDate logDate;
    private final String content;

    public static SpiritDailyLogResponse from(SpiritDailyLog log) {
        return new SpiritDailyLogResponse(log.getLogDate(), log.getContent());
    }
}