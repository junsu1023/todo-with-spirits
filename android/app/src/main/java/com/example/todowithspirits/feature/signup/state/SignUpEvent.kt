package com.example.todowithspirits.feature.signup.state

sealed interface SignUpEvent {
    data object BackClicked : SignUpEvent
    data class EmailChanged(val email: String) : SignUpEvent
    data class PasswordChanged(val password: String) : SignUpEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent
    data object CredentialsNextClicked : SignUpEvent
}
