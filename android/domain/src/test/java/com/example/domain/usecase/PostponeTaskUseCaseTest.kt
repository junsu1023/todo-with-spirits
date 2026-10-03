package com.example.domain.usecase

import com.example.domain.exception.FieldValidationException
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PostponeTaskUseCaseTest {
    private class FakeTaskRepository : TaskRepository {
        var lastTaskId: Long? = null
        var lastOriginalDate: LocalDate? = null
        var lastNewDate: LocalDate? = null
        var lastNewTime: LocalTime? = null
        var result: Result<Unit> = Result.success(Unit)

        override suspend fun getTask(taskId: Long): Result<Task> = error("not used")
        override suspend fun getTaskCalendar(from: LocalDate?, to: LocalDate?): Result<TaskCalendar> = error("not used")
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
            lastTaskId = taskId
            lastOriginalDate = originalDate
            lastNewDate = newDate
            lastNewTime = newTime
            return result
        }
    }

    @Test fun invokeForwardsAllArgumentsToRepository() = runTest {
        val repo = FakeTaskRepository()
        val useCase = PostponeTaskUseCase(repo)
        val original = LocalDate.of(2026, 1, 1)
        val newDate = LocalDate.of(2026, 1, 2)
        val newTime = LocalTime.of(9, 0)

        val result = useCase(taskId = 10L, originalDate = original, newDate = newDate, newTime = newTime)

        assertEquals(10L, repo.lastTaskId)
        assertEquals(original, repo.lastOriginalDate)
        assertEquals(newDate, repo.lastNewDate)
        assertEquals(newTime, repo.lastNewTime)
        assertTrue(result.isSuccess)
    }

    @Test fun invokeDefaultsOptionalArgumentsToNull() = runTest {
        val repo = FakeTaskRepository()
        val useCase = PostponeTaskUseCase(repo)

        useCase(taskId = 5L)

        assertEquals(5L, repo.lastTaskId)
        assertNull(repo.lastOriginalDate)
        assertNull(repo.lastNewDate)
        assertNull(repo.lastNewTime)
    }

    @Test fun invokePropagatesRepositoryFailure() = runTest {
        val repo = FakeTaskRepository()
        val failure = FieldValidationException(
            mapOf("newDate" to "Target date already has an occurrence of this routine"),
            "요청이 올바르지 않습니다"
        )
        repo.result = Result.failure(failure)
        val useCase = PostponeTaskUseCase(repo)

        val result = useCase(taskId = 1L)

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }
}
