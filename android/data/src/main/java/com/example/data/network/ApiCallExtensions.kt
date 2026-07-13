package com.example.data.network

import android.util.Log
import com.example.data.error.ApiErrorCode
import com.example.data.error.ApiException
import com.example.data.response.ApiErrorResponse
import com.example.data.response.ApiResponse
import com.google.gson.Gson
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "ApiCall"

private val errorBodyGson = Gson()

suspend fun <T> apiCall(request: suspend () -> Response<ApiResponse<T>>): Result<T> {
    return try {
        val response = request()
        val requestLine = response.raw().request.let { "${it.method} ${it.url.encodedPath}" }

        if (response.isSuccessful) {
            response.body()?.detail?.let {
                Log.d(TAG, "$requestLine 성공 (${response.code()})")
                Result.success(it)
            } ?: run {
                Log.e(TAG, "$requestLine 실패 (${response.code()}): 응답 본문이 비어있음")
                Result.failure(IllegalStateException("Response body is empty"))
            }
        } else {
            val exception = response.toApiException()
            val errorDescription = exception.fieldErrors
                .joinToString { "${it.field}: ${it.message}" }
                .ifEmpty { exception.message.orEmpty() }

            Log.e(TAG, "$requestLine 실패 (${exception.httpStatus}): ${exception.errorCode.code} - $errorDescription")
            Result.failure(exception)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "API 호출 예외: ${e.javaClass.simpleName} - ${e.message}", e)
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
