package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.task.*;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.record.DailyRecordResponse;
import com.oow.todowithspirit.dto.record.WeeklyRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
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

    @Transactional(readOnly = true)
    public WeeklyRecordResponse getWeeklyRecord(Long userId, LocalDate date) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid userId");
        }

        // 1. 해당 날짜가 속한 주의 일요일 ~ 토요일 범위 계산
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // 2. 주차 정보 계산 (예: "6월 1주 리포트")
        int month = date.getMonthValue();
        int weekOfMonth = date.get(WeekFields.of(Locale.KOREA).weekOfMonth());

        // 3. 해당 주간의 전체 Task 및 완료된 루틴 ID 조회
        List<Task> weeklyTasks = taskRepository.findByUserIdAndDateRange(userId, startOfWeek, endOfWeek);
        Set<Long> completedRoutineIds = routineCompletionRepository
                .findCompletedTaskIdsByUserIdAndDateRange(userId, startOfWeek, endOfWeek);

        // 4. 일별 차트 및 주간 기록 아이콘 계산
        List<WeeklyRecordResponse.DailyBarChartItem> dailyCharts = new ArrayList<>();

        int totalPlanCount = 0;
        int completedPlanCount = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate current = startOfWeek.plusDays(i);
            boolean isFuture = current.isAfter(LocalDate.now());

            // 해당 날짜 Task 필터링
            List<Task> dayTasks = weeklyTasks.stream()
                    .filter(t -> t.getEndDate() != null && t.getEndDate().equals(current))
                    .toList();

            int scheduleTotal = (int) dayTasks.stream().filter(t -> t.getTaskType() != TaskType.ROUTINE).count();
            int scheduleCompleted = (int) dayTasks.stream()
                    .filter(t -> t.getTaskType() != TaskType.ROUTINE && t.isCompleted())
                    .count();

            int routineTotal = (int) dayTasks.stream().filter(t -> t.getTaskType() == TaskType.ROUTINE).count();
            int routineCompleted = (int) dayTasks.stream()
                    .filter(t -> t.getTaskType() == TaskType.ROUTINE && completedRoutineIds.contains(t.getId()))
                    .count();

            int dayTotal = scheduleTotal + routineTotal;
            int dayCompleted = scheduleCompleted + routineCompleted;

            totalPlanCount += dayTotal;
            completedPlanCount += dayCompleted;

            // 성장력/경험치 계산
            int growthPower = isFuture ? 0 : dayTasks.stream()
                    .filter(task -> isTaskCompleted(task, completedRoutineIds))
                    .mapToInt(Task::getGrowthValue)
                    .sum();

            // 주간 기록 아이콘 상태 (SUCCESS / FAILED / EMPTY)
            String icon = "EMPTY";
            if (!isFuture && dayTotal > 0) {
                icon = (dayCompleted == dayTotal) ? "SUCCESS" : "FAILED";
            }

            dailyCharts.add(WeeklyRecordResponse.DailyBarChartItem.builder()
                    .date(current)
                    .dayOfWeek(current.getDayOfWeek().name().substring(0, 3))
                    .growthPower(growthPower)
                    .scheduleCompleted(scheduleCompleted)
                    .scheduleTotal(scheduleTotal)
                    .routineCompleted(routineCompleted)
                    .routineTotal(routineTotal)
                    .icon(icon)
                    .build());
        }

        double averageCompletionRate = totalPlanCount == 0 ? 0.0 :
                Math.round(((double) completedPlanCount / totalPlanCount) * 1000) / 10.0;

        // todo: insert logic
        List<WeeklyRecordResponse.AnalysisItem> analysises = new ArrayList<>();
        analysises.add(WeeklyRecordResponse.AnalysisItem.builder()
                .analysisTitle("가장 빈도 높은 플랜")
                .taskTitle("영어 단어 20개 외우기")
                .completedCount(7)
                .targetCount(7)
                .build());
        analysises.add(WeeklyRecordResponse.AnalysisItem.builder()
                .analysisTitle("가장 잘 지킨 플랜")
                .taskTitle("물 2L 마시기")
                .completedCount(6)
                .targetCount(7)
                .build());
        analysises.add(WeeklyRecordResponse.AnalysisItem.builder()
                .analysisTitle("가장 많이 미룬 플")
                .taskTitle("운동 계획 짜")
                .completedCount(6)
                .targetCount(6)
                .build());

        // todo: insert logic
        List<WeeklyRecordResponse.AchievementItem> achievements = new ArrayList<>();
        achievements.add(WeeklyRecordResponse.AchievementItem.builder()
                .code("testCode")
                .title("테스트 업적")
                .description("테스트 업적 설명")
                .icon("TEST_ICON")
                .targetCount(5)
                .build());

        return WeeklyRecordResponse.builder()
                .week(date.get(WeekFields.of(Locale.KOREA).weekOfMonth()))
                .message("일정과 중요한 일들의 비중이 높았던 정신없는 한 주 였네요! 그래도 미뤘던 플랜들은 다시 한 번 도전 해보면 어떨까요?")
                .dailyCharts(dailyCharts)
                .completedTaskCount(completedPlanCount)
                .delayedCount(0) // todo: insert logic
                .totalTaskCount(totalPlanCount)
                .averageCompletionRate(averageCompletionRate)
                .analysises(analysises)
                .achievements(achievements)
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