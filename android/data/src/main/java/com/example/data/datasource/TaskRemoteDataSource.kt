package com.example.data.datasource

import com.example.data.api.TaskApi
import com.example.data.network.apiCall
import com.example.data.request.CreateRoutineRequest
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(
    private val taskApi: TaskApi
) {
    suspend fun getTask(taskId: Long): Result<TaskDetailResponse> = apiCall { taskApi.getTask(taskId) }

    suspend fun getTaskCalendar(from: String?, to: String?): Result<TaskCalendarResponse> =
        apiCall { taskApi.getTaskCalendar(from, to) }

    suspend fun createRoutine(request: CreateRoutineRequest): Result<TaskDetailResponse> =
        apiCall { taskApi.createRoutine(request) }
}
