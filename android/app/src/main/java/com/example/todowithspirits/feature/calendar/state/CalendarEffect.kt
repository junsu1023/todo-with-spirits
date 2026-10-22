package com.example.todowithspirits.feature.calendar.state

sealed interface CalendarEffect {
    data object NavigateToAlarm : CalendarEffect
    data object NavigateToAdd : CalendarEffect
    data class NavigateToEditTask(val taskId: Long) : CalendarEffect
}
