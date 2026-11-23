package com.example.todowithspirits.feature.setting.changepw.state

import androidx.compose.runtime.Immutable

@Immutable
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    // key: "currentPassword", "newPassword", "confirmPassword"
    val fieldErrors: Map<String, String> = emptyMap()
) {
    val canProceed: Boolean
        get() = currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                fieldErrors.isEmpty()
}
