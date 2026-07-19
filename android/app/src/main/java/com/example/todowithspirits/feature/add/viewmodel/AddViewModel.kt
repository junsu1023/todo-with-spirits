package com.example.todowithspirits.feature.add.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AlarmOption
import com.example.domain.model.CategoryOption
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.PublicStateOption
import com.example.domain.model.RepeatOption
import com.example.domain.model.Task
import com.example.domain.model.TaskType
import com.example.domain.usecase.CreateRoutineUseCase
import com.example.domain.usecase.CreateTodoUseCase
import com.example.domain.usecase.GetTaskUseCase
import com.example.domain.usecase.UpdateRoutineUseCase
import com.example.domain.usecase.UpdateTodoUseCase
import com.example.todowithspirits.feature.add.state.AddUiState
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val createTodoUseCase: CreateTodoUseCase,
    private val createRoutineUseCase: CreateRoutineUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> get() = _uiState.asStateFlow()

    fun setTitle(title: String) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(title = title) }
        }
    }

    fun setImportant(important: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(isImportant = important) }
        }
    }

    fun setDate(date: LocalDate) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(date = date) }
        }
    }

    fun setTimeEnabled(enabled: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(isTimeEnabled = enabled) }
        }
    }

    fun setDueTime(time: LocalTime) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(dueTime = time) }
        }
    }

    fun setRepeatOption(option: RepeatOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(repeatOption = option) }
        }
    }

    fun toggleWeekDay(day: DayOfWeek) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(
                    selectedWeekDays = it.selectedWeekDays.toMutableSet().apply {
                        if (day in this) remove(day) else add(day)
                    }
                )
            }
        }
    }

    fun toggleMonthDay(day: Int) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(
                    selectedMonthDays = it.selectedMonthDays.toMutableSet().apply {
                        if (day in this) remove(day) else add(day)
                    }
                )
            }
        }
    }

    fun setExcludeHolidays(exclude: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(excludeHolidays = exclude) }
        }
    }

    fun setAlarmOption(option: AlarmOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(alarmOption = option) }
        }
    }

    fun setCategoryOption(option: CategoryOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(categoryOption = option) }
        }
    }

    fun setPublicOption(option: PublicStateOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(publicOption = option) }
        }
    }

    fun setMemo(memo: String) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(memo = memo) }
        }
    }

    fun loadTaskForEdit(taskId: Long) {
        viewModelScope.launchWithLoading {
            getTaskUseCase(taskId)
                .onSuccess { task ->
                    println("test-kjs: task = $task")
                    val taskType = if(task.taskType == TaskType.ROUTINE.type) TaskType.ROUTINE else TaskType.SCHEDULE

                    _uiState.update {
                        it.copy(
                            editingTaskId = taskId,
                            editingTaskType = taskType,
                            title = task.title,
                            isImportant = task.isImportant,
                            date = task.endDate ?: it.date,
                            isTimeEnabled = task.endTime != null,
                            dueTime = task.endTime ?: it.dueTime,
                            alarmOption = task.notificationMinutes.toAlarmOption(),
                            categoryOption = CategoryOption.entries.find { option -> option.name == task.category } ?: CategoryOption.NONE,
                            publicOption = if (task.isPublic) PublicStateOption.PUBLIC else PublicStateOption.PRIVATE,
                            memo = task.memo
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "loadTaskForEdit failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "플랜 정보를 불러오지 못했습니다")
                }
        }
    }

    fun registerTodo(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            val state = _uiState.value

            val todo = NewTodo(
                title = state.title,
                isAllDay = !state.isTimeEnabled,
                endDateTime = Pair(state.date, state.dueTime).concatenating(),
                isImportant = state.isImportant,
                notificationType = state.alarmOption,
                category = state.categoryOption,
                isPublic = state.publicOption == PublicStateOption.PUBLIC,
                memo = state.memo.ifBlank { null }
            )

            createTodoUseCase(todo)
                .onSuccess {
                    Log.d(TAG, "registerTodo success = $it")
                    _uiState.update { AddUiState() }
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "registerTodo, failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "Todo 생성에 실패했습니다")
                }
        }
    }

    fun registerRoutine(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            val state = _uiState.value

            val routine = NewRoutine(
                title = state.title,
                repeatType = state.repeatOption,
                repeatDaysOfWeek = state.selectedWeekDays.toList(),
                repeatDaysOfMonth = state.selectedMonthDays.toList(),
                notification = state.alarmOption,
                isPublic = state.publicOption == PublicStateOption.PUBLIC,
                memo = state.memo.ifBlank { null }
            )

            createRoutineUseCase(routine)
                .onSuccess {
                    Log.d(TAG, "registerRoutine success = $it")
                    _uiState.update { AddUiState() }
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "registerRoutine failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "루틴 생성에 실패했습니다")
                }
        }
    }

    fun updateTodo(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            val state = _uiState.value
            val taskId = state.editingTaskId ?: return@launchWithLoading

            val todo = NewTodo(
                title = state.title,
                isAllDay = !state.isTimeEnabled,
                endDateTime = Pair(state.date, state.dueTime).concatenating(),
                isImportant = state.isImportant,
                notificationType = state.alarmOption,
                category = state.categoryOption,
                isPublic = state.publicOption == PublicStateOption.PUBLIC,
                memo = state.memo.ifBlank { null }
            )

            updateTodoUseCase(taskId, todo)
                .onSuccess {
                    Log.d(TAG, "updateTodo success = $it")
                    _uiState.update { AddUiState() }
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "updateTodo failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "Todo 수정에 실패했습니다")
                }
        }
    }

    fun updateRoutine(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            val state = _uiState.value
            val taskId = state.editingTaskId ?: return@launchWithLoading

            val routine = NewRoutine(
                title = state.title,
                repeatType = state.repeatOption,
                category = state.categoryOption,
                repeatDaysOfWeek = state.selectedWeekDays.toList(),
                repeatDaysOfMonth = state.selectedMonthDays.toList(),
                notification = state.alarmOption,
                isPublic = state.publicOption == PublicStateOption.PUBLIC,
                excludeHoliday = state.excludeHolidays,
                memo = state.memo.ifBlank { null }
            )

            updateRoutineUseCase(taskId, routine)
                .onSuccess {
                    Log.d(TAG, "updateRoutine success = $it")
                    _uiState.update { AddUiState() }
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "updateRoutine failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "루틴 수정에 실패했습니다")
                }
        }
    }
}

fun Pair<LocalDate, LocalTime>.concatenating(): String =
    "%sT%02d:%02d:%02d".format(first, second.hour, second.minute, second.second)

fun Int?.toAlarmOption(): AlarmOption = when (this) {
    10 -> AlarmOption.TEN_MINUTES
    30 -> AlarmOption.THIRTY_MINUTES
    60 -> AlarmOption.ONE_HOUR
    else -> AlarmOption.NONE
}

fun Task.toNewTodo(endDateTime: String): NewTodo = NewTodo(
    title = title,
    isAllDay = isAllDay,
    endDateTime = endDateTime,
    isImportant = isImportant,
    notificationType = notificationMinutes.toAlarmOption(),
    category = CategoryOption.entries.find { it.name == category } ?: CategoryOption.NONE,
    isPublic = isPublic,
    memo = memo.ifBlank { null }
)
