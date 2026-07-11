package com.example.data.network

import com.example.data.error.ApiErrorCode
import com.example.data.error.ApiException
import com.example.data.response.ApiErrorResponse
import com.example.data.response.ApiResponse
import com.google.gson.Gson
import retrofit2.Response

private val errorBodyGson = Gson()

suspend fun <T> apiCall(request: suspend () -> Response<ApiResponse<T>>): Result<T> {
    return try {
        val response = request()

        if (response.isSuccessful) {
            response.body()?.detail?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Response body is empty"))
        } else {
            Result.failure(response.toApiException())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun Response<*>.toApiException(): ApiException {
    val errorDetail = errorBody()?.string()?.let {
        runCatching { errorBodyGson.fromJson(it, ApiErrorResponse::class.java) }.getOrNull()
    }?.detail

    return ApiException(
        httpStatus = errorDetail?.status ?: code(),
        errorCode = ApiErrorCode.from(errorDetail?.errorCode ?: "UNKNOWN"),
        fieldErrors = errorDetail?.description.orEmpty(),
        message = errorDetail?.description?.firstOrNull()?.message
            ?: message().ifBlank { "요청 처리 중 오류가 발생했습니다" }
    )
}
