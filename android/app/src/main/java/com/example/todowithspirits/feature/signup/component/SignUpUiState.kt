package com.example.todowithspirits.feature.signup.component

import com.example.todowithspirits.feature.signup.SignUpStep

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val step: SignUpStep = SignUpStep.CREDENTIALS
)
