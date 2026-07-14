package com.example.todowithspirits.feature.plan.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.PlanSortOption
import com.example.domain.model.TaskSummary
import com.example.domain.model.TaskType
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.plan.PlanItemData
import com.example.todowithspirits.feature.plan.PlanType
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
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> get() = _uiState.asStateFlow()

    init {
        loadPlans()

        taskRefreshBus.events
            .onEach { loadPlans() }
            .launchIn(viewModelScope)
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadPlans()
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
}

private fun TaskSummary.toPlanItemData(): PlanItemData = PlanItemData(
    id = taskId.toInt(),
    title = title,
    type = if (taskType == TaskType.HABIT.type) PlanType.ROUTINE else PlanType.TODO,
    isImportant = isImportant,
    isDone = isCompleted,
    dueDate = startDate,
    dueTime = startTime,
    memo = memo ?: "",
    category = category.takeIf { it.isNotBlank() && it != "NONE" },
    repeatInfo = repeatType?.takeIf { it != "NONE" }
)
