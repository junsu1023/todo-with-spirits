package com.example.todowithspirits.feature.login.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.SocialProvider
import com.example.domain.usecase.SocialLoginUseCase
import com.example.todowithspirits.feature.login.SocialAuthUser
import com.example.todowithspirits.feature.login.state.LoginEffect
import com.example.todowithspirits.feature.login.state.LoginEvent
import com.example.todowithspirits.feature.login.state.LoginUiState
import com.example.todowithspirits.widget.TodoWidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
class LoginViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase,
    private val todoWidgetRepository: TodoWidgetRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.KakaoLoginClicked -> startSocialAuth(SocialProvider.KAKAO)
            LoginEvent.GoogleLoginClicked -> startSocialAuth(SocialProvider.GOOGLE)
            LoginEvent.EmailLoginClicked -> sendEffect(LoginEffect.NavigateToEmailLogin)
            LoginEvent.SignUpClicked -> sendEffect(LoginEffect.NavigateToSignUp)
            is LoginEvent.SocialAuthFinished -> handleSocialAuthResult(event.provider, event.result)
        }
    }

    // LoadingOverlay가 그려지기 전에 시작된 터치(두 버튼 동시 누름)는 막히지 않으므로 중복 인증만 방지한다.
    private fun startSocialAuth(provider: SocialProvider) {
        if (_uiState.value.isAuthenticating) return

        _uiState.update { it.copy(isAuthenticating = true) }
        sendEffect(LoginEffect.StartSocialAuth(provider))
    }

    private fun handleSocialAuthResult(provider: SocialProvider, result: Result<SocialAuthUser>) {
        result
            .onSuccess { socialLogin(provider, it) }
            .onFailure {
                finishAuthenticating()
                // 사용자가 취소했거나 화면이 사라진 경우는 오류로 알리지 않는다.
                if (it is CancellationException) return

                emitErrorMsg(it.localizedMessage ?: provider.failureMessage())
            }
    }

    private fun socialLogin(provider: SocialProvider, user: SocialAuthUser) {
        viewModelScope.launchWithLoading {
            socialLoginUseCase(provider, user.providerUserId, user.providerAccessToken, user.email)
                .onSuccess {
                    todoWidgetRepository.requestUpdate()
                    sendEffect(LoginEffect.NavigateToHome)
                }
                .onFailure {
                    Log.e(TAG, "socialLogin failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "소셜 로그인에 실패했습니다")
                }
        }.invokeOnCompletion { finishAuthenticating() }
    }

    private fun finishAuthenticating() {
        _uiState.update { it.copy(isAuthenticating = false) }
    }

    private fun sendEffect(effect: LoginEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

private fun SocialProvider.failureMessage(): String = when (this) {
    SocialProvider.KAKAO -> "카카오 로그인에 실패했습니다"
    SocialProvider.GOOGLE -> "구글 로그인에 실패했습니다"
}
