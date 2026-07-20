package com.example.todowithspirits.feature.splash.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.auth.TokenHolder
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// dummy data, Login Flow 생성되면 지울 예정
private const val TEST_EMAIL = "test@example.com"
private const val TEST_PASSWORD = "password1"

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    private val _loginCompleted = MutableStateFlow(false)
    val loginCompleted: StateFlow<Boolean> get() = _loginCompleted.asStateFlow()

    init {
        login()
    }

    private fun login() {
        viewModelScope.launchWithLoading {
            try {
                loginUseCase(TEST_EMAIL, TEST_PASSWORD)
            } finally {
                TokenHolder.markBootstrapCompleted()
                _loginCompleted.update { true }
            }
        }
    }
}
