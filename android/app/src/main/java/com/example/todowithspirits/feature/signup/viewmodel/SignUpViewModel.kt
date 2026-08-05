package com.example.todowithspirits.feature.signup.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.exception.FieldValidationException
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.SignUpUseCase
import com.example.todowithspirits.feature.signup.SignUpStep
import com.example.todowithspirits.feature.signup.component.SignUpUiState
import com.example.todowithspirits.feature.signup.isValidEmail
import com.example.todowithspirits.feature.signup.isValidPasswordFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> get() = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                fieldErrors = it.fieldErrors - "email"
            )
        }
    }

    fun setPassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                fieldErrors = it.fieldErrors - "password" - "confirmPassword"
            )
        }
    }

    fun setConfirmPassword(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                fieldErrors = it.fieldErrors - "confirmPassword"
            )
        }
    }

    fun setNickname(nickname: String) {
        _uiState.update {
            it.copy(
                nickname = nickname,
                fieldErrors = it.fieldErrors - "nickname"
            )
        }
    }

    fun validateCredentials() {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (!isValidEmail(state.email)) {
            errors["email"] = "유효한 이메일을 입력해주세요."
        }

        if (!isValidPasswordFormat(state.password)) {
            errors["password"] = "영문/숫자/특수문자 중 2가지 포함 20자 내"
        }

        if (state.confirmPassword.isEmpty() || state.confirmPassword != state.password) {
            errors["confirmPassword"] = "비밀번호가 일치하지 않습니다."
        }

        _uiState.update {
            it.copy(
                fieldErrors = errors,
                step = if (errors.isEmpty()) SignUpStep.NICKNAME else SignUpStep.CREDENTIALS
            )
        }
    }

    fun goBackToCredentials() {
        _uiState.update { it.copy(step = SignUpStep.CREDENTIALS, fieldErrors = emptyMap()) }
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        _uiState.update { it.copy(fieldErrors = emptyMap()) }

        viewModelScope.launchWithLoading {
            val state = _uiState.value

            signUpUseCase(state.email, state.password, state.nickname.ifBlank { null })
                .onSuccess {
                    Log.d(TAG, "signUp success = $it")

                    loginUseCase(state.email, state.password)
                        .onSuccess { onSuccess() }
                        .onFailure { error ->
                            Log.e(TAG, "auto login after signUp failed!", error)
                            emitErrorMsg("회원가입은 완료되었지만 자동 로그인에 실패했습니다. 다시 로그인해주세요")
                        }
                }
                .onFailure { error ->
                    Log.e(TAG, "signUp failed!", error)

                    if (error is FieldValidationException) {
                        _uiState.update { it.copy(fieldErrors = error.fieldErrors) }
                    } else {
                        emitErrorMsg(error.localizedMessage ?: "회원가입에 실패했습니다")
                    }
                }
        }
    }
}
