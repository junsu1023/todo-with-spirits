package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.CreateRoutineRequest
import com.example.data.response.ApiResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {
    @GET(URLConstant.TASK.TASK_DETAIL)
    suspend fun getTask(@Path("taskId") taskId: Long): Response<ApiResponse<TaskDetailResponse>>

    @GET(URLConstant.TASK.TASK_CALENDAR)
    suspend fun getTaskCalendar(
        @Query("from") from: String?,
        @Query("to") to: String?
    ): Response<ApiResponse<TaskCalendarResponse>>

    @POST(URLConstant.TASK.TASK_ROUTINE)
    suspend fun createRoutine(@Body request: CreateRoutineRequest): Response<ApiResponse<TaskDetailResponse>>
}
