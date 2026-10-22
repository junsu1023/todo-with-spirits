package com.example.todowithspirits.feature.calendar.state

sealed interface CalendarSearchEffect {
    data object NavigateBack : CalendarSearchEffect
}
