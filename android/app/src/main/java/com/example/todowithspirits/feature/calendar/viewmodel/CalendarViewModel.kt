package com.example.todowithspirits.feature.calendar.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.PlanSortOption
import com.example.domain.model.TaskSummary
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import com.example.todowithspirits.feature.calendar.state.CalendarEffect
import com.example.todowithspirits.feature.calendar.state.CalendarEvent
import com.example.todowithspirits.feature.calendar.state.CalendarUiState
import com.example.todowithspirits.feature.calendar.state.DayPlanEvents
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val cancelTaskCompletionUseCase: CancelTaskCompletionUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<CalendarEffect>(Channel.BUFFERED)
    val effect: Flow<CalendarEffect> = _effect.receiveAsFlow()

    init {
        loadPlans()
        loadCalendarEvents()

        taskRefreshBus.events
            .onEach {
                loadPlans()
                loadCalendarEvents()
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            CalendarEvent.BackClicked -> sendEffect(CalendarEffect.NavigateBack)
            CalendarEvent.AddClicked -> sendEffect(CalendarEffect.NavigateToAdd)
            is CalendarEvent.DateSelected -> selectDate(event.date)
            is CalendarEvent.MonthChanged -> changeMonth(event.month)
            CalendarEvent.HideCompletedToggled -> toggleHideCompleted()
            is CalendarEvent.SortOptionSelected -> _uiState.update { it.copy(sortOption = event.option) }
            is CalendarEvent.TaskClicked -> _uiState.update {
                it.copy(expandedTaskId = if (it.expandedTaskId == event.taskId) null else event.taskId)
            }
            is CalendarEvent.TaskCompletionToggled -> toggleTaskCompletion(event.taskId.toLong(), event.isDone, event.date)
            is CalendarEvent.TaskDeleteClicked -> deleteTask(event.taskId.toLong())
            is CalendarEvent.TaskEditClicked -> sendEffect(CalendarEffect.NavigateToEditTask(event.taskId.toLong()))
            CalendarEvent.SearchClicked -> _uiState.update { it.copy(isSearchOpen = true) }
            CalendarEvent.SearchClosed -> _uiState.update { it.copy(isSearchOpen = false, searchInput = "") }
            is CalendarEvent.SearchInputChanged -> _uiState.update { it.copy(searchInput = event.input) }
            CalendarEvent.SearchSubmitted -> search(_uiState.value.searchInput)
            is CalendarEvent.RecentSearchClicked -> search(event.query)
            is CalendarEvent.RecentSearchRemoved -> _uiState.update {
                it.copy(recentSearches = it.recentSearches - event.query)
            }
            CalendarEvent.RecentSearchesCleared -> _uiState.update { it.copy(recentSearches = emptyList()) }
            CalendarEvent.SearchResultCleared -> _uiState.update { it.copy(appliedQuery = null, expandedTaskId = null) }
        }
    }

    // 최근 검색어는 앱 실행 중에만 유지한다. (맨 앞에 추가, 중복 제거, 최대 10개)
    private fun search(input: String) {
        val query = input.trim()
        if (query.isEmpty()) return

        _uiState.update {
            it.copy(
                isSearchOpen = false,
                searchInput = "",
                appliedQuery = query,
                expandedTaskId = null,
                recentSearches = (listOf(query) + it.recentSearches.filterNot { recent -> recent == query })
                    .take(MAX_RECENT_SEARCHES)
            )
        }
    }

    private fun selectDate(date: LocalDate) {
        // 날짜를 고르면 검색 결과에서 그 날짜의 일정으로 돌아간다.
        _uiState.update { it.copy(selectedDate = date, expandedTaskId = null, appliedQuery = null) }
        loadPlans()
    }

    private fun changeMonth(month: YearMonth) {
        if (month == _uiState.value.calendarMonth) return

        _uiState.update { it.copy(calendarMonth = month) }
        loadCalendarEvents()
    }

    private fun toggleHideCompleted() {
        _uiState.update {
            val hidden = !it.isHidden
            val sortOption = if (hidden && it.sortOption == PlanSortOption.COMPLETE) {
                PlanSortOption.DEADLINE
            } else {
                it.sortOption
            }

            it.copy(isHidden = hidden, sortOption = sortOption)
        }
    }

    private fun toggleTaskCompletion(taskId: Long, isDone: Boolean, taskDate: LocalDate?) {
        val date = taskDate ?: _uiState.value.selectedDate
        if (isDone) cancelTaskCompletion(taskId, date) else completeTask(taskId, date)
    }

    private fun sendEffect(effect: CalendarEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun loadPlans() {
        viewModelScope.launchWithLoading {
            val selectedDate = _uiState.value.selectedDate

            getTaskCalendarUseCase(selectedDate, selectedDate)
                .onSuccess { calendar ->
                    _uiState.update { it.copy(plans = calendar.items.map { item -> item.toPlanItemData() }) }
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "플랜을 불러오지 못했습니다")
                }
        }
    }

    private fun deleteTask(taskId: Long) {
        viewModelScope.launchWithLoading {
            deleteTasksUseCase(listOf(taskId))
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "삭제하지 못했습니다")
                }
        }
    }

    private fun completeTask(taskId: Long, date: LocalDate) {
        viewModelScope.launchWithLoading {
            completeTaskUseCase(taskId, date)
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "완료 처리에 실패했습니다")
                }
        }
    }

    private fun cancelTaskCompletion(taskId: Long, date: LocalDate) {
        viewModelScope.launchWithLoading {
            cancelTaskCompletionUseCase(taskId, date)
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "완료 취소에 실패했습니다")
                }
        }
    }

    private fun loadCalendarEvents() {
        viewModelScope.launchWithLoading {
            val month = _uiState.value.calendarMonth
            val monthStart = month.atDay(1)
            val monthEnd = month.atEndOfMonth()

            getTaskCalendarUseCase(monthStart, monthEnd)
                .onSuccess { calendar ->
                    val events = calendar.items
                        .groupBy { it.occurrenceDate }
                        .mapValues { (_, items) ->
                            DayPlanEvents(importantCount = items.count { it.isImportant })
                        }

                    _uiState.update {
                        it.copy(
                            calendarEvents = events,
                            monthPlans = calendar.items.map { item -> item.toPlanItemData() }
                        )
                    }
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "캘린더 일정을 불러오지 못했습니다")
                }
        }
    }
}

private fun TaskSummary.toPlanItemData(): PlanItemData = PlanItemData(
    id = taskId.toInt(),
    title = title,
    isImportant = isImportant,
    isDone = isCompleted,
    endDate = occurrenceDate,
    endTime = endTime,
    isAllDay = isAllDay
)

private const val MAX_RECENT_SEARCHES = 10
