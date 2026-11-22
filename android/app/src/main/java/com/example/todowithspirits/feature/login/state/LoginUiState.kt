package com.example.todowithspirits.feature.login.state

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    // 소셜 SDK 인증 시작부터 서버 로그인이 끝날 때까지
    val isAuthenticating: Boolean = false
)
