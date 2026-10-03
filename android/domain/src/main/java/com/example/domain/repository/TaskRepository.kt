package com.example.domain.repository

import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import java.time.LocalDate
import java.time.LocalTime

interface TaskRepository {
    suspend fun getTask(taskId: Long): Result<Task>

    suspend fun getTaskCalendar(from: LocalDate? = null, to: LocalDate? = null): Result<TaskCalendar>

    suspend fun createTodo(todo: NewTodo): Result<Task>

    suspend fun createRoutine(routine: NewRoutine): Result<Task>

    suspend fun completeTask(taskId: Long, date: LocalDate): Result<Unit>

    suspend fun cancelTaskCompletion(taskId: Long, date: LocalDate): Result<Unit>

    suspend fun deleteTasks(taskIds: List<Long>): Result<Int>

    suspend fun updateTodo(taskId: Long, todo: NewTodo): Result<Task>

    suspend fun updateRoutine(taskId: Long, routine: NewRoutine): Result<Routine>

    // originalDate: routine은 필수(미룰 발생일), schedule은 무시된다.
    // newDate/newTime: null이면 서버가 기존 값을 유지한다.
    suspend fun postponeTask(
        taskId: Long,
        originalDate: LocalDate? = null,
        newDate: LocalDate? = null,
        newTime: LocalTime? = null
    ): Result<Unit>
}
