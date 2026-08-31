package com.example.todowithspirits.feature.login.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.SocialProvider
import com.example.domain.usecase.SocialLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase
) : BaseViewModel() {
    fun socialLogin(
        provider: SocialProvider,
        providerUserId: String,
        providerAccessToken: String,
        email: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launchWithLoading {
            socialLoginUseCase(provider, providerUserId, providerAccessToken, email)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "socialLogin failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "소셜 로그인에 실패했습니다")
                }
        }
    }
}
