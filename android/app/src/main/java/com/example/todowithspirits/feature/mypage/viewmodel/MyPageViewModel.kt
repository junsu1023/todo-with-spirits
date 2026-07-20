package com.example.todowithspirits.feature.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel() {
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
