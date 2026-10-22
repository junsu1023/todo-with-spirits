package com.example.todowithspirits.feature.home.state

sealed interface HomeEffect {
    data object NavigateToMyPage : HomeEffect
    data object NavigateToAlarm : HomeEffect
    data object NavigateToForest : HomeEffect
    data object NavigateToCalendar : HomeEffect
    data class NavigateToEditTask(val taskId: Long) : HomeEffect
}
