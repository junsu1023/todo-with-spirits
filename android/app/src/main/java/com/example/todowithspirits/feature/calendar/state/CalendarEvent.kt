package com.example.todowithspirits.feature.calendar.state

import com.example.domain.model.PlanSortOption
import java.time.LocalDate
import java.time.YearMonth

sealed interface CalendarEvent {
    data object BackClicked : CalendarEvent
    data object AddClicked : CalendarEvent
    data class DateSelected(val date: LocalDate) : CalendarEvent
    data class MonthChanged(val month: YearMonth) : CalendarEvent
    data object HideCompletedToggled : CalendarEvent
    data class SortOptionSelected(val option: PlanSortOption) : CalendarEvent
    data class TaskClicked(val taskId: Int) : CalendarEvent
    data class TaskCompletionToggled(val taskId: Int, val isDone: Boolean, val date: LocalDate?) : CalendarEvent
    data class TaskDeleteClicked(val taskId: Int) : CalendarEvent
    data class TaskEditClicked(val taskId: Int) : CalendarEvent
    data object SearchClicked : CalendarEvent
    data object SearchClosed : CalendarEvent
    data class SearchInputChanged(val input: String) : CalendarEvent
    data object SearchSubmitted : CalendarEvent
    data class RecentSearchClicked(val query: String) : CalendarEvent
    data class RecentSearchRemoved(val query: String) : CalendarEvent
    data object RecentSearchesCleared : CalendarEvent
    data object SearchResultCleared : CalendarEvent
}
