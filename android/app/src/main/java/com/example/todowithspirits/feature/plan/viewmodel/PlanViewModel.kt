package com.example.todowithspirits.feature.plan.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.CategoryOption
import com.example.domain.model.PlanSortOption
import com.example.domain.model.TaskSummary
import com.example.domain.model.TaskType
import com.example.domain.usecase.CancelTaskCompletionUseCase
import com.example.domain.usecase.CompleteTaskUseCase
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.plan.model.PlanItemData
import com.example.todowithspirits.feature.plan.model.PlanType
import com.example.todowithspirits.feature.plan.state.DayPlanEvents
import com.example.todowithspirits.feature.plan.state.PlanUiState
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val cancelTaskCompletionUseCase: CancelTaskCompletionUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> get() = _uiState.asStateFlow()

    init {
        loadPlans()
        loadCalendarEvents()

        taskRefreshBus.events
            .onEach {
                loadPlans()
                loadCalendarEvents()
            }
            .launchIn(viewModelScope)
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadPlans()
    }

    fun setCalendarMonth(month: YearMonth) {
        if (month == _uiState.value.calendarMonth) return

        _uiState.update { it.copy(calendarMonth = month) }
        loadCalendarEvents()
    }

    fun setSelectedTab(tab: String) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(selectedTab = tab)
            }
        }
    }

    fun setHiddenState(hidden: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isHidden = hidden)
            }
        }
    }

    fun setSortOption(option: PlanSortOption) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(sortOption = option)
            }
        }
    }

    fun loadPlans() {
        viewModelScope.launchWithLoading {
            val selectedDate = _uiState.value.selectedDate

            getTaskCalendarUseCase(selectedDate, selectedDate)
                .onSuccess { calendar ->
                    _uiState.update { it.copy(plans = calendar.items.map { item -> item.toPlanItemData() }) }
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "플랜을 불러오지 못했습니다")
                }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launchWithLoading {
            deleteTasksUseCase(listOf(taskId))
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "삭제하지 못했습니다")
                }
        }
    }

    fun completeTask(taskId: Long, date: LocalDate?) {
        viewModelScope.launchWithLoading {
            completeTaskUseCase(taskId, date)
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "완료 처리에 실패했습니다")
                }
        }
    }

    fun cancelTaskCompletion(taskId: Long, date: LocalDate?) {
        viewModelScope.launchWithLoading {
            cancelTaskCompletionUseCase(taskId, date)
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "완료 취소에 실패했습니다")
                }
        }
    }

    fun loadCalendarEvents() {
        viewModelScope.launchWithLoading {
            val month = _uiState.value.calendarMonth
            val monthStart = month.atDay(1)
            val monthEnd = month.atEndOfMonth()

            getTaskCalendarUseCase(monthStart, monthEnd)
                .onSuccess { calendar ->
                    val events = calendar.items
                        .groupBy { it.occurrenceDate }
                        .mapValues { (_, items) ->
                            DayPlanEvents(
                                types = items.map {
                                    if (it.taskType == TaskType.ROUTINE.type) PlanType.ROUTINE else PlanType.TODO
                                },
                                importantCount = items.count { it.isImportant }
                            )
                        }

                    _uiState.update { it.copy(calendarEvents = events) }
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "캘린더 일정을 불러오지 못했습니다")
                }
        }
    }
}

private fun TaskSummary.toPlanItemData(): PlanItemData = PlanItemData(
    id = taskId.toInt(),
    title = title,
    type = if (taskType == TaskType.ROUTINE.type) PlanType.ROUTINE else PlanType.TODO,
    isImportant = isImportant,
    isDone = isCompleted,
    endDate = occurrenceDate,
    endTime = endTime,
    isAllDay = isAllDay,
    memo = memo ?: "",
    category = CategoryOption.entries.find { it.name == category }
        ?.takeIf { it != CategoryOption.NONE }
        ?.displayName
)
