package com.example.data.response

data class DailyRecordResponse(
    val date: String,
    val completionRate: Double,
    val completedCount: Int,
    val totalCount: Int,
    val earnedGrowthPower: Int,
    val typeBreakdown: Map<String, RecordTypeProgressResponse> = emptyMap(),
    val items: List<RecordTaskItemResponse> = emptyList(),
    val todayRewards: List<RecordRewardResponse> = emptyList()
)

data class RecordTypeProgressResponse(
    val completed: Int,
    val total: Int
)

data class RecordTaskItemResponse(
    val taskId: Long,
    val title: String,
    val taskType: String,
    val growthType: String?,
    val growthValue: Int,
    val interpretation: String,
    val completed: Boolean
)

data class RecordRewardResponse(
    val missionType: String,
    val title: String,
    val rewardExp: Int,
    val iconType: String,
    val achieved: Boolean
)
