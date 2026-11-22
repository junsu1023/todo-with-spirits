package com.example.todowithspirits.feature.login.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.LoginUseCase
import com.example.todowithspirits.feature.login.state.EmailLoginEffect
import com.example.todowithspirits.feature.login.state.EmailLoginEvent
import com.example.todowithspirits.feature.login.state.EmailLoginUiState
import com.example.todowithspirits.widget.TodoWidgetRepository
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
class EmailLoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val todoWidgetRepository: TodoWidgetRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(EmailLoginUiState())
    val uiState: StateFlow<EmailLoginUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<EmailLoginEffect>(Channel.BUFFERED)
    val effect: Flow<EmailLoginEffect> = _effect.receiveAsFlow()

    init {
        // launchWithLoading이 잡은 예외까지 포함해 모든 오류를 실패 팝업으로 보여준다.
        viewModelScope.launch {
            errorMsg.collect { message -> _uiState.update { it.copy(errorMessage = message) } }
        }
    }

    fun onEvent(event: EmailLoginEvent) {
        when (event) {
            EmailLoginEvent.BackClicked -> sendEffect(EmailLoginEffect.NavigateBack)
            is EmailLoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.email) }
            is EmailLoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password) }
            EmailLoginEvent.LoginClicked -> login()
            EmailLoginEvent.FindPasswordClicked -> sendEffect(EmailLoginEffect.NavigateToFindPassword)
            EmailLoginEvent.ErrorDialogDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun login() {
        viewModelScope.launchWithLoading {
            loginUseCase(_uiState.value.email, _uiState.value.password)
                .onSuccess {
                    todoWidgetRepository.requestUpdate()
                    sendEffect(EmailLoginEffect.NavigateToHome)
                }
                .onFailure {
                    Log.e(TAG, "login failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "로그인에 실패했습니다")
                }
        }
    }

    private fun sendEffect(effect: EmailLoginEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
