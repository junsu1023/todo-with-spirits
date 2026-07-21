package com.example.todowithspirits.feature.plan.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.model.CategoryOption
import com.example.domain.model.Task
import com.example.domain.model.TaskType
import com.example.domain.usecase.DeleteTasksUseCase
import com.example.domain.usecase.GetTaskUseCase
import com.example.todowithspirits.feature.plan.model.PlanItemData
import com.example.todowithspirits.feature.plan.model.PlanType
import com.example.todowithspirits.util.TaskRefreshBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val getTaskUseCase: GetTaskUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val taskRefreshBus: TaskRefreshBus
) : BaseViewModel() {
    private val _plan = MutableStateFlow<PlanItemData?>(null)
    val plan: StateFlow<PlanItemData?> get() = _plan.asStateFlow()

    fun loadTask(taskId: Long) {
        viewModelScope.launchWithLoading {
            getTaskUseCase(taskId)
                .onSuccess { task ->
                    _plan.value = task.toPlanItemData()
                }
                .onFailure {
                    Log.e(TAG, "loadTask failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "플랜 정보를 불러오지 못했습니다")
                }
        }
    }

    fun deleteTask(taskId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launchWithLoading {
            deleteTasksUseCase(listOf(taskId))
                .onSuccess {
                    taskRefreshBus.notifyTaskChanged()
                    onSuccess()
                }
                .onFailure {
                    Log.e(TAG, "deleteTask failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "삭제에 실패했습니다")
                }
        }
    }
}

private fun Task.toPlanItemData(): PlanItemData = PlanItemData(
    id = taskId.toInt(),
    title = title,
    type = if (taskType == TaskType.ROUTINE.type) PlanType.ROUTINE else PlanType.TODO,
    isImportant = isImportant,
    isDone = isCompleted,
    endDate = endDate,
    endTime = endTime,
    isAllDay = isAllDay,
    memo = memo,
    category = CategoryOption.entries.find { it.name == category }
        ?.takeIf { it != CategoryOption.NONE }
        ?.displayName,
    isPublic = isPublic,
    notificationMinutes = notificationMinutes
)
