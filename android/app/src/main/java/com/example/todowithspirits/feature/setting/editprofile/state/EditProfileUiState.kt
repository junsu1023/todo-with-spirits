package com.example.todowithspirits.feature.setting.editprofile.state

import androidx.compose.runtime.Immutable

@Immutable
data class EditProfileUiState(
    // 입력 중인 닉네임. 처음에는 현재 닉네임으로 채운다.
    val nickname: String = ""
)
