package com.example.domain.repository

import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskCompletionReward
import com.example.domain.model.TaskList
import java.time.LocalDate
import java.time.LocalTime

interface TaskRepository {
    suspend fun getTask(taskId: Long): Result<Task>

    suspend fun getTaskCalendar(from: LocalDate? = null, to: LocalDate? = null): Result<TaskCalendar>

    // from/to 중 하나만 넘기면 서버가 날짜 필터를 적용하지 않는다.
    suspend fun getSchedules(from: LocalDate? = null, to: LocalDate? = null): Result<TaskList>

    // from/to를 생략하면 서버 기본값(오늘)으로 조회된다.
    suspend fun getRoutines(from: LocalDate? = null, to: LocalDate? = null): Result<TaskList>

    suspend fun createTodo(todo: NewTodo): Result<Task>

    suspend fun createRoutine(routine: NewRoutine): Result<Task>

    suspend fun completeTask(taskId: Long, date: LocalDate): Result<TaskCompletionReward>

    suspend fun cancelTaskCompletion(taskId: Long, date: LocalDate): Result<Unit>

    suspend fun deleteTasks(taskIds: List<Long>): Result<Int>

    suspend fun updateTodo(taskId: Long, todo: NewTodo): Result<Task>

    suspend fun updateRoutine(taskId: Long, routine: NewRoutine): Result<Task>

    // originalDate: routine은 필수(미룰 발생일), schedule은 무시된다.
    // newDate/newTime: null이면 서버가 기존 값을 유지한다.
    suspend fun postponeTask(
        taskId: Long,
        originalDate: LocalDate? = null,
        newDate: LocalDate? = null,
        newTime: LocalTime? = null
    ): Result<Unit>
}
