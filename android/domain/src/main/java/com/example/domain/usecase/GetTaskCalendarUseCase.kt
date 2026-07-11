package com.example.domain.usecase

import com.example.domain.model.TaskCalendar
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTaskCalendarUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(from: LocalDate? = null, to: LocalDate? = null): Result<TaskCalendar> =
        taskRepository.getTaskCalendar(from, to)
}
