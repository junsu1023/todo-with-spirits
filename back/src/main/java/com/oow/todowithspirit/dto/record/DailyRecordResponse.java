package com.oow.todowithspirit.dto.record;

import com.oow.todowithspirit.domain.task.MissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DailyRecordResponse {
    private final LocalDate date;
    private final double completionRate;
    private final int completedCount;
    private final int totalCount;
    private final int earnedGrowthPower;
    private final Map<String, TypeCount> typeBreakdown;
    private final List<RecordItem> items;
    private List<DailyRewardItem> todayRewards;
//    private final SpiritGrowthSummary spiritGrowthSummary;

    @Getter
    @AllArgsConstructor
    public static class TypeCount {
        private final int completed;
        private final int total;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecordItem {
        private final Long taskId;
        private final String title;
        private final String taskType;
        private final boolean isCompleted;
        private final String growthType;
        private final int growthValue;
        private final String interpretation;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRewardItem {
        private MissionType missionType; // "일일 미션", "끈기 스코어", "히든 미션"
        private String title;       // "오늘 플랜 5개 이상 완료"
        private int rewardExp;      // 20, 100
        private String iconType;    // "THUMB_UP", "FLAME", "DIAMOND" // todo: front와 협의
        private boolean isAchieved; // 달성 여부
    }

//    @Getter
//    @AllArgsConstructor
//    public static class SpiritGrowthSummary {
//        private final Long spiritId;
//        private final String spiritName;
//        private final int expGained;
//    }
}