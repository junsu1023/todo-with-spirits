package com.example.data.response

data class RoutineDetailResponse(
    val category: String,
    val completedAt: String?,
    val createdAt: String,
    val excludeHoliday: Boolean,
    val growthType: String?,
    val growthValue: Int,
    val isCompleted: Boolean,
    val isPublic: Boolean,
    val memo: String?,
    val notificationAt: String?,
    val notificationMinutes: Int?,
    val repeatDaysOfMonth: List<Int>,
    val repeatDaysOfWeek: List<String>,
    val repeatEndDate: String?,
    val repeatType: String,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: String
)
