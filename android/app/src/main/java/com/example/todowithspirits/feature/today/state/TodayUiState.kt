package com.example.todowithspirits.feature.today.state

import java.time.LocalDate
import java.time.LocalTime

data class TodayUiState(
    val spiritInfo: SpiritInfo,
    val todos: List<TodoItem> = emptyList(),
    val routines: List<RoutineItem> = emptyList()
)

data class SpiritInfo(
    val name: String = "",
    val level: Int = 0,
    val curExp: Int = 0,
    val maxExp: Int = 0,
    val todayPoints: Int = 0
)

data class TodoItem(
    val taskId: Long,
    val title: String,
    val isDone: Boolean,
    val isImportant: Boolean,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val memo: String = ""
)

data class RoutineItem(
    val taskId: Long,
    val title: String,
    val isDone: Boolean,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val memo: String = ""
)
