package com.example.todowithspirits.feature.setting.changepw.state

sealed interface ChangePasswordEvent {
    data object BackClicked : ChangePasswordEvent
    data class CurrentPasswordChanged(val password: String) : ChangePasswordEvent
    data class NewPasswordChanged(val password: String) : ChangePasswordEvent
    data class ConfirmPasswordChanged(val password: String) : ChangePasswordEvent
    data object NextClicked : ChangePasswordEvent
}
