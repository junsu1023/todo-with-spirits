package com.example.todowithspirits.feature.today.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.TaskSummary
import com.example.domain.model.TaskType
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.GetTaskUseCase
import com.example.domain.usecase.UpdateTodoUseCase
import com.example.todowithspirits.feature.add.viewmodel.concatenating
import com.example.todowithspirits.feature.add.viewmodel.toNewTodo
import com.example.todowithspirits.feature.plan.model.PlanType
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
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val cancelTaskCompletionUseCase: CancelTaskCompletionUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(TodayUiState(spiritInfo = SpiritInfo("루미", 99, 5555, 9999, 999)))
    val uiState: StateFlow<TodayUiState> get() = _uiState.asStateFlow()

    init {
        loadTask()
        loadWeekEvents()
        loadTodayAchievement()

        taskRefreshBus.events
            .onEach {
                loadTask()
                loadWeekEvents()
                loadTodayAchievement()
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

    fun completeTask(taskId: Long, date: LocalDate) {
        viewModelScope.launchWithLoading {
            completeTaskUseCase(taskId, date)
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    Log.e(TAG, "completeTask failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "완료 처리에 실패했습니다")
                }
        }
    }

    fun cancelTaskCompletion(taskId: Long, date: LocalDate) {
        viewModelScope.launchWithLoading {
            cancelTaskCompletionUseCase(taskId, date)
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    Log.e(TAG, "cancelTaskCompletion failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "완료 취소에 실패했습니다")
                }
        }
    }

    fun deleteTask(taskId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            deleteTasksUseCase(listOf(taskId))
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "deleteTask failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "삭제에 실패했습니다")
                }
        }
    }

    fun postponeTodo(taskId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            getTaskUseCase(taskId)
                .onSuccess { task ->
                    val postponedEndDateTime = Pair(
                        (task.endDate ?: LocalDate.now()).plusDays(1),
                        task.endTime ?: LocalTime.of(0, 0, 0)
                    ).concatenating()

                    updateTodoUseCase(taskId, task.toNewTodo(postponedEndDateTime))
                        .onSuccess {
                            taskRefreshBus.notifyTaskChanged()
                            onSuccess()
                        }
                        .onFailure {
                            Log.e(TAG, "postponeTodo update failed!", it)
                            emitErrorMsg(it.localizedMessage ?: "미루기에 실패했습니다")
                        }
                }
                .onFailure {
                    Log.e(TAG, "postponeTodo load failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "플랜 정보를 불러오지 못했습니다")
                }
        }
    }

    fun loadTodayAchievement() {
        viewModelScope.launchWithLoading {
            val today = LocalDate.now()

            getTaskCalendarUseCase(today, today).onSuccess { calendar ->
                val todayItems = calendar.items.filter {
                    it.taskType == TaskType.SCHEDULE.type || it.taskType == TaskType.ROUTINE.type
                }
                val totalCount = todayItems.size
                val completedCount = todayItems.count { it.isCompleted }
                val rate = if (totalCount == 0) 0 else completedCount * 100 / totalCount

                _uiState.update { it.copy(todayAchievementRate = rate) }
            }.onFailure {
                Log.e(TAG, "loadTodayAchievement failed!", it)
                emitErrorMsg(it.localizedMessage ?: "오늘의 달성률을 불러오지 못했습니다")
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
