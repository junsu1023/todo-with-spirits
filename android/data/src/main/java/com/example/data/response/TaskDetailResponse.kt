package com.example.data.response

// 일정/루틴 단건 조회 · 생성 · 수정 응답 공용.
// SCHEDULE 전용(isAllDay, endDate, endTime)과 ROUTINE 전용(excludeHoliday, repeat*) 필드는
// 다른 타입에서는 내려오지 않으므로 nullable로 받는다.
data class TaskDetailResponse(
    val completedAt: String?,
    val createdAt: String?,
    val endDate: String?,
    val endTime: String?,
    val excludeHoliday: Boolean?,
    val growthType: String?,
    val growthValue: Int?,
    val isAllDay: Boolean?,
    val isCompleted: Boolean,
    val isImportant: Boolean?,
    val notificationAt: String?,
    val notificationMinutes: Int?,
    val occurrenceDate: String?,
    val repeatDaysOfMonth: List<Int>?,
    val repeatDaysOfWeek: List<String>?,
    val repeatEndDate: String?,
    val repeatType: String?,
    val taskId: Long,
    val taskType: String,
    val title: String,
    val updatedAt: String
)

// 일정 목록 / 루틴 목록 조회 응답
data class TaskListResponse(
    val totalCount: Int,
    val items: List<TaskDetailResponse> = emptyList()
)
