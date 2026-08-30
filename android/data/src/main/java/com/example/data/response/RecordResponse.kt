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

data class WeeklyRecordResponse(
    val week: Int,
    val message: String,
    val dailyCharts: List<WeeklyDailyChartResponse>? = null,
    val completedTaskCount: Int,
    val delayedCount: Int,
    val totalTaskCount: Int,
    val averageCompletionRate: Double,
    val typeAnalysis: WeeklyTypeAnalysisResponse? = null,
    val analyses: List<WeeklyPlanAnalysisResponse>? = null,
    val achievements: List<WeeklyAchievementResponse>? = null
)

data class WeeklyDailyChartResponse(
    val date: String,
    val dayOfWeek: String,
    val dayNumber: Int?,
    val growthPower: Int?,
    val scheduleCompleted: Int,
    val scheduleTotal: Int,
    val routineCompleted: Int,
    val routineTotal: Int,
    val icon: String
)

data class WeeklyTypeAnalysisResponse(
    val scheduleRatio: Double,
    val routineRatio: Double,
    val delayedRatio: Double
)

data class WeeklyPlanAnalysisResponse(
    val analysisTitle: String,
    val taskTitle: String,
    val completedCount: Int,
    val targetCount: Int
)

data class WeeklyAchievementResponse(
    val code: String,
    val title: String,
    val description: String,
    val icon: String,
    val targetCount: Int
)
