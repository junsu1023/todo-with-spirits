package com.example.todowithspirits.feature.calendar.model

import java.time.LocalDate
import java.time.LocalTime

enum class PlanType { TODO, ROUTINE }

data class PlanItemData(
    val id: Int,
    val title: String,
    val type: PlanType,
    val isImportant: Boolean,
    val isDone: Boolean,
    val endDate: LocalDate?,
    val endTime: LocalTime?,
    val isAllDay: Boolean = false
)
