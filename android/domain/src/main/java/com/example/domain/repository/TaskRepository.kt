package com.example.domain.repository

import com.example.domain.model.Task

interface TaskRepository {
    suspend fun getTask(taskId: Long): Result<Task>
}
