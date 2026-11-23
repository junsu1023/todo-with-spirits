package com.example.todowithspirits.feature.setting.main.state

import androidx.compose.runtime.Immutable

@Immutable
data class MainSettingUiState(
    val nickname: String = "",
    val email: String? = null,
    val isPasswordChangeAvailable: Boolean = false
)
