package com.example.todowithspirits.feature.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.home.state.HomeEffect
import com.example.todowithspirits.feature.home.state.HomeEvent
import com.example.todowithspirits.feature.home.state.HomeTodayTask
import com.example.todowithspirits.feature.home.state.HomeUiState
import com.example.todowithspirits.feature.home.state.SpiritInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            spiritInfo = SpiritInfo(name = "루미"),
            todayTasks = TempTodayTasks
        )
    )
    val uiState: StateFlow<HomeUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.SettingClicked -> sendEffect(HomeEffect.NavigateToMyPage)
            HomeEvent.AlarmClicked -> sendEffect(HomeEffect.NavigateToAlarm)
            HomeEvent.ForestButtonClicked -> sendEffect(HomeEffect.NavigateToForest)
            HomeEvent.CalendarButtonClicked -> sendEffect(HomeEffect.NavigateToCalendar)
            HomeEvent.TodayTasksToggled -> toggleTodayTasks()
            is HomeEvent.TodayTaskCompletionChanged -> changeTodayTaskCompletion(event.id, event.isCompleted)
        }
    }

    private fun toggleTodayTasks() {
        _uiState.update { it.copy(isTodayTasksExpanded = !it.isTodayTasksExpanded) }
    }

    // 임시 데이터라 서버 호출 없이 상태만 바꾼다.
    private fun changeTodayTaskCompletion(id: Long, isCompleted: Boolean) {
        _uiState.update { state ->
            state.copy(
                todayTasks = state.todayTasks.map { task ->
                    if (task.id == id) task.copy(isCompleted = isCompleted) else task
                }
            )
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

// TODO: 임시 데이터 - 실제 데이터 연결 시 GetTaskCalendarUseCase로 오늘 일정/루틴을 불러와 HomeTodayTask로 매핑해 교체
private val TempTodayTasks = listOf(
    HomeTodayTask(id = 1, title = "아침 스트레칭 10분", isCompleted = true, isImportant = false),
    HomeTodayTask(id = 2, title = "팀 회의 자료 정리", isCompleted = false, isImportant = true),
    HomeTodayTask(id = 3, title = "물 2L 마시기", isCompleted = true, isImportant = false),
    HomeTodayTask(id = 4, title = "병원 예약 확인", isCompleted = false, isImportant = true),
    HomeTodayTask(id = 5, title = "책 30쪽 읽기", isCompleted = false, isImportant = false),
    HomeTodayTask(id = 6, title = "저녁 장보기", isCompleted = false, isImportant = true)
)
