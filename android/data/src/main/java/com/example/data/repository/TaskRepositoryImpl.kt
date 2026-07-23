package com.example.data.repository

import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.data.mapper.toRequest
import com.example.data.mapper.toUpdateRequest
import com.example.data.request.CompleteTaskRequest
import com.example.data.request.DeleteTaskRequest
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
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

    override suspend fun completeTask(taskId: Long, date: LocalDate?): Result<Unit> {
        return taskRemoteDataSource.completeTask(taskId, CompleteTaskRequest(date = date?.toString()))
    }

    override suspend fun cancelTaskCompletion(taskId: Long, date: LocalDate?): Result<Unit> {
        return taskRemoteDataSource.cancelTaskCompletion(taskId, CompleteTaskRequest(date = date?.toString()))
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
}
