package com.oow.todowithspirit.dto.record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyRecordResponse {

    private int week;
    private String message;

    // 1. 주간 요일별 막대 차트 데이터 (일~토 7일)
    private List<DailyBarChartItem> dailyCharts;

    // 2. 주간 요약 지표
    private int completedTaskCount;    // 이번주 달성 플랜
    private int delayedCount;           // 이번 주 미룬 플랜
    private int totalTaskCount;        // 이번 주 전체 플랜
    private double averageCompletionRate; // 평균 달성률

    // 3. 분석
    private List<AnalysisItem> analysises;

    // 4. 미션
    private List<AchievementItem> achievements;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyBarChartItem {
        private LocalDate date;
        private String dayOfWeek;       // "SUN", "MON", ...
        private int growthPower;        // 상단 수치 (99, 0 등, 미래 일자는 0)
        private int scheduleCompleted;
        private int scheduleTotal;
        private int routineCompleted;
        private int routineTotal;
        private String icon; // "SUCCESS", "FAILED", "EMPTY"
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisItem {
        private String analysisTitle; // 분석 명
        private String taskTitle; // Task 명
        private int completedCount;
        private int targetCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementItem {
        private String code; // 업저코드
        private String title; // 업적명
        private String description; // 업적 설명
        private String icon; // 아이콘 명
        private int targetCount;
    }
}