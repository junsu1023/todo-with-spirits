package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.task.*;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.record.DailyRecordResponse;
import com.oow.todowithspirit.dto.record.MonthlyRecordResponse;
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

    private final TaskService taskService;

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

        // 2. 통합 조회
        List<Task> allTasks = taskRepository.findCalendarTasksWithDateRange(userId, startOfWeek, endOfWeek);
        List<Task> schedules = allTasks.stream()
                .filter(t -> t.getTaskType() == TaskType.SCHEDULE)
                .toList();
        List<Task> routines = allTasks.stream()
                .filter(t -> t.getTaskType() == TaskType.ROUTINE)
                .toList();

        Map<Long, Map<LocalDate, RoutineCompletion>> completionMap = Collections.emptyMap();
        if (!routines.isEmpty()) {
            List<Long> routineIds = routines.stream().map(Task::getId).toList();
            completionMap = taskService.loadCompletionMap(routineIds, startOfWeek, endOfWeek);
        }

        // 3. 일별 차트 및 주간 기록 아이콘 계산
        List<WeeklyRecordResponse.DailyBarChartItem> dailyCharts = new ArrayList<>();
        Map<CategoryType, CategoryCounter> categoryMap = new HashMap<>();

        int totalPlanCount = 0;
        int completedPlanCount = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate current = startOfWeek.plusDays(i);
            boolean isFuture = current.isAfter(LocalDate.now());

            // 당일 일정(Schedule) 필터링 (시작일/종료일 기준)
            List<Task> daySchedules = schedules.stream()
                    .filter(s -> isScheduleOnDate(s, current))
                    .toList();

            int scheduleTotal = daySchedules.size();
            int scheduleCompleted = (int) daySchedules.stream().filter(Task::isCompleted).count();

            // 일정 카테고리 집계
            for (Task schedule : daySchedules) {
                CategoryType category = (schedule.getCategory() != null) ? schedule.getCategory() : CategoryType.NONE;
                categoryMap.computeIfAbsent(category, CategoryCounter::new).add(schedule.isCompleted());
            }

            // B. 당일 루틴(Routine) 전개 및 필터링
            int routineTotal = 0;
            int routineCompleted = 0;

            for (Task routine : routines) {
                // 해당 날짜에 루틴 수행일인지 확인 (expandOccurrences 결과 검증)
                if (taskService.expandOccurrences(routine, current, current).contains(current)) {
                    routineTotal++;

                    // 완료 여부 체크
                    boolean isCompleted = completionMap
                            .getOrDefault(routine.getId(), Collections.emptyMap())
                            .containsKey(current);

                    if (isCompleted) {
                        routineCompleted++;
                    }

                    // 루틴 카테고리 집계 (전개된 Occurrence 단위로 카운트)
                    CategoryType category = (routine.getCategory() != null) ? routine.getCategory() : CategoryType.NONE;
                    categoryMap.computeIfAbsent(category, CategoryCounter::new).add(isCompleted);
                }
            }

            int dayTotal = scheduleTotal + routineTotal;
            int dayCompleted = scheduleCompleted + routineCompleted;

            totalPlanCount += dayTotal;
            completedPlanCount += dayCompleted;

            // 성장력/경험치 계산
            int growthPower = 0;
            if (!isFuture) {
                int scheduleGrowth = daySchedules.stream().filter(Task::isCompleted).mapToInt(Task::getGrowthValue).sum();
                int routineGrowth = 0;
                for (Task routine : routines) {
                    if (taskService.expandOccurrences(routine, current, current).contains(current) &&
                            completionMap.getOrDefault(routine.getId(), Collections.emptyMap()).containsKey(current)) {
                        routineGrowth += routine.getGrowthValue();
                    }
                }
                growthPower = scheduleGrowth + routineGrowth;
            }

            // 아이콘 상태 (SUCCESS / FAILED / EMPTY)
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

        // 평균 달성률
        double averageCompletionRate = totalPlanCount == 0 ? 0.0 :
                Math.round(((double) completedPlanCount / totalPlanCount) * 1000) / 10.0;

        // 4. 카테고리별 분석
        List<WeeklyRecordResponse.CategoryStatItem> topCategories = categoryMap.values().stream()
                .filter(c -> c.completedCount > 0)
                .sorted((a, b) -> Integer.compare(b.completedCount, a.completedCount))
                .limit(3)
                .map(c -> WeeklyRecordResponse.CategoryStatItem.builder()
                        .category(c.category)
                        .completedCount(c.completedCount)
                        .totalCount(c.totalCount)
                        .build())
                .toList();

        WeeklyRecordResponse.CategoryStatItem bottomCategory = categoryMap.values().stream()
                .filter(c -> c.getMissedCount() > 0)
                .max(Comparator.comparingInt(CategoryCounter::getMissedCount))
                .map(c -> WeeklyRecordResponse.CategoryStatItem.builder()
                        .category(c.category)
                        .completedCount(c.completedCount)
                        .totalCount(c.totalCount)
                        .build())
                .orElse(null);

        return WeeklyRecordResponse.builder()
                .week(date.get(WeekFields.of(Locale.KOREA).weekOfMonth()))
                .message("일정과 중요한 일들의 비중이 높았던 정신없는 한 주 였네요! 그래도 미뤘던 플랜들은 다시 한 번 도전 해보면 어떨까요?")
                .dailyCharts(dailyCharts)
                .completedTaskCount(completedPlanCount)
                .delayedCount(0) // todo: insert logic
                .totalTaskCount(totalPlanCount)
                .averageCompletionRate(averageCompletionRate)
                .topCategories(topCategories)
                .bottomCategory(bottomCategory)
                .build();
    }

    @Transactional(readOnly = true)
    public MonthlyRecordResponse getMonthlyRecord(Long userId, LocalDate date) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid userId");
        }

        int year = date.getYear();
        int month = date.getMonthValue();

        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        // 1. 해당 월 전체 Task 및 루틴 조회
        List<Task> allTasks = taskRepository.findCalendarTasksWithDateRange(userId, startOfMonth, endOfMonth);

        List<Task> schedules = allTasks.stream()
                .filter(t -> t.getTaskType() == TaskType.SCHEDULE)
                .toList();

        List<Task> routines = allTasks.stream()
                .filter(t -> t.getTaskType() == TaskType.ROUTINE)
                .toList();

        // 2. 루틴 완료 데이터 조회
        Map<Long, Map<LocalDate, RoutineCompletion>> completionMap = Collections.emptyMap();
        if (!routines.isEmpty()) {
            List<Long> routineIds = routines.stream().map(Task::getId).toList();
            completionMap = taskService.loadCompletionMap(routineIds, startOfMonth, endOfMonth);
        }

        // 3. 성능 최적화: 루틴별 한 달 전체 Occurrence 일자를 미리 전개
        Map<Long, Set<LocalDate>> routineOccurrenceMap = new HashMap<>();
        for (Task routine : routines) {
            List<LocalDate> dates = taskService.expandOccurrences(routine, startOfMonth, endOfMonth);
            routineOccurrenceMap.put(routine.getId(), new HashSet<>(dates));
        }

        // 4. 일별 순회 데이터 집계
        List<MonthlyRecordResponse.DailyHeatmapItem> dailyHeatmaps = new ArrayList<>();
        Map<CategoryType, CategoryCounter> categoryMap = new HashMap<>();
        List<Double> dailyRates = new ArrayList<>();

        int totalMonthCompleted = 0;
        int totalMonthPlan = 0;
        int daysInMonth = date.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);

            // A. 당일 일정(Schedule) 필터링
            List<Task> daySchedules = schedules.stream()
                    .filter(s -> isScheduleOnDate(s, current))
                    .toList();

            int scheduleTotal = daySchedules.size();
            int scheduleCompleted = (int) daySchedules.stream().filter(Task::isCompleted).count();

            for (Task schedule : daySchedules) {
                CategoryType category = (schedule.getCategory() != null) ? schedule.getCategory() : CategoryType.NONE;
                categoryMap.computeIfAbsent(category, CategoryCounter::new).add(schedule.isCompleted());
            }

            // B. 당일 루틴(Routine) 필터링 (미리 전개해둔 Map 활용)
            int routineTotal = 0;
            int routineCompleted = 0;

            for (Task routine : routines) {
                Set<LocalDate> occurrenceDates = routineOccurrenceMap.getOrDefault(routine.getId(), Collections.emptySet());

                if (occurrenceDates.contains(current)) {
                    routineTotal++;

                    boolean isDone = completionMap
                            .getOrDefault(routine.getId(), Collections.emptyMap())
                            .containsKey(current);

                    if (isDone) {
                        routineCompleted++;
                    }

                    CategoryType category = (routine.getCategory() != null) ? routine.getCategory() : CategoryType.NONE;
                    categoryMap.computeIfAbsent(category, CategoryCounter::new).add(isDone);
                }
            }

            int dayTotal = scheduleTotal + routineTotal;
            int dayCompleted = scheduleCompleted + routineCompleted;

            totalMonthPlan += dayTotal;
            totalMonthCompleted += dayCompleted;

            // 평균 달성률 계산을 위한 일별 달성률 수집 (계획이 있는 날만)
            if (dayTotal > 0) {
                dailyRates.add((dayCompleted * 100.0) / dayTotal);
            }

            // 일별 히트맵 추가
            dailyHeatmaps.add(MonthlyRecordResponse.DailyHeatmapItem.builder()
                    .date(current)
                    .scheduleTotalCount(scheduleTotal)
                    .scheduleCompletedCount(scheduleCompleted)
                    .routineTotalCount(routineTotal)
                    .routineCompletedCount(routineCompleted)
                    .build());
        }

        // 5. 월간 평균 달성률 계산 (소수점 첫째 자리 반올림)
        double averageCompletionRate = dailyRates.isEmpty() ? 0.0 :
                Math.round(dailyRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) * 10.0) / 10.0;

        // 6. 카테고리 분석 (Top 3 / Bottom 1)
        List<MonthlyRecordResponse.CategoryStatItem> topCategories = categoryMap.values().stream()
                .filter(c -> c.completedCount > 0)
                .sorted((a, b) -> Integer.compare(b.completedCount, a.completedCount))
                .limit(3)
                .map(c -> MonthlyRecordResponse.CategoryStatItem.builder()
                        .category(c.category)
                        .completedCount(c.completedCount)
                        .totalCount(c.totalCount)
                        .build())
                .toList();

        MonthlyRecordResponse.CategoryStatItem bottomCategory = categoryMap.values().stream()
                .filter(c -> c.getMissedCount() > 0)
                .max(Comparator.comparingInt(CategoryCounter::getMissedCount))
                .map(c -> MonthlyRecordResponse.CategoryStatItem.builder()
                        .category(c.category)
                        .completedCount(c.completedCount)
                        .totalCount(c.totalCount)
                        .build())
                .orElse(null);

        // 7. 월간 분석 지표 계산 (주력 분야명, 백분율, 주력 분야 달성률)
        String mainCategory = topCategories.isEmpty() ? null : topCategories.get(0).getCategory().name();

        Double mainCategoryCompletionRate = 0.0;
        if (!topCategories.isEmpty() && topCategories.get(0).getTotalCount() > 0) {
            MonthlyRecordResponse.CategoryStatItem top1 = topCategories.get(0);
            mainCategoryCompletionRate = Math.round((top1.getCompletedCount() * 100.0 / top1.getTotalCount()) * 10.0) / 10.0;
        }

        Integer mainCategoryPeerPercentile = calculatePeerPercentile(userId, mainCategoryCompletionRate);

        // 8. DTO 조립 및 반환
        return MonthlyRecordResponse.builder()
                .year(year)
                .month(month)
                .message("이번 달도 바쁘겠지만 할 수 있어요!")
                .completedTaskCount(totalMonthCompleted)
                .totalTaskCount(totalMonthPlan)
                .averageCompletionRate(averageCompletionRate)
                .dailyHeatmaps(dailyHeatmaps)
                .monthlyComparisons(getYearlyComparisonData(userId, year))
                .mainCategory(mainCategory)
                .mainCategoryPeerPercentile(mainCategoryPeerPercentile)
                .mainCategoryCompletionRate(mainCategoryCompletionRate)
                .title("기분 좋은 몰입의 흔적 🌊")
                .content("커리어 레벨업부터 취미 리프레시까지! 계획했던 핵심 루틴들을 뚝심 있게 지켜냈어요.")
                .topCategories(topCategories)
                .bottomCategory(bottomCategory)
                .build();
    }

    // 1월~12월 월별 비교 차트 데이터를 구하는 헬퍼 메서드
    private List<MonthlyRecordResponse.MonthlyComparisonItem> getYearlyComparisonData(Long userId, int year) {
        // DB에서 연간 데이터를 월별로 Group By하여 가져오거나, 필요 시 간략 집계 쿼리로 구성
        List<MonthlyRecordResponse.MonthlyComparisonItem> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(MonthlyRecordResponse.MonthlyComparisonItem.builder()
                    .month(m)
                    .completedRate(0.0) // 실제 쿼리 결과 맵핑
                    .build());
        }
        return result;
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

    private static class CategoryCounter {
        CategoryType category;
        int completedCount = 0;
        int totalCount = 0;

        CategoryCounter(CategoryType category) {
            this.category = category;
        }

        void add(boolean isCompleted) {
            this.totalCount++;
            if (isCompleted) {
                this.completedCount++;
            }
        }

        int getMissedCount() {
            return totalCount - completedCount;
        }
    }

    // 해당 날짜에 일정이 존재하는지 검증
    private boolean isScheduleOnDate(Task schedule, LocalDate targetDate) {
        if (schedule.getEndDate() != null) {
            return schedule.getEndDate().equals(targetDate);
        }
        return schedule.getStartDate() != null && schedule.getStartDate().equals(targetDate);
    }

    private Integer calculatePeerPercentile(Long userId, double completionRate) {
        // 또래 백분위 계산 산출 로직 (기본값 설정)
        return 4;
    }
}