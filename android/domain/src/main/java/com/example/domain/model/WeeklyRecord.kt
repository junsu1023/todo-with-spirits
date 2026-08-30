package com.example.domain.model

import java.time.LocalDate

data class WeeklyRecord(
    val week: Int,
    val message: String,
    val dailyCharts: List<WeeklyDailyChart>,
    val completedTaskCount: Int,
    val delayedCount: Int,
    val totalTaskCount: Int,
    val averageCompletionRate: Double,
    val typeAnalysis: WeeklyTypeAnalysis,
    val analyses: List<WeeklyPlanAnalysis>,
    val achievements: List<WeeklyAchievement>
)

data class WeeklyDailyChart(
    val date: LocalDate,
    val dayOfWeek: String,
    val dayNumber: Int?,
    val growthPower: Int?,
    val scheduleCompleted: Int,
    val scheduleTotal: Int,
    val routineCompleted: Int,
    val routineTotal: Int,
    val icon: String
)

data class WeeklyTypeAnalysis(
    val scheduleRatio: Double,
    val routineRatio: Double,
    val delayedRatio: Double
)

data class WeeklyPlanAnalysis(
    val analysisTitle: String,
    val taskTitle: String,
    val completedCount: Int,
    val targetCount: Int
)

data class WeeklyAchievement(
    val code: String,
    val title: String,
    val description: String,
    val icon: String,
    val targetCount: Int
)
