package com.example.todowithspirits.feature.calendar.model

import com.example.domain.model.TaskSummary
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

fun TaskSummary.toPlanItemData(): PlanItemData = PlanItemData(
    id = taskId.toInt(),
    title = title,
    isImportant = isImportant,
    isDone = isCompleted,
    endDate = occurrenceDate,
    endTime = endTime,
    isAllDay = isAllDay
)
