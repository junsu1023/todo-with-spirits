package com.example.data.request

data class CreateRoutineRequest(
    val title: String,
    val repeatType: String,
    val repeatEndDate: String? = null,
    val repeatDaysOfWeek: List<String>? = null,
    val repeatDaysOfMonth: List<Int>? = null,
    val notification: String? = null,
    val isPublic: Boolean = false,
    val memo: String? = null
)
