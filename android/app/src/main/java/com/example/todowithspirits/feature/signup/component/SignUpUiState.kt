package com.example.todowithspirits.feature.signup.component

import com.example.todowithspirits.feature.signup.SignUpStep

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val verificationCode: String = "",
    val verificationRemainingSeconds: Int = 0,
    val nickname: String = "행복한돼지123",
    val fieldErrors: Map<String, String> = emptyMap(),
    val step: SignUpStep = SignUpStep.CREDENTIALS
) {
    val canProceedCredentials: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                fieldErrors.isEmpty()

    val canVerifyCode: Boolean
        get() = verificationCode.isNotBlank()
}
