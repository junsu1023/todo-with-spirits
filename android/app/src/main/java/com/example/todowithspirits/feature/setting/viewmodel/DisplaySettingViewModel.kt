package com.example.todowithspirits.feature.setting.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AppLanguage
import com.example.domain.usecase.UpdateDisplaySettingUseCase
import com.example.todowithspirits.feature.setting.state.DisplaySettingUiState
import com.example.todowithspirits.feature.setting.state.toAppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DisplaySettingViewModel @Inject constructor(
    private val updateDisplaySettingUseCase: UpdateDisplaySettingUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(DisplaySettingUiState())
    val uiState: StateFlow<DisplaySettingUiState> get() = _uiState.asStateFlow()

    fun setDarkMode(state: Boolean) {
        _uiState.update { it.copy(isDarkMode = state) }
        updateDisplaySetting(darkMode = state)
    }

    fun setShowPlanDday(state: Boolean) {
        _uiState.update { it.copy(isShowPlanDday = state) }
        updateDisplaySetting(ddayDisplayEnabled = state)
    }

    fun setPlanSortOption(option: String) {
        _uiState.update { it.copy(planSortOption = option) }
    }

    fun setThemeOption(option: String) {
        _uiState.update { it.copy(themeOption = option) }
    }

    fun setLanguageOption(option: String) {
        _uiState.update { it.copy(languageOption = option) }
        updateDisplaySetting(language = option.toAppLanguage())
    }

    private fun updateDisplaySetting(
        darkMode: Boolean? = null,
        ddayDisplayEnabled: Boolean? = null,
        language: AppLanguage? = null
    ) {
        viewModelScope.launchWithLoading {
            updateDisplaySettingUseCase(darkMode, ddayDisplayEnabled, language)
                .onFailure {
                    Log.e(TAG, "updateDisplaySetting failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "디스플레이 설정을 저장하지 못했습니다")
                }
        }
    }
}
