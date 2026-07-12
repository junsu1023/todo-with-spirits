package com.example.todowithspirits.feature.setting.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.setting.state.AlarmSettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AlarmSettingViewModel @Inject constructor(): BaseViewModel() {
    private val _uiState = MutableStateFlow(AlarmSettingUiState())
    val uiState: StateFlow<AlarmSettingUiState> get() = _uiState.asStateFlow()

    fun setActiveRemindState(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isOnActiveRemind = state)
            }
        }
    }

    fun setPostponePlanState(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isOnPostponePlan = state)
            }
        }
    }

    fun setRoutineGuideState(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isOnRoutineGuide = state)
            }
        }
    }

    fun setStreakSaveState(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isOnStreakSave = state)
            }
        }
    }

    fun setNightPushState(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isOnNightPush = state)
            }
        }
    }

    fun setPromotionConsentState(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isOnPromotionConsent = state)
            }
        }
    }
}