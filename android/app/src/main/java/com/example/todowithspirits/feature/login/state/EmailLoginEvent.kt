package com.example.todowithspirits.feature.login.state

sealed interface EmailLoginEvent {
    data object BackClicked : EmailLoginEvent
    data class EmailChanged(val email: String) : EmailLoginEvent
    data class PasswordChanged(val password: String) : EmailLoginEvent
    data object LoginClicked : EmailLoginEvent
    data object FindPasswordClicked : EmailLoginEvent
    data object ErrorDialogDismissed : EmailLoginEvent
}
