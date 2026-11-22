package com.example.todowithspirits.feature.login.state

import androidx.compose.runtime.Immutable

@Immutable
data class EmailLoginUiState(
    val email: String = "",
    val password: String = "",
    // null이 아니면 로그인 실패 팝업을 띄운다.
    val errorMessage: String? = null
) {
    val canLogin: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}
