package com.example.todowithspirits.feature.signup.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.exception.FieldValidationException
import com.example.domain.usecase.CheckEmailAvailabilityUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.SendEmailVerificationUseCase
import com.example.domain.usecase.SignUpUseCase
import com.example.domain.usecase.UpdateUserProfileUseCase
import com.example.domain.usecase.VerifyEmailCodeUseCase
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
    private val checkEmailAvailabilityUseCase: CheckEmailAvailabilityUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
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

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        viewModelScope.launchWithLoading {
            checkEmailAvailabilityUseCase(state.email).onSuccess { availability ->
                Log.d(TAG, "checkEmail = $availability")

                if (availability.registered) {
                    val message = if (availability.provider != null) {
                        "이미 소셜 계정으로 가입된 이메일입니다. 소셜 로그인을 이용해주세요."
                    } else {
                        "이미 가입된 이메일입니다."
                    }
                    _uiState.update { it.copy(fieldErrors = mapOf("email" to message)) }
                    return@launchWithLoading
                }

                 sendVerificationEmail(state.email)
            }.onFailure { error ->
                Log.e(TAG, "checkEmail failed!", error)

                if (error is FieldValidationException) {
                    _uiState.update { it.copy(fieldErrors = error.fieldErrors) }
                } else {
                    emitErrorMsg(error.localizedMessage ?: "이메일 확인에 실패했습니다")
                }
            }
        }
    }

    private suspend fun sendVerificationEmail(email: String) {
        sendEmailVerificationUseCase(email).onSuccess {
            Log.d(TAG, "sendEmailVerification success")
            _uiState.update { it.copy(fieldErrors = emptyMap(), step = SignUpStep.EMAIL_VERIFICATION) }
            startVerificationTimer()
        }.onFailure { error ->
            Log.e(TAG, "sendEmailVerification failed!", error)

            if (error is FieldValidationException) {
                _uiState.update { it.copy(fieldErrors = error.fieldErrors) }
            } else {
                emitErrorMsg(error.localizedMessage ?: "인증 메일 발송에 실패했습니다")
            }
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
        val code = state.verificationCode.toIntOrNull()

        if (code == null) {
            _uiState.update { it.copy(fieldErrors = mapOf("verificationCode" to "인증번호를 다시 확인해주세요.")) }
            return
        }

        viewModelScope.launchWithLoading {
            verifyEmailCodeUseCase(state.email, code).onSuccess {
                Log.d(TAG, "verifyEmailCode success")
                cancelVerificationTimer()
                 performSignUp(
                     onSuccess = {
                         _uiState.update {
                             it.copy(fieldErrors = emptyMap(), step = SignUpStep.NICKNAME)
                         }
                     }
                 )
            }.onFailure { error ->
                Log.e(TAG, "verifyEmailCode failed!", error)

                if (error is FieldValidationException) {
                    _uiState.update { it.copy(fieldErrors = error.fieldErrors) }
                } else {
                    emitErrorMsg(error.localizedMessage ?: "인증번호가 일치하지 않습니다.")
                }
            }
        }
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

    private suspend fun performSignUp(onSuccess: () -> Unit) {
        val state = _uiState.value

        signUpUseCase(state.email, state.password, null).onSuccess {
            Log.d(TAG, "signUp success = $it")

            loginUseCase(state.email, state.password)
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    Log.e(TAG, "auto login after signUp failed!", error)
                    emitErrorMsg("회원가입은 완료되었지만 자동 로그인에 실패했습니다. 다시 로그인해주세요")
                }
        }.onFailure { error ->
            Log.e(TAG, "signUp failed!", error)

            if (error is FieldValidationException) {
                _uiState.update { it.copy(fieldErrors = error.fieldErrors, step = SignUpStep.CREDENTIALS) }
            } else {
                emitErrorMsg(error.localizedMessage ?: "회원가입에 실패했습니다")
            }
        }
    }

    // NICKNAME 단계(회원가입 마지막 화면)에서 확인 버튼 클릭 시 호출된다. 화면에 입력된 닉네임으로
    // 내 정보 수정 API를 호출해 서버에 반영한다.
    fun completeSignUp(onSuccess: () -> Unit) {
        val nickname = _uiState.value.nickname

        viewModelScope.launchWithLoading {
            updateUserProfileUseCase(nickname = nickname)
                .onSuccess {
                    Log.d(TAG, "updateUserProfile success = $it")
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "updateUserProfile failed!", error)

                    if (error is FieldValidationException) {
                        _uiState.update { it.copy(fieldErrors = error.fieldErrors) }
                    } else {
                        emitErrorMsg(error.localizedMessage ?: "닉네임 저장에 실패했습니다")
                    }
                }
        }
    }

    companion object {
        private const val VERIFICATION_TIME_LIMIT_SECONDS = 5 * 60
    }
}
