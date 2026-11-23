package com.example.todowithspirits.feature.setting.changepw.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordEffect
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordEvent
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordUiState
import com.example.todowithspirits.util.isValidPasswordFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor() : BaseViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<ChangePasswordEffect>(Channel.BUFFERED)
    val effect: Flow<ChangePasswordEffect> = _effect.receiveAsFlow()

    fun onEvent(event: ChangePasswordEvent) {
        when (event) {
            ChangePasswordEvent.BackClicked -> sendEffect(ChangePasswordEffect.NavigateBack)
            is ChangePasswordEvent.CurrentPasswordChanged -> setCurrentPassword(event.password)
            is ChangePasswordEvent.NewPasswordChanged -> setNewPassword(event.password)
            is ChangePasswordEvent.ConfirmPasswordChanged -> setConfirmPassword(event.password)
            ChangePasswordEvent.NextClicked -> changePassword()
        }
    }

    private fun setCurrentPassword(password: String) {
        _uiState.update {
            it.copy(currentPassword = password, fieldErrors = it.fieldErrors - "currentPassword")
        }
    }

    private fun setNewPassword(password: String) {
        _uiState.update {
            it.copy(newPassword = password, fieldErrors = it.fieldErrors - "newPassword" - "confirmPassword")
        }
    }

    private fun setConfirmPassword(password: String) {
        _uiState.update {
            it.copy(confirmPassword = password, fieldErrors = it.fieldErrors - "confirmPassword")
        }
    }

    private fun changePassword() {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (!isValidPasswordFormat(state.newPassword)) {
            errors["newPassword"] = "영문/숫자/특수문자 중 2가지 포함 20자 내"
        }

        if (state.confirmPassword.isEmpty() || state.confirmPassword != state.newPassword) {
            errors["confirmPassword"] = "비밀번호가 일치하지 않습니다."
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        // TODO 비밀번호 변경 API가 생기면 연결 (현재 앱/서버 모두 없음). 성공 시 NavigateBack.
    }

    private fun sendEffect(effect: ChangePasswordEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
