package com.example.todowithspirits.feature.calendar.state

sealed interface CalendarEffect {
    data object NavigateBack : CalendarEffect
    data object NavigateToAdd : CalendarEffect
    data class NavigateToEditTask(val taskId: Long) : CalendarEffect
    data class NavigateToSearch(val query: String) : CalendarEffect
}
