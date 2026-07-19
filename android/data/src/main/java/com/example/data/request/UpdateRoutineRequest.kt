package com.example.data.request

data class UpdateRoutineRequest(
    val title: String,
    val repeatType: String,
    val category: String = "NONE",
    val repeatEndDate: String? = null,
    val repeatDaysOfWeek: List<String>? = null,
    val repeatDaysOfMonth: List<Int>? = null,
    val notificationType: String? = null,
    val isPublic: Boolean = false,
    val excludeHoliday: Boolean = false,
    val memo: String? = null
)
