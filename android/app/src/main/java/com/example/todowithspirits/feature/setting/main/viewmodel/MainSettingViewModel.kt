package com.example.todowithspirits.feature.setting.main.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.LoginMethod
import com.example.domain.usecase.GetLoginMethodUseCase
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.todowithspirits.feature.setting.main.state.MainSettingEffect
import com.example.todowithspirits.feature.setting.main.state.MainSettingEvent
import com.example.todowithspirits.feature.setting.main.state.MainSettingUiState
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
class MainSettingViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    getLoginMethodUseCase: GetLoginMethodUseCase
) : BaseViewModel() {
    // 이메일 로그인일 때만 비밀번호 변경 노출 (소셜 로그인은 비밀번호가 없음)
    private val _uiState = MutableStateFlow(
        MainSettingUiState(isPasswordChangeAvailable = getLoginMethodUseCase() == LoginMethod.EMAIL)
    )
    val uiState: StateFlow<MainSettingUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<MainSettingEffect>(Channel.BUFFERED)
    val effect: Flow<MainSettingEffect> = _effect.receiveAsFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: MainSettingEvent) {
        when (event) {
            MainSettingEvent.BackClicked -> sendEffect(MainSettingEffect.NavigateBack)
            is MainSettingEvent.MenuClicked -> sendEffect(MainSettingEffect.NavigateToMenu(event.menu))
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launchWithLoading {
            getUserProfileUseCase()
                .onSuccess { profile ->
                    Log.d(TAG, "getUserProfile success = $profile")
                    _uiState.update { it.copy(nickname = profile.nickname, email = profile.email) }
                }
                .onFailure {
                    Log.e(TAG, "getUserProfile failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "내 정보를 불러오지 못했습니다")
                }
        }
    }

    private fun sendEffect(effect: MainSettingEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
