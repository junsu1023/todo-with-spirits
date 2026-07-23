package com.example.todowithspirits.feature.setting.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.setting.state.DisplaySettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DisplaySettingViewModel @Inject constructor(): BaseViewModel() {
    private val _uiState = MutableStateFlow(DisplaySettingUiState())
    val uiState: StateFlow<DisplaySettingUiState> get() = _uiState.asStateFlow()

    fun setDarkMode(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isDarkMode = state)
            }
        }
    }

    fun setShowPlanDday(state: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isShowPlanDday = state)
            }
        }
    }

    fun setPlanSortOption(option: String) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(planSortOption = option)
            }
        }
    }

    fun setThemeOption(option: String) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(themeOption = option)
            }
        }
    }

    fun setLanguageOption(option: String) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(languageOption = option)
            }
        }
    }
}
