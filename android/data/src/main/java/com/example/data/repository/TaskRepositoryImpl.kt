package com.example.data.repository

import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.data.mapper.toRequest
import com.example.data.request.CompleteTaskRequest
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskRemoteDataSource: TaskRemoteDataSource
) : TaskRepository {
    override suspend fun getTask(taskId: Long): Result<Task> {
        return taskRemoteDataSource.getTask(taskId).map { it.toDomain() }
    }

    override suspend fun getTaskCalendar(from: LocalDate?, to: LocalDate?): Result<TaskCalendar> {
        return taskRemoteDataSource.getTaskCalendar(from?.toString(), to?.toString()).map { it.toDomain() }
    }

    override suspend fun createTodo(todo: NewTodo): Result<Task> {
        return taskRemoteDataSource.createTodo(todo.toRequest()).map { it.toDomain() }
    }

    override suspend fun createRoutine(routine: NewRoutine): Result<Task> {
        return taskRemoteDataSource.createRoutine(routine.toRequest()).map { it.toDomain() }
    }

    override suspend fun completeTask(taskId: Long, date: LocalDate?): Result<Unit> {
        return taskRemoteDataSource.completeTask(taskId, CompleteTaskRequest(date = date?.toString()))
    }
}
