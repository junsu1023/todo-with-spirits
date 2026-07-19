package com.example.todowithspirits.feature.today.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.AlarmOption
import com.example.domain.model.CategoryOption
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.RepeatOption
import com.example.domain.usecase.CreateRoutineUseCase
import com.example.domain.usecase.CreateTodoUseCase
import com.example.todowithspirits.feature.add.viewmodel.concatenating
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val createTodoUseCase: CreateTodoUseCase,
    private val createRoutineUseCase: CreateRoutineUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    fun createTodo(
        title: String,
        isImportant: Boolean,
        date: LocalDate,
        isTimeEnabled: Boolean,
        dueTime: LocalTime,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launchWithLoading {
            val todo = NewTodo(
                title = title,
                isAllDay = !isTimeEnabled,
                endDateTime = Pair(date, dueTime).concatenating(),
                isImportant = isImportant,
                notificationType = AlarmOption.NONE,
                category = CategoryOption.NONE,
                isPublic = false,
                memo = null
            )

            createTodoUseCase(todo)
                .onSuccess {
                    Log.d(TAG, "quickAdd createTodo success = $it")
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "quickAdd createTodo failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "Todo 생성에 실패했습니다")
                }
        }
    }

    fun createRoutine(
        title: String,
        repeatOption: RepeatOption,
        selectedWeekDays: Set<DayOfWeek>,
        selectedMonthDays: Set<Int>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launchWithLoading {
            val routine = NewRoutine(
                title = title,
                repeatType = repeatOption,
                repeatDaysOfWeek = selectedWeekDays.toList(),
                repeatDaysOfMonth = selectedMonthDays.toList(),
                notification = AlarmOption.NONE,
                isPublic = false,
                memo = null
            )

            createRoutineUseCase(routine)
                .onSuccess {
                    Log.d(TAG, "quickAdd createRoutine success = $it")
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "quickAdd createRoutine failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "루틴 생성에 실패했습니다")
                }
        }
    }
}
