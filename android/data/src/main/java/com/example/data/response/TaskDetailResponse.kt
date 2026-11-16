package com.example.data.response

// 일정 단건 조회 · 생성 · 수정 응답 공용.
// 단건 조회는 그날의 occurrenceDate를, 생성·수정은 startDate(단발성: 날짜 / 반복: 반복 시작일)를 내려준다.
data class TaskDetailResponse(
    val completedAt: String?,
    val createdAt: String?,
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
    val startDate: String?,
    val taskId: Long,
    val title: String,
    val updatedAt: String
)
