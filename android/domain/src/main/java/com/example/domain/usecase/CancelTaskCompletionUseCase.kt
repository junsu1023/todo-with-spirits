package com.example.domain.usecase

import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class CancelTaskCompletionUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, date: LocalDate? = null): Result<Unit> =
        taskRepository.cancelTaskCompletion(taskId, date)
}
