package com.example.todowithspirits.feature.setting.display.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AppLanguage
import com.example.domain.usecase.GetDisplaySettingUseCase
import com.example.domain.usecase.UpdateDisplaySettingUseCase
import com.example.todowithspirits.feature.setting.display.state.DisplaySettingEffect
import com.example.todowithspirits.feature.setting.display.state.DisplaySettingEvent
import com.example.todowithspirits.feature.setting.display.state.DisplaySettingUiState
import com.example.todowithspirits.feature.setting.display.state.toAppLanguage
import com.example.todowithspirits.feature.setting.display.state.toLanguageOption
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
class DisplaySettingViewModel @Inject constructor(
    private val getDisplaySettingUseCase: GetDisplaySettingUseCase,
    private val updateDisplaySettingUseCase: UpdateDisplaySettingUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(DisplaySettingUiState())
    val uiState: StateFlow<DisplaySettingUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<DisplaySettingEffect>(Channel.BUFFERED)
    val effect: Flow<DisplaySettingEffect> = _effect.receiveAsFlow()

    init {
        loadDisplaySetting()
    }

    fun onEvent(event: DisplaySettingEvent) {
        when (event) {
            DisplaySettingEvent.BackClicked,
            DisplaySettingEvent.ConfirmClicked -> sendEffect(DisplaySettingEffect.NavigateBack)
            is DisplaySettingEvent.DarkModeToggled -> setDarkMode(event.isOn)
            is DisplaySettingEvent.PlanDdayToggled -> setShowPlanDday(event.isOn)
            is DisplaySettingEvent.LanguageSelected -> setLanguageOption(event.option)
        }
    }

    private fun loadDisplaySetting() {
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

    // 바뀐 값은 바로 반영하고 그 항목만 서버에 보낸다. 실패하면 원래 값으로 되돌린다.
    // 연속으로 바꿀 수 있도록 로딩 표시는 하지 않는다.
    private fun setDarkMode(isOn: Boolean) {
        _uiState.update { it.copy(isDarkMode = isOn) }
        updateDisplaySetting(darkMode = isOn) {
            _uiState.update { it.copy(isDarkMode = !isOn) }
        }
    }

    private fun setShowPlanDday(isOn: Boolean) {
        _uiState.update { it.copy(isShowPlanDday = isOn) }
        updateDisplaySetting(ddayDisplayEnabled = isOn) {
            _uiState.update { it.copy(isShowPlanDday = !isOn) }
        }
    }

    private fun setLanguageOption(option: String) {
        val previous = _uiState.value.languageOption
        _uiState.update { it.copy(languageOption = option) }
        updateDisplaySetting(language = option.toAppLanguage()) {
            _uiState.update { it.copy(languageOption = previous) }
        }
    }

    private fun updateDisplaySetting(
        darkMode: Boolean? = null,
        ddayDisplayEnabled: Boolean? = null,
        language: AppLanguage? = null,
        revert: () -> Unit
    ) {
        viewModelScope.launch {
            updateDisplaySettingUseCase(darkMode, ddayDisplayEnabled, language)
                .onFailure {
                    Log.e(TAG, "updateDisplaySetting failed!", it)
                    revert()
                    emitErrorMsg(it.localizedMessage ?: "디스플레이 설정을 저장하지 못했습니다")
                }
        }
    }

    private fun sendEffect(effect: DisplaySettingEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
