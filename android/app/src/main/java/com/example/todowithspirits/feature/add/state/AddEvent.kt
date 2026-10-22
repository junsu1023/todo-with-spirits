package com.example.todowithspirits.feature.add.state

import com.example.domain.model.AlarmOption
import com.example.domain.model.RepeatOption
import java.time.LocalDate
import java.time.LocalTime

sealed interface AddEvent {
    data object BackClicked : AddEvent
    data class TitleChanged(val title: String) : AddEvent
    data class ImportantChanged(val isImportant: Boolean) : AddEvent
    data class DateChanged(val date: LocalDate) : AddEvent
    data class TimeEnabledChanged(val isEnabled: Boolean) : AddEvent
    data class DueTimeChanged(val time: LocalTime) : AddEvent
    data class RepeatOptionChanged(val option: RepeatOption) : AddEvent
    data class AlarmOptionChanged(val option: AlarmOption) : AddEvent
    data object ConfirmClicked : AddEvent
}
