package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritDailyLogRepository;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.dto.spirit.SpiritDailyLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 정령의 하루 기록 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpiritHistoryService {

    private final SpiritRepository spiritRepository;
    private final SpiritDailyLogRepository spiritDailyLogRepository;

    public List<SpiritDailyLogResponse> getDailyLogs(Long userId, Long spiritId) {
        Spirit spirit = findOwnedSpirit(userId, spiritId);
        return spiritDailyLogRepository.findAllBySpiritIdOrderByLogDateDesc(spirit.getId()).stream()
                .map(SpiritDailyLogResponse::from)
                .toList();
    }

    private Spirit findOwnedSpirit(Long userId, Long spiritId) {
        Spirit spirit = spiritRepository.findById(spiritId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Spirit not found"));
        if (!spirit.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this spirit");
        }
        return spirit;
    }
}
