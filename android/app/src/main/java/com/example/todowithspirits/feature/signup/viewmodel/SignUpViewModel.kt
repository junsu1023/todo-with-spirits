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
import com.example.todowithspirits.util.isValidEmail
import com.example.todowithspirits.util.isValidPasswordFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> get() = _uiState.asStateFlow()

    private var verificationTimerJob: Job? = null

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

    fun setVerificationCode(code: String) {
        _uiState.update {
            it.copy(
                verificationCode = code,
                fieldErrors = it.fieldErrors - "verificationCode"
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

        val nextStep = if (errors.isEmpty()) SignUpStep.EMAIL_VERIFICATION else SignUpStep.CREDENTIALS

        _uiState.update {
            it.copy(
                fieldErrors = errors,
                step = nextStep
            )
        }

        if (nextStep == SignUpStep.EMAIL_VERIFICATION) {
            startVerificationTimer()
        }
    }

    fun goBackToCredentials() {
        cancelVerificationTimer()
        _uiState.update { it.copy(step = SignUpStep.CREDENTIALS, fieldErrors = emptyMap()) }
    }

    fun goBackToEmailVerification() {
        _uiState.update { it.copy(step = SignUpStep.EMAIL_VERIFICATION, fieldErrors = emptyMap()) }
    }

    fun verifyEmailCode() {
        val state = _uiState.value

        // TODO: 인증번호 서버 검증 로직이 구현되기 전까지 더미 코드(123456)로 성공 여부를 판단한다.
        if (state.verificationCode != DUMMY_VERIFICATION_CODE) {
            _uiState.update { it.copy(fieldErrors = mapOf("verificationCode" to "인증번호를 다시 확인해주세요.")) }
            emitErrorMsg("인증번호가 일치하지 않습니다.")
            return
        }

        cancelVerificationTimer()
        _uiState.update { it.copy(fieldErrors = emptyMap(), step = SignUpStep.NICKNAME) }

//        signUp()
    }

    private fun startVerificationTimer() {
        verificationTimerJob?.cancel()

        verificationTimerJob = viewModelScope.launch {
            val totalTimeMillis = VERIFICATION_TIME_LIMIT_SECONDS * 1000L
            val endTimeMillis = System.currentTimeMillis() + totalTimeMillis

            while (true) {
                val remaining = (endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                val remainingSeconds = ((remaining + 999L) / 1000L).toInt()

                _uiState.update { it.copy(verificationRemainingSeconds = remainingSeconds) }

                if (remaining <= 0L) {
                    handleVerificationTimeout()
                    break
                }

                val untilNextSecond = remaining % 1000L
                delay((if (untilNextSecond == 0L) 1000L else untilNextSecond).milliseconds)
            }
        }
    }

    private fun cancelVerificationTimer() {
        verificationTimerJob?.cancel()
        verificationTimerJob = null
    }

    private fun handleVerificationTimeout() {
        verificationTimerJob = null
        _uiState.update {
            it.copy(
                step = SignUpStep.CREDENTIALS,
                fieldErrors = emptyMap(),
                verificationCode = ""
            )
        }

        emitErrorMsg("인증 시간이 초과되었습니다. 다시 시도해주세요.")
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        val state = _uiState.value

        viewModelScope.launchWithLoading {
            signUpUseCase(state.email, state.password, null)
                .onSuccess {
                    Log.d(TAG, "signUp success = $it")

                    loginUseCase(state.email, state.password)
                        .onSuccess { _uiState.update { it.copy(step = SignUpStep.NICKNAME) } }
                        .onFailure { error ->
                            Log.e(TAG, "auto login after signUp failed!", error)
                            emitErrorMsg("회원가입은 완료되었지만 자동 로그인에 실패했습니다. 다시 로그인해주세요")
                        }
                }
                .onFailure { error ->
                    Log.e(TAG, "signUp failed!", error)

                    if (error is FieldValidationException) {
                        _uiState.update { it.copy(fieldErrors = error.fieldErrors, step = SignUpStep.CREDENTIALS) }
                    } else {
                        emitErrorMsg(error.localizedMessage ?: "회원가입에 실패했습니다")
                    }
                }
        }
    }

    companion object {
        private const val DUMMY_VERIFICATION_CODE = "123456"
        private const val VERIFICATION_TIME_LIMIT_SECONDS = 5 * 60
    }
}
