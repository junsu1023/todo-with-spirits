package com.example.todowithspirits.feature.calendar.state

sealed interface CalendarSearchEvent {
    data object BackClicked : CalendarSearchEvent
    data class InputChanged(val input: String) : CalendarSearchEvent
    data object Submitted : CalendarSearchEvent
}
