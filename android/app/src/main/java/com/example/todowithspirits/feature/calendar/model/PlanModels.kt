package com.example.todowithspirits.feature.calendar.model

import java.time.LocalDate
import java.time.LocalTime

data class PlanItemData(
    val id: Int,
    val title: String,
    val isImportant: Boolean,
    val isDone: Boolean,
    val endDate: LocalDate?,
    val endTime: LocalTime?,
    val isAllDay: Boolean = false
)
