package com.example.todowithspirits.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.todowithspirits.feature.mypage.state.MyPageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> get() = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
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

    fun logout(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            logoutUseCase()
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "logout failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "로그아웃에 실패했습니다")
                }
        }
    }
}
