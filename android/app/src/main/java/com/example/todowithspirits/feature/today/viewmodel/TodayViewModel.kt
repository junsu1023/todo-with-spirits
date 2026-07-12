package com.example.todowithspirits.feature.today.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.TaskSummary
import com.example.domain.model.TaskType
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.today.state.RoutineItem
import com.example.todowithspirits.feature.today.state.SpiritInfo
import com.example.todowithspirits.feature.today.state.TodayUiState
import com.example.todowithspirits.feature.today.state.TodoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase
) : BaseViewModel() {
    private val _uiState =
        MutableStateFlow(TodayUiState(spiritInfo = SpiritInfo("루미", 99, 5555, 9999, 999)))
    val uiState: StateFlow<TodayUiState> get() = _uiState.asStateFlow()

    init {
        loadToday()
    }

    fun loadToday() {
        viewModelScope.launchWithLoading {
            val today = LocalDate.now()
            getTaskCalendarUseCase(today, today).onSuccess { calendar ->
                _uiState.update { state ->
                    Log.d(TAG, "loadTask = $calendar")

                    state.copy(
                        todos = calendar.items
                            .filter { it.taskType == TaskType.TODO.type }
                            .map { it.toTodoItem() },
                        routines = calendar.items
                            .filter { it.taskType == TaskType.HABIT.type }
                            .map { it.toRoutineItem() }
                    )
                }
            }.onFailure {
                Log.e(TAG, "loadToday failed!", it)
                emitErrorMsg(it.localizedMessage ?: "오늘의 일정을 불러오지 못했습니다")
            }
        }
    }
}

private fun TaskSummary.toTodoItem() = TodoItem(
    taskId = taskId,
    title = title,
    isDone = isCompleted,
    isImportant = isImportant,
    dueDate = startDate,
    dueTime = startTime,
    memo = memo ?: ""
)

private fun TaskSummary.toRoutineItem() = RoutineItem(
    taskId = taskId,
    title = title,
    isDone = isCompleted,
    dueDate = startDate,
    dueTime = startTime,
    memo = memo ?: ""
)
