package com.example.todowithspirits.feature.setting.editprofile.state

sealed interface EditProfileEffect {
    data object NavigateBack : EditProfileEffect
}
