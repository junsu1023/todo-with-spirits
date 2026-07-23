package com.example.domain.usecase

import com.example.domain.model.NewRoutine
import com.example.domain.model.Routine
import com.example.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateRoutineUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, routine: NewRoutine): Result<Routine> =
        taskRepository.updateRoutine(taskId, routine)
}
