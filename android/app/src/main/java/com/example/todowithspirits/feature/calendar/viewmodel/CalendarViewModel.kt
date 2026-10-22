package com.example.todowithspirits.feature.calendar.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.PlanSortOption
import com.example.domain.model.TaskSummary
import com.example.domain.model.TaskType
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import com.example.todowithspirits.feature.calendar.model.PlanType
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
            CalendarEvent.AlarmClicked -> sendEffect(CalendarEffect.NavigateToAlarm)
            CalendarEvent.AddClicked -> sendEffect(CalendarEffect.NavigateToAdd)
            is CalendarEvent.DateSelected -> selectDate(event.date)
            is CalendarEvent.MonthChanged -> changeMonth(event.month)
            CalendarEvent.HideCompletedToggled -> toggleHideCompleted()
            is CalendarEvent.SortOptionSelected -> _uiState.update { it.copy(sortOption = event.option) }
            is CalendarEvent.TaskClicked -> _uiState.update {
                it.copy(expandedTaskId = if (it.expandedTaskId == event.taskId) null else event.taskId)
            }
            is CalendarEvent.TaskCompletionToggled -> toggleTaskCompletion(event.taskId.toLong(), event.isDone)
            is CalendarEvent.TaskDeleteClicked -> deleteTask(event.taskId.toLong())
            is CalendarEvent.TaskEditClicked -> sendEffect(CalendarEffect.NavigateToEditTask(event.taskId.toLong()))
        }
    }

    private fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, expandedTaskId = null) }
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

    private fun toggleTaskCompletion(taskId: Long, isDone: Boolean) {
        val date = _uiState.value.selectedDate
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
                            DayPlanEvents(
                                types = items.map {
                                    if (it.taskType == TaskType.ROUTINE.type) PlanType.ROUTINE else PlanType.TODO
                                },
                                importantCount = items.count { it.isImportant }
                            )
                        }

                    _uiState.update { it.copy(calendarEvents = events) }
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
    type = if (taskType == TaskType.ROUTINE.type) PlanType.ROUTINE else PlanType.TODO,
    isImportant = isImportant,
    isDone = isCompleted,
    endDate = occurrenceDate,
    endTime = endTime,
    isAllDay = isAllDay
)
