package com.example.todowithspirits.feature.plan.model

import java.time.LocalDate
import java.time.LocalTime

enum class PlanType { TODO, ROUTINE }

data class PlanItemData(
    val id: Int,
    val title: String,
    val type: PlanType,
    val isImportant: Boolean,
    val isDone: Boolean,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val memo: String = "",
    val category: String? = null,
    val repeatInfo: String? = null
)
