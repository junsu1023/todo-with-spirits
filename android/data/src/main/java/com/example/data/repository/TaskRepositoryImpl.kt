package com.example.data.repository

import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskRemoteDataSource: TaskRemoteDataSource
) : TaskRepository {
    override suspend fun getTask(taskId: Long): Result<Task> {
        return taskRemoteDataSource.getTask(taskId).map { it.toDomain() }
    }
}
