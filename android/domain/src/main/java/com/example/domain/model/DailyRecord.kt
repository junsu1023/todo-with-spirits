package com.example.domain.model

import java.time.LocalDate

data class DailyRecord(
    val date: LocalDate,
    val completionRate: Double,
    val completedCount: Int,
    val totalCount: Int,
    val earnedGrowthPower: Int,
    val typeBreakdown: Map<String, RecordTypeProgress>,
    val items: List<RecordTaskItem>,
    val todayRewards: List<RecordReward>
)

data class RecordTypeProgress(
    val completed: Int,
    val total: Int
)

data class RecordTaskItem(
    val taskId: Long,
    val title: String,
    val taskType: String,
    val growthType: String?,
    val growthValue: Int,
    val interpretation: String,
    val completed: Boolean
)

data class RecordReward(
    val missionType: String,
    val title: String,
    val rewardExp: Int,
    val iconType: String,
    val achieved: Boolean
)
