package com.example.todowithspirits.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.LoginMethod
import com.example.domain.usecase.GetLoginMethodUseCase
import com.example.domain.usecase.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val withdrawUseCase: WithdrawUseCase,
    getLoginMethodUseCase: GetLoginMethodUseCase
) : BaseViewModel() {
    // 이메일 로그인일 때만 비밀번호 수정 노출 (소셜 로그인은 비밀번호가 없음)
    private val _isPasswordChangeAvailable = MutableStateFlow(
        getLoginMethodUseCase() == LoginMethod.EMAIL
    )
    val isPasswordChangeAvailable: StateFlow<Boolean> = _isPasswordChangeAvailable.asStateFlow()

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
