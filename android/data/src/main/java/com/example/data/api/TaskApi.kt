package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.request.CompleteTaskRequest
import com.example.data.request.CreateRoutineRequest
import com.example.data.request.CreateTodoRequest
import com.example.data.request.DeleteTaskRequest
import com.example.data.request.UpdateRoutineRequest
import com.example.data.response.ApiResponse
import com.example.data.response.DeleteTaskResponse
import com.example.data.response.RoutineDetailResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
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

    @POST(URLConstant.TASK.TASK_SCHEDULE)
    suspend fun createTodo(@Body request: CreateTodoRequest): Response<ApiResponse<TaskDetailResponse>>

    @POST(URLConstant.TASK.TASK_ROUTINE)
    suspend fun createRoutine(@Body request: CreateRoutineRequest): Response<ApiResponse<TaskDetailResponse>>

    @POST(URLConstant.TASK.TASK_COMPLETE)
    suspend fun completeTask(
        @Path("taskId") taskId: Long,
        @Body request: CompleteTaskRequest
    ): Response<ApiResponse<Unit?>>

    @HTTP(method = "DELETE", path = URLConstant.TASK.TASK_COMPLETE, hasBody = true)
    suspend fun cancelTaskCompletion(
        @Path("taskId") taskId: Long,
        @Body request: CompleteTaskRequest
    ): Response<ApiResponse<Unit?>>

    @HTTP(method = "DELETE", path = URLConstant.TASK.TASK_DELETE, hasBody = true)
    suspend fun deleteTasks(@Body request: DeleteTaskRequest): Response<ApiResponse<DeleteTaskResponse>>

    @PATCH(URLConstant.TASK.TASK_SCHEDULE_DETAIL)
    suspend fun updateTodo(
        @Path("taskId") taskId: Long,
        @Body request: CreateTodoRequest
    ): Response<ApiResponse<TaskDetailResponse>>

    @PATCH(URLConstant.TASK.TASK_ROUTINE_DETAIL)
    suspend fun updateRoutine(
        @Path("taskId") taskId: Long,
        @Body request: UpdateRoutineRequest
    ): Response<ApiResponse<RoutineDetailResponse>>
}
