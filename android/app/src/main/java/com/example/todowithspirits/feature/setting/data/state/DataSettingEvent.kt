package com.example.todowithspirits.feature.setting.data.state

sealed interface DataSettingEvent {
    data object BackClicked : DataSettingEvent
    data object ConfirmClicked : DataSettingEvent
    data object BackupClicked : DataSettingEvent
    data object SyncClicked : DataSettingEvent
    data object ExportDataClicked : DataSettingEvent
}
