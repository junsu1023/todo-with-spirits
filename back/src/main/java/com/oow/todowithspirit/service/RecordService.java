package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.task.*;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.record.DailyRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SpiritRepository spiritRepository;
    private final RoutineCompletionRepository routineCompletionRepository;

    @Transactional(readOnly = true)
    public DailyRecordResponse getDailyRecord(Long userId, LocalDate date) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid userId");
        }

        // 1. 유저 및 대표 정령 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        // 대표 정령 ID Null 체크
        if (user.getRepresentativeSpiritId() == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Representative spirit not found");
        }

//        Spirit spirit = spiritRepository.findById(user.getRepresentativeSpiritId())
//                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Representative spirit not found"));

        // 2. 해당 유저의 특정 날짜(오늘) Task 목록 조회
        List<Task> tasks = taskRepository.findDailyTasks(userId, date);

        // 해당 날짜에 완료된 유저의 루틴 ID 목록 조회
//        Set<Long> completedRoutineIds = routineCompletionRepository
//                .findAllByUserIdAndCompletionDate(userId, date)
//                .stream()
//                .map(rc -> rc.getTask().getId())
//                .collect(Collectors.toSet());
        Set<Long> completedRoutineIds = routineCompletionRepository
                .findCompletedTaskIdsByUserIdAndDate(userId, date);

        int totalCount = tasks.size();
        int completedCount = (int) tasks.stream().filter(task -> isTaskCompleted(task, completedRoutineIds)).count();
        double completionRate = totalCount == 0 ? 0.0 : Math.round(((double) completedCount / totalCount) * 1000) / 10.0;

        // 3. 작업 유형별 통계 분류
        Map<String, DailyRecordResponse.TypeCount> typeBreakdown = new HashMap<>();
        for (TaskType type : TaskType.values()) {
            List<Task> filtered = tasks.stream().filter(t -> t.getTaskType() == type).toList();
            int total = filtered.size();
            int completed = (int) filtered.stream().filter(task -> isTaskCompleted(task, completedRoutineIds)).count();
            typeBreakdown.put(type.name(), new DailyRecordResponse.TypeCount(completed, total));
        }

        // 4. 개별 아이템 상세 매핑 및 가이드라인 문구 생성
        List<DailyRecordResponse.RecordItem> items = tasks.stream().map(task -> {
            String krGrowthType = getKoreanGrowthType(task.getGrowthType().name());
            boolean completed = isTaskCompleted(task, completedRoutineIds);

            String interpretation = completed
                    ? "꾸준함이 정령의 " + krGrowthType + "으(로) 전달됐어요."
                    : "완료하면 정령의 " + krGrowthType + "이(가) 성장해요.";

            return DailyRecordResponse.RecordItem.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .taskType(task.getTaskType().name())
                    .isCompleted(completed)
                    .growthType(task.getGrowthType().name())
                    .growthValue(task.getGrowthValue())
                    .interpretation(interpretation)
                    .build();
        }).toList();

        // 오늘 획득한 총 성장력 계산 (완료된 태스크의 성장력 합산)
        int earnedGrowthPower = tasks.stream()
                .filter(task -> isTaskCompleted(task, completedRoutineIds))
                .mapToInt(Task::getGrowthValue)
                .sum();

        List<DailyRecordResponse.DailyRewardItem> todayRewards = getTodayRewards(completedCount);

        return DailyRecordResponse.builder()
                .date(date)
                .completionRate(completionRate)
                .completedCount(completedCount)
                .totalCount(totalCount)
                .earnedGrowthPower(earnedGrowthPower)
                .typeBreakdown(typeBreakdown)
                .items(items)
                .todayRewards(todayRewards)
//                .spiritGrowthSummary(new DailyRecordResponse.SpiritGrowthSummary(spirit.getId(), spirit.getSpiritName(), earnedGrowthPower))
                .build();
    }

    // todo: 성과 기준 생성
    private List<DailyRecordResponse.DailyRewardItem> getTodayRewards(int completedCount) {
        List<DailyRecordResponse.DailyRewardItem> todayRewards = new ArrayList<>();

        // (1) 오늘 플랜 5개 이상 완료
        boolean isPlan5Completed = completedCount >= 5;
        todayRewards.add(DailyRecordResponse.DailyRewardItem.builder()
                .missionType(MissionType.DAILY)
                .title("오늘 플랜 5개 이상 완료")
                .rewardExp(20)
                .iconType("THUMB_UP")
                .isAchieved(isPlan5Completed)
                .build());

        // (2) 끈기 스코어 (미뤘던 목표 완료 등)
        todayRewards.add(DailyRecordResponse.DailyRewardItem.builder()
                .missionType(MissionType.CONSISTENCY)
                .title("미뤘던 목표 2개 완료")
                .rewardExp(20)
                .iconType("FLAME")
                .isAchieved(false) // 해당되는 로직 조건 연결
                .build());

        // (3) 히든 미션
        todayRewards.add(DailyRecordResponse.DailyRewardItem.builder()
                .missionType(MissionType.HIDDEN)
                .title("정령의 호감도 +10 쌓기")
                .rewardExp(100)
                .iconType("DIAMOND")
                .isAchieved(false)
                .build());

        return todayRewards;
    }

    private String getKoreanGrowthType(String growthType) {
        return switch (growthType) {
            case "ENERGY" -> "활력";
            case "CREATIVITY" -> "창의력";
            case "FOCUS" -> "집중력";
            case "CONSISTENCY" -> "끈기";
            default -> "능력";
        };
    }

    private boolean isTaskCompleted(Task task, Set<Long> completedRoutineIds) {
        if (task.getTaskType() == TaskType.ROUTINE) {
            return completedRoutineIds.contains(task.getId());
        }
        return task.isCompleted();
    }
}