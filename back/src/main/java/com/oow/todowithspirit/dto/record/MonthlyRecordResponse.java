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
public class MonthlyRecordResponse {

    private int year;
    private int month;
    private String message;

    // 1. 요약 지표
    private int completedTaskCount;
    private int totalTaskCount;
    private double averageCompletionRate; // 평균 달성률

    // 2. 월간 캘린더 히트맵 데이터 (1일 - 말일)
    private List<DailyHeatmapItem> dailyHeatmaps;

    // 3. 월간 비교 차트 (1월 - 12월 월별 달성 수치)
    private List<MonthlyComparisonItem> monthlyComparisons;

    // 4. 월간 분석
    private String title;
    private String content;

    // --- Sub DTOs ---

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyHeatmapItem {
        private LocalDate date;
        private int scheduleTotalCount;
        private int scheduleCompletedCount;
        private int routineTotalCount;
        private int routineCompletedCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyComparisonItem {
        private int month;
        private double completedRate; // 월별 달성률
    }
}
