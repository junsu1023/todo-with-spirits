package com.example.data.network

import android.util.Log
import com.example.data.error.ApiErrorCode
import com.example.data.error.ApiErrorMessageTranslator
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

// detail이 null인 것 자체가 정상 성공 응답인 API(예: 완료 처리)를 위한 변형
suspend fun apiCallUnit(request: suspend () -> Response<ApiResponse<Unit?>>): Result<Unit> {
    return try {
        val response = request()
        val requestLine = response.raw().request.let { "${it.method} ${it.url.encodedPath}" }

        if (response.isSuccessful) {
            Log.d(TAG, "$requestLine 성공 (${response.code()})")
            Result.success(Unit)
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
    val rawErrorCode = errorDetail?.errorCode ?: "UNKNOWN"

    // 서버 메시지는 영어라 그대로 보여줄 수 없어서, ApiException을 만드는 이 한 곳에서
    // 한국어로 치환해둔다. 이후 ApiException.message/fieldErrors를 쓰는 모든 ViewModel은
    // 번역 여부를 신경 쓸 필요가 없다. 매핑이 없는 메시지는 원문 그대로 둔다.
    val translatedFieldErrors = errorDetail?.description.orEmpty().map { field ->
        field.copy(message = ApiErrorMessageTranslator.translate(field.message, rawErrorCode) ?: field.message)
    }

    val message = translatedFieldErrors.firstOrNull()?.message
        ?: ApiErrorMessageTranslator.translate(null, rawErrorCode)
        ?: message().ifBlank { "요청 처리 중 오류가 발생했습니다" }

    return ApiException(
        httpStatus = errorDetail?.status ?: code(),
        errorCode = ApiErrorCode.from(rawErrorCode),
        fieldErrors = translatedFieldErrors,
        message = message
    )
}
