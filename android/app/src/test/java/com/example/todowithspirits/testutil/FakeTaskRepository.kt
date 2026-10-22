package com.example.todowithspirits.testutil

import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime

// ViewModel 테스트용 TaskRepository 더블. CalendarViewModel이 init{}에서
// 곧바로 getTaskCalendar를 호출하므로, 기본값을 빈 캘린더 성공 응답으로 둔다.
class FakeTaskRepository : TaskRepository {
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
    ): Result<Unit> = error("not used")
}
