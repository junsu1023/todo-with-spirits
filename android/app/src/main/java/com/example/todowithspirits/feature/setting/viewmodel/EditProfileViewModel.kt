package com.example.todowithspirits.feature.setting.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.exception.FieldValidationException
import com.example.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : BaseViewModel() {
    fun updateNickname(nickname: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            updateUserProfileUseCase(nickname = nickname)
                .onSuccess {
                    Log.d(TAG, "updateUserProfile success = $it")
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "updateUserProfile failed!", error)

                    val message = if (error is FieldValidationException) {
                        error.fieldErrors["nickname"] ?: error.message
                    } else {
                        error.localizedMessage
                    }

                    emitErrorMsg(message ?: "닉네임 변경에 실패했습니다")
                }
        }
    }
}
