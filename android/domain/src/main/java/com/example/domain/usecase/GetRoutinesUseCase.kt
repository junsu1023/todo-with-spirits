package com.example.domain.usecase

import com.example.domain.model.TaskList
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class GetRoutinesUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(from: LocalDate? = null, to: LocalDate? = null): Result<TaskList> =
        taskRepository.getRoutines(from, to)
}
