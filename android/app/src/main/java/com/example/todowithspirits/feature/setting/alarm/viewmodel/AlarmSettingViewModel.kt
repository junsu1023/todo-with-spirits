package com.example.todowithspirits.feature.setting.alarm.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetNotificationSettingUseCase
import com.example.domain.usecase.UpdateNotificationSettingUseCase
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

@HiltViewModel
class AlarmSettingViewModel @Inject constructor(
    private val getNotificationSettingUseCase: GetNotificationSettingUseCase,
    private val updateNotificationSettingUseCase: UpdateNotificationSettingUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AlarmSettingUiState())
    val uiState: StateFlow<AlarmSettingUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<AlarmSettingEffect>(Channel.BUFFERED)
    val effect: Flow<AlarmSettingEffect> = _effect.receiveAsFlow()

    init {
        loadNotificationSetting()
    }

    fun onEvent(event: AlarmSettingEvent) {
        when (event) {
            AlarmSettingEvent.BackClicked,
            AlarmSettingEvent.ConfirmClicked -> sendEffect(AlarmSettingEffect.NavigateBack)
            is AlarmSettingEvent.AlarmToggled -> toggleAlarm(event.type, event.isOn)
        }
    }

    private fun loadNotificationSetting() {
        viewModelScope.launchWithLoading {
            getNotificationSettingUseCase()
                .onSuccess { setting ->
                    Log.d(TAG, "getNotificationSetting success = $setting")
                    _uiState.update {
                        it.copy(
                            isOnActiveRemind = setting.reminderEnabled,
                            isOnPostponePlan = setting.postponedTaskEnabled,
                            isOnRoutineGuide = setting.routineReminderEnabled,
                            isOnStreakSave = setting.streakSaveEnabled,
                            isOnNightPush = setting.nightPushEnabled
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "getNotificationSetting failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "알림 설정을 불러오지 못했습니다")
                }
        }
    }

    private fun toggleAlarm(type: AlarmSettingType, isOn: Boolean) {
        setAlarm(type, isOn)

        viewModelScope.launch {
            updateNotificationSetting(type, isOn)
                .onFailure {
                    Log.e(TAG, "updateNotificationSetting failed! type = $type", it)
                    setAlarm(type, !isOn)
                    emitErrorMsg(it.localizedMessage ?: "알림 설정을 변경하지 못했습니다")
                }
        }
    }

    private suspend fun updateNotificationSetting(type: AlarmSettingType, isOn: Boolean): Result<Unit> =
        when (type) {
            AlarmSettingType.ACTIVE_REMIND -> updateNotificationSettingUseCase(reminderEnabled = isOn)
            AlarmSettingType.POSTPONE_PLAN -> updateNotificationSettingUseCase(postponedTaskEnabled = isOn)
            AlarmSettingType.ROUTINE_GUIDE -> updateNotificationSettingUseCase(routineReminderEnabled = isOn)
            AlarmSettingType.STREAK_SAVE -> updateNotificationSettingUseCase(streakSaveEnabled = isOn)
            AlarmSettingType.NIGHT_PUSH -> updateNotificationSettingUseCase(nightPushEnabled = isOn)
        }

    private fun setAlarm(type: AlarmSettingType, isOn: Boolean) {
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
