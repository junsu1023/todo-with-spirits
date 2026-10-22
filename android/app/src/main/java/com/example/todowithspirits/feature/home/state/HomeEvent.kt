package com.example.todowithspirits.feature.home.state

sealed interface HomeEvent {
    data object SettingClicked : HomeEvent
    data object AlarmClicked : HomeEvent
    data object ForestButtonClicked : HomeEvent
    data object CalendarButtonClicked : HomeEvent
    data object TodayTasksToggled : HomeEvent
    data class TodayTaskCompletionChanged(val id: Long, val isCompleted: Boolean) : HomeEvent
    data class TodayTaskClicked(val id: Long) : HomeEvent
    data object TodayTaskDetailDismissed : HomeEvent
    data class TodayTaskDeleteClicked(val id: Long) : HomeEvent
    data class TodayTaskEditClicked(val id: Long) : HomeEvent
}
