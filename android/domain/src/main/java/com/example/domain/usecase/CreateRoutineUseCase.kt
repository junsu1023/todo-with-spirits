package com.example.domain.usecase

import com.example.domain.model.NewRoutine
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import javax.inject.Inject

class CreateRoutineUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(routine: NewRoutine): Result<Task> = taskRepository.createRoutine(routine)
}
