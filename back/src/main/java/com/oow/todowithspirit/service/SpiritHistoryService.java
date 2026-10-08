package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.*;
import com.oow.todowithspirit.domain.task.Task;
import com.oow.todowithspirit.domain.task.TaskRepository;
import com.oow.todowithspirit.dto.spirit.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 함께했던 정령 목록과, 정령이 살아있는 동안 쌓인 하루기록/편지/발견물/실제 TODO 기록 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpiritHistoryService {

    private final SpiritRepository spiritRepository;
    private final SpiritTemperamentTagRepository spiritTemperamentTagRepository;
    private final SpiritDailyLogRepository spiritDailyLogRepository;
    private final SpiritLetterRepository spiritLetterRepository;
    private final SpiritDiscoveryRepository spiritDiscoveryRepository;
    private final TaskRepository taskRepository;

    public List<SpiritHistoryResponse> getHistory(Long userId) {
        return spiritRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(spirit -> SpiritHistoryResponse.of(spirit, temperamentsOf(spirit.getId())))
                .toList();
    }

    public List<SpiritDailyLogResponse> getDailyLogs(Long userId, Long spiritId) {
        Spirit spirit = findOwnedSpirit(userId, spiritId);
        return spiritDailyLogRepository.findAllBySpiritIdOrderByLogDateDesc(spirit.getId()).stream()
                .map(SpiritDailyLogResponse::from)
                .toList();
    }

    public List<SpiritLetterResponse> getLetters(Long userId) {
        return spiritLetterRepository.findAllByUserIdOrderBySentAtDesc(userId).stream()
                .map(SpiritLetterResponse::from)
                .toList();
    }

    @Transactional
    public void markLetterRead(Long userId, Long letterId) {
        SpiritLetter letter = spiritLetterRepository.findById(letterId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Letter not found"));
        if (!letter.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this letter");
        }
        letter.markRead();
    }

    public List<SpiritDiscoveryResponse> getDiscoveries(Long userId) {
        return spiritDiscoveryRepository.findAllByUserIdOrderByDiscoveredAtDesc(userId).stream()
                .map(SpiritDiscoveryResponse::from)
                .toList();
    }

    public SpiritTodoSummaryResponse getTodoSummary(Long userId, Long spiritId) {
        Spirit spirit = findOwnedSpirit(userId, spiritId);

        LocalDate from = spirit.getCreatedAt().toLocalDate();
        LocalDate to = spirit.getStatus() == SpiritStatus.GROWING
                ? LocalDate.now()
                : (spirit.getDepartedAt() != null ? spirit.getDepartedAt().toLocalDate() : LocalDate.now());

        List<Task> tasks = taskRepository.findByUserIdAndDateRange(userId, from, to);
        return SpiritTodoSummaryResponse.of(tasks);
    }

    private Spirit findOwnedSpirit(Long userId, Long spiritId) {
        Spirit spirit = spiritRepository.findById(spiritId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Spirit not found"));
        if (!spirit.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this spirit");
        }
        return spirit;
    }

    private List<SpiritTemperament> temperamentsOf(Long spiritId) {
        return spiritTemperamentTagRepository.findAllBySpiritId(spiritId).stream()
                .map(SpiritTemperamentTag::getTemperament)
                .toList();
    }
}