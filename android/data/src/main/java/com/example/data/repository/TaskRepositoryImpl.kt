package com.example.data.repository

import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.error.ApiException
import com.example.data.mapper.toDomain
import com.example.data.mapper.toRequest
import com.example.data.mapper.toUpdateRequest
import com.example.data.request.DeleteTaskRequest
import com.example.data.request.PostponeTaskRequest
import com.example.domain.exception.FieldValidationException
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskRemoteDataSource: TaskRemoteDataSource
) : TaskRepository {
    override suspend fun getTask(taskId: Long): Result<Task> {
        return taskRemoteDataSource.getTask(taskId).mapCatching { it.toDomain() }
    }

    override suspend fun getTaskCalendar(from: LocalDate?, to: LocalDate?): Result<TaskCalendar> {
        return taskRemoteDataSource.getTaskCalendar(from?.toString(), to?.toString()).mapCatching { it.toDomain() }
    }

    override suspend fun createTodo(todo: NewTodo): Result<Task> {
        return taskRemoteDataSource.createTodo(todo.toRequest()).mapCatching { it.toDomain() }
    }

    override suspend fun createRoutine(routine: NewRoutine): Result<Task> {
        return taskRemoteDataSource.createRoutine(routine.toRequest()).mapCatching { it.toDomain() }
    }

    override suspend fun completeTask(taskId: Long, date: LocalDate): Result<Unit> {
        return taskRemoteDataSource.completeTask(taskId, date.toString())
    }

    override suspend fun cancelTaskCompletion(taskId: Long, date: LocalDate): Result<Unit> {
        return taskRemoteDataSource.cancelTaskCompletion(taskId, date.toString())
    }

    override suspend fun deleteTasks(taskIds: List<Long>): Result<Int> {
        return taskRemoteDataSource.deleteTasks(DeleteTaskRequest(taskIds)).mapCatching { it.deletedCount }
    }

    override suspend fun updateTodo(taskId: Long, todo: NewTodo): Result<Task> {
        return taskRemoteDataSource.updateTodo(taskId, todo.toRequest()).mapCatching { it.toDomain() }
    }

    override suspend fun updateRoutine(taskId: Long, routine: NewRoutine): Result<Routine> {
        return taskRemoteDataSource.updateRoutine(taskId, routine.toUpdateRequest()).mapCatching { it.toDomain() }
    }

    override suspend fun postponeTask(
        taskId: Long,
        originalDate: LocalDate?,
        newDate: LocalDate?,
        newTime: LocalTime?
    ): Result<Unit> {
        val request = PostponeTaskRequest(
            originalDate = originalDate?.toString(),
            newDate = newDate?.toString(),
            newTime = newTime?.toString()
        )
        return taskRemoteDataSource.postponeTask(taskId, request).recoverFieldValidationErrors()
    }

    // 미루기 400 응답은 originalDate/newDate/postponeCount/repeatType 등 구체적인 필드별 사유를
    // 내려주므로, 화면에서 그대로 보여줄 수 있게 FieldValidationException으로 변환해둔다.
    private fun <T> Result<T>.recoverFieldValidationErrors(): Result<T> {
        val error = exceptionOrNull() ?: return this
        if (error !is ApiException || error.fieldErrors.isEmpty()) return this

        val fieldErrors = error.fieldErrors.mapNotNull { field ->
            field.field?.let { it to field.message }
        }.toMap()

        return Result.failure(FieldValidationException(fieldErrors, error.message ?: "요청이 올바르지 않습니다"))
    }
}
