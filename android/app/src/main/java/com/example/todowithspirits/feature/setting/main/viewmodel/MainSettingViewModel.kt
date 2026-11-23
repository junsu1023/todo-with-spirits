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

    // 첫 진입 때는 init에서 이미 불러오므로 resume 갱신을 건너뛴다.
    private var isFirstResume = true

    init {
        loadUserProfile()
    }

    fun onEvent(event: MainSettingEvent) {
        when (event) {
            MainSettingEvent.ScreenResumed -> refreshUserProfile()
            MainSettingEvent.BackClicked -> sendEffect(MainSettingEffect.NavigateBack)
            is MainSettingEvent.MenuClicked -> sendEffect(MainSettingEffect.NavigateToMenu(event.menu))
        }
    }

    // 닉네임 수정 등 다른 화면에서 바뀐 프로필을 반영한다. 이미 화면에 값이 있으니 로딩 없이 조용히 갱신하고,
    // 실패해도 기존 값을 그대로 둔다.
    private fun refreshUserProfile() {
        if (isFirstResume) {
            isFirstResume = false
            return
        }

        viewModelScope.launch {
            getUserProfileUseCase()
                .onSuccess { profile ->
                    _uiState.update { it.copy(nickname = profile.nickname, email = profile.email) }
                }
                .onFailure { Log.e(TAG, "refreshUserProfile failed!", it) }
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
