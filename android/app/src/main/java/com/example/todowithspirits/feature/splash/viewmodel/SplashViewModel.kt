package com.example.todowithspirits.feature.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.auth.TokenHolder
import com.example.domain.usecase.RestoreSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val restoreSessionUseCase: RestoreSessionUseCase
) : ViewModel() {
    // null: 아직 판단 중, true/false: 세션 복원 여부(로그인 유지 상태)가 확정됨
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> get() = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            // 기기에 저장된 세션(이메일 로그인/차후 소셜 로그인 공통)이 있으면 자동 로그인 처리
            val restored = restoreSessionUseCase()
            TokenHolder.markBootstrapCompleted()
            _isLoggedIn.update { restored }
        }
    }
}
