package com.example.todowithspirits.feature.setting.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AppLanguage
import com.example.domain.usecase.GetDisplaySettingUseCase
import com.example.domain.usecase.UpdateDisplaySettingUseCase
import com.example.todowithspirits.feature.setting.state.DisplaySettingUiState
import com.example.todowithspirits.feature.setting.state.toAppLanguage
import com.example.todowithspirits.feature.setting.state.toLanguageOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DisplaySettingViewModel @Inject constructor(
    private val getDisplaySettingUseCase: GetDisplaySettingUseCase,
    private val updateDisplaySettingUseCase: UpdateDisplaySettingUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(DisplaySettingUiState())
    val uiState: StateFlow<DisplaySettingUiState> get() = _uiState.asStateFlow()

    init {
        loadDisplaySetting()
    }

    fun loadDisplaySetting() {
        viewModelScope.launchWithLoading {
            getDisplaySettingUseCase()
                .onSuccess { setting ->
                    Log.d(TAG, "loadDisplaySetting = $setting")
                    _uiState.update {
                        it.copy(
                            isDarkMode = setting.darkMode,
                            isShowPlanDday = setting.ddayDisplayEnabled,
                            languageOption = setting.language.toLanguageOption()
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "loadDisplaySetting failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "디스플레이 설정을 불러오지 못했습니다")
                }
        }
    }

    fun setDarkMode(state: Boolean) {
        _uiState.update { it.copy(isDarkMode = state) }
        updateDisplaySetting(darkMode = state)
    }

    fun setShowPlanDday(state: Boolean) {
        _uiState.update { it.copy(isShowPlanDday = state) }
        updateDisplaySetting(ddayDisplayEnabled = state)
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
