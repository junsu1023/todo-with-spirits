package com.example.todowithspirits.feature.login.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.LoginUseCase
import com.example.todowithspirits.feature.login.state.EmailLoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EmailLoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): BaseViewModel() {
    private val _uiState = MutableStateFlow(EmailLoginUiState())
    val uiState: StateFlow<EmailLoginUiState> get() = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            loginUseCase(_uiState.value.email, _uiState.value.password)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "login failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "로그인에 실패했습니다")
                }
        }
    }
}