package com.example.data.request

// 루틴 생성/수정 공용. repeatDaysOfWeek은 WEEKLY, repeatDaysOfMonth는 MONTHLY일 때 필수.
data class RoutineRequest(
    val title: String,
    val repeatType: String,
    val repeatEndDate: String? = null,
    val repeatDaysOfWeek: List<String>? = null,
    val repeatDaysOfMonth: List<Int>? = null,
    val notificationType: String = "NONE",
    val excludeHoliday: Boolean = false
)
