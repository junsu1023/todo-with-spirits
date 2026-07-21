package com.example.todowithspirits.feature.login.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> get() = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password.asStateFlow()

    fun setEmail(value: String) {
        _email.update { value }
    }

    fun setPassword(value: String) {
        _password.update { value }
    }

    fun login(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            loginUseCase(_email.value, _password.value)
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
