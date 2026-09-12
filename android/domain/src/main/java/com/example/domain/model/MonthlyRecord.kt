package com.example.domain.model

import java.time.LocalDate

data class MonthlyRecord(
    val year: Int,
    val month: Int,
    val message: String,
    val completedTaskCount: Int,
    val totalTaskCount: Int,
    val averageCompletionRate: Double,
    val dailyHeatmaps: List<MonthlyDailyHeatmap>,
    val monthlyComparisons: List<MonthlyComparison>,
    val mainCategory: String?,
    val mainCategoryPeerPercentile: Int,
    val mainCategoryCompletionRate: Double,
    val title: String,
    val content: String,
    val topCategories: List<MonthlyCategoryCount>,
    val bottomCategory: MonthlyCategoryCount
)

data class MonthlyDailyHeatmap(
    val date: LocalDate,
    val scheduleTotalCount: Int,
    val scheduleCompletedCount: Int,
    val routineTotalCount: Int,
    val routineCompletedCount: Int
)

data class MonthlyComparison(
    val month: Int,
    val completedRate: Double
)

data class MonthlyCategoryCount(
    val category: String,
    val completedCount: Int,
    val totalCount: Int
)
