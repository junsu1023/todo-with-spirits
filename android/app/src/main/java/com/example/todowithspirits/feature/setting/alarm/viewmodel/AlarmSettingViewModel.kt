package com.example.todowithspirits.feature.setting.alarm.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEffect
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEvent
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingType
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingUiState
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

// TODO 알림 설정 저장/조회 API가 없어 화면 안에서만 값이 바뀐다. 화면을 다시 열면 초기값으로 돌아간다.
@HiltViewModel
class AlarmSettingViewModel @Inject constructor() : BaseViewModel() {
    private val _uiState = MutableStateFlow(AlarmSettingUiState())
    val uiState: StateFlow<AlarmSettingUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<AlarmSettingEffect>(Channel.BUFFERED)
    val effect: Flow<AlarmSettingEffect> = _effect.receiveAsFlow()

    fun onEvent(event: AlarmSettingEvent) {
        when (event) {
            AlarmSettingEvent.BackClicked,
            AlarmSettingEvent.ConfirmClicked -> sendEffect(AlarmSettingEffect.NavigateBack)
            is AlarmSettingEvent.AlarmToggled -> toggleAlarm(event.type, event.isOn)
        }
    }

    private fun toggleAlarm(type: AlarmSettingType, isOn: Boolean) {
        _uiState.update {
            when (type) {
                AlarmSettingType.ACTIVE_REMIND -> it.copy(isOnActiveRemind = isOn)
                AlarmSettingType.POSTPONE_PLAN -> it.copy(isOnPostponePlan = isOn)
                AlarmSettingType.ROUTINE_GUIDE -> it.copy(isOnRoutineGuide = isOn)
                AlarmSettingType.STREAK_SAVE -> it.copy(isOnStreakSave = isOn)
                AlarmSettingType.NIGHT_PUSH -> it.copy(isOnNightPush = isOn)
            }
        }
    }

    private fun sendEffect(effect: AlarmSettingEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
