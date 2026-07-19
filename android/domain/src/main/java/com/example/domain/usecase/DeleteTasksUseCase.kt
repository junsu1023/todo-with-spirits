package com.example.domain.usecase

import com.example.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskIds: List<Long>): Result<Int> = taskRepository.deleteTasks(taskIds)
}
