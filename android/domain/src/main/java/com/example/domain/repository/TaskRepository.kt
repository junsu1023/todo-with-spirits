package com.example.domain.repository

import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import java.time.LocalDate

interface TaskRepository {
    suspend fun getTask(taskId: Long): Result<Task>

    suspend fun getTaskCalendar(from: LocalDate? = null, to: LocalDate? = null): Result<TaskCalendar>
}
