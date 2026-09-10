package com.example.todowithspirits.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val withdrawUseCase: WithdrawUseCase
) : BaseViewModel() {
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
