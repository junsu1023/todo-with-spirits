package com.example.todowithspirits.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.domain.usecase.WithdrawUseCase
import com.example.todowithspirits.feature.mypage.state.WithdrawUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val withdrawUseCase: WithdrawUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState: StateFlow<WithdrawUiState> get() = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launchWithLoading {
            getUserProfileUseCase()
                .onSuccess { profile ->
                    Log.d(TAG, "getUserProfile success = $profile")
                    _uiState.update { it.copy(nickname = profile.nickname) }
                }
                .onFailure {
                    Log.e(TAG, "getUserProfile failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "내 정보를 불러오지 못했습니다")
                }
        }
    }

    fun withdraw(onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            withdrawUseCase()
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "withdraw failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "회원탈퇴에 실패했습니다")
                }
        }
    }
}
