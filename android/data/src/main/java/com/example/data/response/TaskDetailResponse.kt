package com.example.data.response

data class TaskDetailResponse(
    val category: String,
    val completedAt: String?,
    val createdAt: String,
    val endDate: String?,
    val endTime: String?,
    val growthType: String?,
    val growthValue: Int,
    val isAllDay: Boolean,
    val isCompleted: Boolean,
    val isImportant: Boolean,
    val isPublic: Boolean,
    val memo: String?,
    val notificationMinutes: Int?,
    val repeatDaysOfWeek: List<String> = emptyList(),
    val repeatDaysOfMonth: List<Int> = emptyList(),
    val repeatEndDate: String?,
    val repeatType: String?,
    val startDate: String,
    val startTime: String?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: String
)