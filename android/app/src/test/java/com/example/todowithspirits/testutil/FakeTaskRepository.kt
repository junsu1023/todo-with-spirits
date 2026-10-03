package com.example.todowithspirits.testutil

import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.CompletableDeferred
import java.time.LocalDate
import java.time.LocalTime

// ViewModel 테스트용 TaskRepository 더블. TodayViewModel/PlanViewModel 둘 다 init{}에서
// 곧바로 getTaskCalendar를 호출하므로, 기본값을 빈 캘린더 성공 응답으로 둬서 postpone 외
// 호출이 테스트를 방해하지 않게 한다.
class FakeTaskRepository : TaskRepository {
    data class PostponeCall(
        val taskId: Long,
        val originalDate: LocalDate?,
        val newDate: LocalDate?,
        val newTime: LocalTime?
    )

    var taskCalendarResult: Result<TaskCalendar> = Result.success(
        TaskCalendar(
            completedCount = 0,
            completedRoutineCount = 0,
            completedScheduleCount = 0,
            inCompleteCount = 0,
            routineCount = 0,
            scheduleCount = 0,
            totalCount = 0,
            items = emptyList()
        )
    )
    var postponeResult: Result<Unit> = Result.success(Unit)

    // 로딩 상태 전환(true -> false)을 관찰하려는 테스트가, postponeTask 호출이 완료되는
    // 시점을 직접 통제할 수 있도록 하는 훅. 설정돼 있으면 완료될 때까지 suspend한다.
    var postponeBlocker: CompletableDeferred<Unit>? = null
    var lastPostponeCall: PostponeCall? = null

    override suspend fun getTask(taskId: Long): Result<Task> = error("not used")
    override suspend fun getTaskCalendar(from: LocalDate?, to: LocalDate?): Result<TaskCalendar> = taskCalendarResult
    override suspend fun createTodo(todo: NewTodo): Result<Task> = error("not used")
    override suspend fun createRoutine(routine: NewRoutine): Result<Task> = error("not used")
    override suspend fun completeTask(taskId: Long, date: LocalDate): Result<Unit> = error("not used")
    override suspend fun cancelTaskCompletion(taskId: Long, date: LocalDate): Result<Unit> = error("not used")
    override suspend fun deleteTasks(taskIds: List<Long>): Result<Int> = error("not used")
    override suspend fun updateTodo(taskId: Long, todo: NewTodo): Result<Task> = error("not used")
    override suspend fun updateRoutine(taskId: Long, routine: NewRoutine): Result<Routine> = error("not used")

    override suspend fun postponeTask(
        taskId: Long,
        originalDate: LocalDate?,
        newDate: LocalDate?,
        newTime: LocalTime?
    ): Result<Unit> {
        lastPostponeCall = PostponeCall(taskId, originalDate, newDate, newTime)
        postponeBlocker?.await()
        return postponeResult
    }
}
