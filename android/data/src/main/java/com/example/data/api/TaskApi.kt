package com.example.data.api

import com.example.data.constant.URLConstant
import com.example.data.response.ApiResponse
import com.example.data.response.TaskDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TaskApi {
    @GET(URLConstant.TASK.TASK_DETAIL)
    suspend fun getTask(@Path("taskId") taskId: Long): Response<ApiResponse<TaskDetailResponse>>
}
