package com.example.todowithspirits.feature.today.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.TaskSummary
import com.example.domain.model.TaskType
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.plan.PlanType
import com.example.todowithspirits.feature.today.state.RoutineItem
import com.example.todowithspirits.feature.today.state.SpiritInfo
import com.example.todowithspirits.feature.today.state.TodayUiState
import com.example.todowithspirits.feature.today.state.TodoItem
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(TodayUiState(spiritInfo = SpiritInfo("루미", 99, 5555, 9999, 999)))
    val uiState: StateFlow<TodayUiState> get() = _uiState.asStateFlow()

    init {
        loadTask()
        loadWeekEvents()

        taskRefreshBus.events
            .onEach {
                loadTask()
                loadWeekEvents()
            }
            .launchIn(viewModelScope)
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadTask()
        loadWeekEvents()
    }

    fun loadTask() {
        viewModelScope.launchWithLoading {
            val selectedDate = _uiState.value.selectedDate

            getTaskCalendarUseCase(selectedDate, selectedDate).onSuccess { calendar ->
                _uiState.update { state ->
                    Log.d(TAG, "loadTask = $calendar")

                    state.copy(
                        todos = calendar.items
                            .filter { it.taskType == TaskType.SCHEDULE.type }
                            .map { it.toTodoItem() },
                        routines = calendar.items
                            .filter { it.taskType == TaskType.ROUTINE.type }
                            .map { it.toRoutineItem() }
                    )
                }
            }.onFailure {
                Log.e(TAG, "loadToday failed!", it)
                emitErrorMsg(it.localizedMessage ?: "오늘의 일정을 불러오지 못했습니다")
            }
        }
    }

    fun loadWeekEvents() {
        viewModelScope.launchWithLoading {
            val selectedDate = _uiState.value.selectedDate
            val weekStart = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
            val weekEnd = weekStart.plusDays(6)

            getTaskCalendarUseCase(weekStart, weekEnd).onSuccess { calendar ->
                val events = calendar.items
                    .map { item ->
                        val type = if (item.taskType == TaskType.ROUTINE.type) PlanType.ROUTINE else PlanType.TODO
                        item.occurrenceDate to type
                    }
                    .groupBy({ it.first }, { it.second })

                _uiState.update { it.copy(weekEvents = events) }
            }.onFailure {
                Log.e(TAG, "loadWeekEvents failed!", it)
                emitErrorMsg(it.localizedMessage ?: "주간 일정을 불러오지 못했습니다")
            }
        }
    }
}

private fun TaskSummary.toTodoItem() = TodoItem(
    taskId = taskId,
    title = title,
    isDone = isCompleted,
    isImportant = isImportant,
    dueDate = occurrenceDate,
    dueTime = endTime,
    memo = memo ?: ""
)

private fun TaskSummary.toRoutineItem() = RoutineItem(
    taskId = taskId,
    title = title,
    isDone = isCompleted,
    dueDate = occurrenceDate,
    dueTime = endTime,
    memo = memo ?: ""
)
