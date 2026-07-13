package com.example.todowithspirits.feature.add.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AlarmOption
import com.example.domain.model.CategoryOption
import com.example.domain.model.NewRoutine
import com.example.domain.model.PublicStateOption
import com.example.domain.model.RepeatOption
import com.example.domain.usecase.CreateRoutineUseCase
import com.example.todowithspirits.feature.add.state.AddUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val createRoutineUseCase: CreateRoutineUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> get() = _uiState.asStateFlow()

    fun setTitle(title: String) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(title = title) }
        }
    }

    fun setImportant(important: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(isImportant = important) }
        }
    }

    fun setDate(date: LocalDate) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(date = date) }
        }
    }

    fun setTimeEnabled(enabled: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(isTimeEnabled = enabled) }
        }
    }

    fun setDueTime(time: LocalTime) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(dueTime = time) }
        }
    }

    fun setRepeatOption(option: RepeatOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(repeatOption = option) }
        }
    }

    fun toggleWeekDay(day: DayOfWeek) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(
                    selectedWeekDays = it.selectedWeekDays.toMutableSet().apply {
                        if (day in this) remove(day) else add(day)
                    }
                )
            }
        }
    }

    fun toggleMonthDay(day: Int) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(
                    selectedMonthDays = it.selectedMonthDays.toMutableSet().apply {
                        if (day in this) remove(day) else add(day)
                    }
                )
            }
        }
    }

    fun setExcludeHolidays(exclude: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(excludeHolidays = exclude) }
        }
    }

    fun setAlarmOption(option: AlarmOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(alarmOption = option) }
        }
    }

    fun setCategoryOption(option: CategoryOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(categoryOption = option) }
        }
    }

    fun setPublicOption(option: PublicStateOption) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(publicOption = option) }
        }
    }

    fun setMemo(memo: String) {
        viewModelScope.launchWithLoading {
            _uiState.update { it.copy(memo = memo) }
        }
    }

    fun registerRoutine() {
        viewModelScope.launchWithLoading {
            val state = _uiState.value

            val routine = NewRoutine(
                title = state.title,
                repeatType = state.repeatOption,
                repeatDaysOfWeek = state.selectedWeekDays.toList(),
                repeatDaysOfMonth = state.selectedMonthDays.toList(),
                notification = state.alarmOption,
                isPublic = state.publicOption == PublicStateOption.PUBLIC,
                memo = state.memo.ifBlank { null }
            )

            createRoutineUseCase(routine)
                .onSuccess {
                    Log.d(TAG, "registerRoutine success = $it")
                    _uiState.update { AddUiState() }
                }
                .onFailure {
                    Log.e(TAG, "registerRoutine failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "루틴 생성에 실패했습니다")
                }
        }
    }
}
