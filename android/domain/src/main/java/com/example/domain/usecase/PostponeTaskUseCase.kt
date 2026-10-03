package com.example.domain.usecase

import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class PostponeTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        taskId: Long,
        originalDate: LocalDate? = null,
        newDate: LocalDate? = null,
        newTime: LocalTime? = null
    ): Result<Unit> = taskRepository.postponeTask(taskId, originalDate, newDate, newTime)
}
