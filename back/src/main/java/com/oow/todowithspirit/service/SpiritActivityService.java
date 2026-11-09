package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritDailyLog;
import com.oow.todowithspirit.domain.spirit.SpiritDailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * TODO/루틴 완료로 생기는 정령의 생활 기록(일지)을 남긴다.
 * 하루에 여러 건을 완료해도 일지는 하루에 한 번만 생성된다.
 */
@Service
@RequiredArgsConstructor
public class SpiritActivityService {

    private final SpiritDailyLogRepository spiritDailyLogRepository;

    @Transactional
    public void recordActivity(Spirit spirit, LocalDate activityDate) {
        if (spiritDailyLogRepository.existsBySpiritIdAndLogDate(spirit.getId(), activityDate)) {
            return;
        }
        spiritDailyLogRepository.save(SpiritDailyLog.of(spirit.getId(), activityDate));
    }
}
