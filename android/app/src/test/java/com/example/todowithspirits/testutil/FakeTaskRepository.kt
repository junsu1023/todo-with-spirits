package com.example.todowithspirits.testutil

import com.example.domain.model.NewTodo
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskCompletionReward
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime

// ViewModel 테스트용 TaskRepository 더블. CalendarViewModel이 init{}에서
// 곧바로 getTaskCalendar를 호출하므로, 기본값을 빈 캘린더 성공 응답으로 둔다.
class FakeTaskRepository : TaskRepository {
    var taskCalendarResult: Result<TaskCalendar> = Result.success(
        TaskCalendar(
            completedCount = 0,
            inCompleteCount = 0,
            totalCount = 0,
            items = emptyList()
        )
    )
    var getTaskResult: Result<Task>? = null
    var createTodoResult: Result<Task>? = null
    var updateTodoResult: Result<Task>? = null
    var lastCalendarRange: Pair<LocalDate?, LocalDate?>? = null
    var lastCompletedTask: Pair<Long, LocalDate>? = null
    var lastCanceledTask: Pair<Long, LocalDate>? = null
    var lastDeletedTaskIds: List<Long>? = null
    var lastCreatedTodo: NewTodo? = null
    var lastUpdatedTodo: Pair<Long, NewTodo>? = null

    override suspend fun getTask(taskId: Long): Result<Task> = getTaskResult ?: error("not used")
    override suspend fun getTaskCalendar(from: LocalDate?, to: LocalDate?): Result<TaskCalendar> {
        lastCalendarRange = from to to
        return taskCalendarResult
    }
    override suspend fun createTodo(todo: NewTodo): Result<Task> {
        lastCreatedTodo = todo
        return createTodoResult ?: error("not used")
    }
    override suspend fun completeTask(taskId: Long, date: LocalDate): Result<TaskCompletionReward> {
        lastCompletedTask = taskId to date
        return Result.success(TaskCompletionReward(currencyEarned = 10, balance = 10))
    }
    override suspend fun cancelTaskCompletion(taskId: Long, date: LocalDate): Result<Unit> {
        lastCanceledTask = taskId to date
        return Result.success(Unit)
    }
    override suspend fun deleteTasks(taskIds: List<Long>): Result<Int> {
        lastDeletedTaskIds = taskIds
        return Result.success(taskIds.size)
    }
    override suspend fun updateTodo(taskId: Long, todo: NewTodo): Result<Task> {
        lastUpdatedTodo = taskId to todo
        return updateTodoResult ?: error("not used")
    }

    override suspend fun postponeTask(
        taskId: Long,
        originalDate: LocalDate?,
        newDate: LocalDate?,
        newTime: LocalTime?
    ): Result<Unit> = error("not used")
}
