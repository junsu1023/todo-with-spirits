package com.example.domain.usecase

import com.example.domain.model.NewTodo
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTodoUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(todo: NewTodo): Result<Task> = taskRepository.createTodo(todo)
}