package com.example.todowithspirits.feature.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.TaskSummary
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.home.state.HomeEffect
import com.example.todowithspirits.feature.home.state.HomeEvent
import com.example.todowithspirits.feature.home.state.HomeTodayTask
import com.example.todowithspirits.feature.home.state.HomeUiState
import com.example.todowithspirits.feature.home.state.SpiritInfo
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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val cancelTaskCompletionUseCase: CancelTaskCompletionUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(spiritInfo = SpiritInfo(name = "루미")))
    val uiState: StateFlow<HomeUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        loadTodayTasks()

        // 다른 화면(추가/수정, 캘린더)에서 일정이 바뀌면 오늘 일정을 다시 불러온다.
        taskRefreshBus.events
            .onEach { loadTodayTasks() }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.SettingClicked -> sendEffect(HomeEffect.NavigateToMyPage)
            HomeEvent.AlarmClicked -> sendEffect(HomeEffect.NavigateToAlarm)
            HomeEvent.ForestButtonClicked -> sendEffect(HomeEffect.NavigateToForest)
            HomeEvent.CalendarButtonClicked -> sendEffect(HomeEffect.NavigateToCalendar)
            HomeEvent.TodayTasksToggled -> toggleTodayTasks()
            is HomeEvent.TodayTaskCompletionChanged -> changeTodayTaskCompletion(event.id, event.isCompleted)
            is HomeEvent.TodayTaskClicked -> _uiState.update { it.copy(selectedTaskId = event.id) }
            HomeEvent.TodayTaskDetailDismissed -> closeTaskDetail()
            is HomeEvent.TodayTaskDeleteClicked -> deleteTodayTask(event.id)
            HomeEvent.SpiritClicked -> toggleSpiritMenu()
            HomeEvent.SpiritMenuAddClicked -> {
                // 화면을 옮기므로 메뉴는 닫아 두고, 돌아왔을 때 접힌 상태로 보이게 한다.
                _uiState.update { it.copy(isSpiritMenuVisible = false) }
                sendEffect(HomeEffect.NavigateToCalendar)
            }
            is HomeEvent.TodayTaskEditClicked -> {
                closeTaskDetail()
                sendEffect(HomeEffect.NavigateToEditTask(event.id))
            }
        }
    }

    private fun toggleTodayTasks() {
        _uiState.update { it.copy(isTodayTasksExpanded = !it.isTodayTasksExpanded) }
    }

    // 일정 목록 조회 API를 오늘 하루 범위(from = to = 오늘)로 호출한다.
    private fun loadTodayTasks() {
        viewModelScope.launchWithLoading {
            val today = LocalDate.now()

            getTaskCalendarUseCase(today, today)
                .onSuccess { calendar ->
                    _uiState.update { state ->
                        state.copy(
                            selectedDate = today,
                            todayTasks = calendar.items.map { it.toHomeTodayTask() }
                        )
                    }
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "오늘 일정을 불러오지 못했습니다")
                }
        }
    }

    private fun changeTodayTaskCompletion(id: Long, isCompleted: Boolean) {
        val date = _uiState.value.todayTasks.find { it.id == id }?.date ?: return

        viewModelScope.launchWithLoading {
            val result = if (isCompleted) {
                completeTaskUseCase(id, date).map { }
            } else {
                cancelTaskCompletionUseCase(id, date)
            }

            result
                .onSuccess { taskRefreshBus.notifyTaskChanged() }
                .onFailure {
                    val fallback = if (isCompleted) "완료 처리에 실패했습니다" else "완료 취소에 실패했습니다"
                    emitErrorMsg(it.localizedMessage ?: fallback)
                }
        }
    }

    // 정령을 누르면 펼쳐진 오늘 일정을 접고, 정령 위 아이콘 메뉴를 열고 닫는다.
    private fun toggleSpiritMenu() {
        _uiState.update {
            it.copy(
                isTodayTasksExpanded = false,
                isSpiritMenuVisible = !it.isSpiritMenuVisible
            )
        }
    }

    private fun closeTaskDetail() {
        _uiState.update { it.copy(selectedTaskId = null) }
    }

    private fun deleteTodayTask(id: Long) {
        closeTaskDetail()

        viewModelScope.launchWithLoading {
            deleteTasksUseCase(listOf(id))
                .onSuccess { taskRefreshBus.notifyTaskChanged() }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "삭제하지 못했습니다")
                }
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

// 종일 일정은 시간 없이 표시한다.
private fun TaskSummary.toHomeTodayTask(): HomeTodayTask = HomeTodayTask(
    id = taskId,
    title = title,
    isCompleted = isCompleted,
    isImportant = isImportant,
    date = occurrenceDate,
    time = endTime.takeUnless { isAllDay }
)
