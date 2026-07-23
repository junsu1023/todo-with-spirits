package com.example.data.datasource

import com.example.data.api.TaskApi
import com.example.data.network.apiCall
import com.example.data.network.apiCallUnit
import com.example.data.request.CreateRoutineRequest
import com.example.data.request.CreateTodoRequest
import com.example.data.request.DeleteTaskRequest
import com.example.data.request.UpdateRoutineRequest
import com.example.data.response.DeleteTaskResponse
import com.example.data.response.RoutineDetailResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(
    private val taskApi: TaskApi
) {
    suspend fun getTask(taskId: Long): Result<TaskDetailResponse> = apiCall { taskApi.getTask(taskId) }

    suspend fun getTaskCalendar(from: String?, to: String?): Result<TaskCalendarResponse> =
        apiCall { taskApi.getTaskCalendar(from, to) }

    suspend fun createTodo(request: CreateTodoRequest): Result<TaskDetailResponse> =
        apiCall { taskApi.createTodo(request) }

    suspend fun createRoutine(request: CreateRoutineRequest): Result<TaskDetailResponse> =
        apiCall { taskApi.createRoutine(request) }

    suspend fun completeTask(taskId: Long, date: String): Result<Unit> =
        apiCallUnit { taskApi.completeTask(taskId, date) }

    suspend fun cancelTaskCompletion(taskId: Long, date: String): Result<Unit> =
        apiCallUnit { taskApi.cancelTaskCompletion(taskId, date) }

    suspend fun deleteTasks(request: DeleteTaskRequest): Result<DeleteTaskResponse> =
        apiCall { taskApi.deleteTasks(request) }

    suspend fun updateTodo(taskId: Long, request: CreateTodoRequest): Result<TaskDetailResponse> =
        apiCall { taskApi.updateTodo(taskId, request) }

    suspend fun updateRoutine(taskId: Long, request: UpdateRoutineRequest): Result<RoutineDetailResponse> =
        apiCall { taskApi.updateRoutine(taskId, request) }
}
