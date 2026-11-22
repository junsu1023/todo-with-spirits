package com.example.todowithspirits.feature.signup.state

sealed interface SignUpEvent {
    // 상단 뒤로가기와 시스템 뒤로가기. 어디로 돌아갈지는 현재 단계에 따라 ViewModel이 정한다.
    data object BackClicked : SignUpEvent
    data class EmailChanged(val email: String) : SignUpEvent
    data class PasswordChanged(val password: String) : SignUpEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent
    data object CredentialsNextClicked : SignUpEvent
    data class VerificationCodeChanged(val code: String) : SignUpEvent
    data object VerifyClicked : SignUpEvent
    data object ResendClicked : SignUpEvent
    data class NicknameChanged(val nickname: String) : SignUpEvent
    data object NicknameConfirmClicked : SignUpEvent
}
