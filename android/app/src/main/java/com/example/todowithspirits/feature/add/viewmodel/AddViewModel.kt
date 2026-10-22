package com.example.todowithspirits.feature.add.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AlarmOption
import com.example.domain.model.NewTodo
import com.example.domain.model.TaskType
import com.example.domain.usecase.CreateTodoUseCase
import com.example.domain.usecase.GetTaskUseCase
import com.example.domain.usecase.UpdateTodoUseCase
import com.example.todowithspirits.feature.add.state.AddEffect
import com.example.todowithspirits.feature.add.state.AddEvent
import com.example.todowithspirits.feature.add.state.AddUiState
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

// 카테고리 선택이 디자인에서 빠져, 서버에는 '없음'에 해당하는 NONE으로 보낸다.
private const val TODO_CATEGORY_NONE = "NONE"

@HiltViewModel
class AddViewModel @Inject constructor(
    private val createTodoUseCase: CreateTodoUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<AddEffect>(Channel.BUFFERED)
    val effect: Flow<AddEffect> = _effect.receiveAsFlow()

    fun onEvent(event: AddEvent) {
        when (event) {
            AddEvent.BackClicked -> sendEffect(AddEffect.NavigateBack)
            is AddEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is AddEvent.ImportantChanged -> _uiState.update { it.copy(isImportant = event.isImportant) }
            is AddEvent.DateChanged -> _uiState.update { it.copy(date = event.date) }
            is AddEvent.TimeEnabledChanged -> _uiState.update { it.copy(isTimeEnabled = event.isEnabled) }
            is AddEvent.DueTimeChanged -> _uiState.update { it.copy(dueTime = event.time) }
            is AddEvent.RepeatOptionChanged -> _uiState.update { it.copy(repeatOption = event.option) }
            is AddEvent.AlarmOptionChanged -> _uiState.update { it.copy(alarmOption = event.option) }
            AddEvent.ConfirmClicked -> if (_uiState.value.isEditMode) updateTodo() else createTodo()
        }
    }

    fun loadTaskForEdit(taskId: Long) {
        viewModelScope.launchWithLoading {
            getTaskUseCase(taskId)
                .onSuccess { task ->
                    // 루틴은 더 이상 만들거나 고칠 수 없다. 일정 API로 수정하면 실패하므로 막는다.
                    if (task.taskType == TaskType.ROUTINE.type) {
                        emitErrorMsg("루틴 일정은 수정할 수 없어요")
                        sendEffect(AddEffect.NavigateBack)
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            editingTaskId = taskId,
                            title = task.title,
                            isImportant = task.isImportant,
                            date = task.endDate ?: it.date,
                            isTimeEnabled = task.endTime != null,
                            dueTime = task.endTime ?: it.dueTime,
                            alarmOption = task.notificationMinutes.toAlarmOption(),
                            isPublic = task.isPublic,
                            memo = task.memo
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "loadTaskForEdit failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "일정 정보를 불러오지 못했습니다")
                }
        }
    }

    private fun createTodo() {
        viewModelScope.launchWithLoading {
            createTodoUseCase(_uiState.value.toNewTodo())
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                    sendEffect(AddEffect.NavigateBack)
                }
                .onFailure {
                    Log.e(TAG, "createTodo failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "일정 추가에 실패했습니다")
                }
        }
    }

    private fun updateTodo() {
        val taskId = _uiState.value.editingTaskId ?: return

        viewModelScope.launchWithLoading {
            updateTodoUseCase(taskId, _uiState.value.toNewTodo())
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                    sendEffect(AddEffect.NavigateBack)
                }
                .onFailure {
                    Log.e(TAG, "updateTodo failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "일정 수정에 실패했습니다")
                }
        }
    }

    private fun sendEffect(effect: AddEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

private fun AddUiState.toNewTodo(): NewTodo = NewTodo(
    title = title.trim(),
    isAllDay = !isTimeEnabled,
    endDateTime = Pair(date, dueTime).concatenating(),
    isImportant = isImportant,
    notificationType = alarmOption,
    category = TODO_CATEGORY_NONE,
    isPublic = isPublic,
    memo = memo.ifBlank { null }
)

private fun Pair<LocalDate, LocalTime>.concatenating(): String =
    "%sT%02d:%02d:%02d".format(first, second.hour, second.minute, second.second)

private fun Int?.toAlarmOption(): AlarmOption = when (this) {
    10 -> AlarmOption.TEN_MINUTES
    30 -> AlarmOption.THIRTY_MINUTES
    60 -> AlarmOption.ONE_HOUR
    else -> AlarmOption.NONE
}
