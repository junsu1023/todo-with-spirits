package com.example.todowithspirits.feature.setting.editprofile.state

sealed interface EditProfileEvent {
    data object BackClicked : EditProfileEvent
    data class NicknameChanged(val nickname: String) : EditProfileEvent
    data object ConfirmClicked : EditProfileEvent
}
