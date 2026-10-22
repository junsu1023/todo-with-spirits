package com.example.todowithspirits.feature.add.state

import androidx.compose.runtime.Immutable
import com.example.domain.model.AlarmOption
import com.example.domain.model.RepeatOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class AddUiState(
    val editingTaskId: Long? = null,

    val title: String = "",
    val isImportant: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val isTimeEnabled: Boolean = false,
    val dueTime: LocalTime = LocalTime.of(0, 0, 0),
    // 일정 API에 반복 필드가 없어 화면에서 고른 값(반복, 요일, 날짜)만 보관한다. (서버 지원 시 연결)
    val repeatOption: RepeatOption = RepeatOption.NONE,
    val selectedWeekDays: Set<DayOfWeek> = emptySet(),
    val selectedMonthDays: Set<Int> = emptySet(),
    val alarmOption: AlarmOption = AlarmOption.NONE,

    // 화면에서 편집하지 않지만, 수정할 때 기존 값이 지워지지 않도록 불러온 값을 그대로 보낸다.
    val isPublic: Boolean = false,
    val memo: String = ""
) {
    val isEditMode: Boolean get() = editingTaskId != null
    val canConfirm: Boolean get() = title.isNotBlank()
}
