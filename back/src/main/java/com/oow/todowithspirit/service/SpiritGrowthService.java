package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.spirit.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * TODO/루틴 완료로 생기는 정령의 생활 기록(활동일)을 반영한다.
 * 하루에 여러 건을 완료해도 활동일수는 하루에 한 번만 증가한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritGrowthService {

    private final SpiritDailyLogRepository spiritDailyLogRepository;
    private final SpiritStageHistoryRepository spiritStageHistoryRepository;

    @Transactional
    public void recordActivity(Spirit spirit, LocalDate activityDate) {
        if (spiritDailyLogRepository.existsBySpiritIdAndLogDate(spirit.getId(), activityDate)) {
            return;
        }
        spiritDailyLogRepository.save(SpiritDailyLog.of(spirit.getId(), activityDate));

        int stageBefore = spirit.getStage();
        boolean matured = spirit.recordActiveDay(activityDate);
        if (matured) {
            spiritStageHistoryRepository.save(
                    SpiritStageHistory.of(spirit.getId(), spirit.getUser().getId(), stageBefore, spirit.getStage())
            );
            log.info("[recordActivity] Spirit matured into adult. spiritId: {}, careerGrowthType: {}",
                    spirit.getId(), spirit.getCareerGrowthType());
        }
    }
}