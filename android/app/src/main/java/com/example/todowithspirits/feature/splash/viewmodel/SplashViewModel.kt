package com.example.todowithspirits.feature.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.auth.TokenHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val _bootstrapCompleted = MutableStateFlow(false)
    val bootstrapCompleted: StateFlow<Boolean> get() = _bootstrapCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            // 저장된 세션이 없으므로 로그인 화면으로 보내기 위해 부트스트랩만 완료 처리한다
            TokenHolder.markBootstrapCompleted()
            _bootstrapCompleted.update { true }
        }
    }
}
