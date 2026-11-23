package com.example.todowithspirits.feature.setting.changepw.state

sealed interface ChangePasswordEffect {
    data object NavigateBack : ChangePasswordEffect
}
