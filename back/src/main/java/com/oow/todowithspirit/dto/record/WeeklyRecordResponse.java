package com.oow.todowithspirit.dto.record;

import com.oow.todowithspirit.domain.task.CategoryType;
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
    private String titleMessage;
    private String subtitleMessage;

    // 1. 주간 요일별 막대 차트 데이터 (일~토 7일)
    private List<DailyBarChartItem> dailyCharts;

    // 2. 주간 요약 지표
    private int completedTaskCount;    // 이번주 달성 플랜
    private int totalTaskCount;        // 전체 플랜
    private double averageCompletionRate; // 평균 달성률 (예: 100.0)

    // 3. 주간 기록 (1일차~7일차 아이콘 상태)
    private List<DailyStatusItem> weeklyStatuses;

    // 4. 주간 분석 (Schedule, 루틴, 미루기 비율)
    private TypeRatioAnalysis typeAnalysis;

    // 5. 주간 실천 Top 3
    private List<CategoryStatItem> topCategories;

    // 6. 자주 놓친 분야
    private CategoryStatItem mostMissedCategory;

    // 7. 하단 피드백 카드
    private FeedbackMessage feedback;

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
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyStatusItem {
        private int dayIndex;           // 1~7일차
        private String status;          // "SUCCESS", "FAILED", "EMPTY"
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TypeRatioAnalysis {
        private double scheduleRatio;   // 스케쥴 비율 (%)
        private double routineRatio;    // 루틴 비율 (%)
        private double delayedRatio;    // 미루기 비율 (%)
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryStatItem {
        private int rank;               // 1, 2, 3 (미루기는 null 가능)
        private CategoryType categoryType;
        private String categoryLabel;   // "학업/커리어", "인간관계/약속", "취미", "건강"
        private int count;              // 15회
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackMessage {
        private String mainMessage;
        private String subMessage;
    }
}