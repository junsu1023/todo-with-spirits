package com.example.todowithspirits.feature.setting.data.state

sealed interface DataSettingEffect {
    data object NavigateBack : DataSettingEffect
}
