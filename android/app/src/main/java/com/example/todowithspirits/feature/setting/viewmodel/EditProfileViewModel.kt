package com.example.todowithspirits.feature.setting.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.exception.FieldValidationException
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.domain.usecase.UpdateUserProfileUseCase
import com.example.todowithspirits.feature.setting.state.EditProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> get() = _uiState.asStateFlow()

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
