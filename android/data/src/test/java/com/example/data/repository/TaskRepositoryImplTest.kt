package com.example.data.repository

import com.example.data.api.TaskApi
import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.error.ApiErrorCode
import com.example.data.error.ApiException
import com.example.data.request.CreateRoutineRequest
import com.example.data.request.CreateTodoRequest
import com.example.data.request.DeleteTaskRequest
import com.example.data.request.PostponeTaskRequest
import com.example.data.request.UpdateRoutineRequest
import com.example.data.response.ApiResponse
import com.example.data.response.DeleteTaskResponse
import com.example.data.response.RoutineDetailResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import com.example.domain.exception.FieldValidationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate
import okhttp3.Response as OkHttpResponse

// 일정/루틴 미루기(postpone) 요청 매핑과, 400 응답의 필드별 사유를 FieldValidationException으로
// 변환하는 TaskRepositoryImpl.recoverFieldValidationErrors()를 검증한다.
// 실제 apiCallUnit 경로(okhttp Response -> ApiException 변환)까지 그대로 태워보기 위해
// TaskApi만 가짜로 두고 TaskRemoteDataSource/TaskRepositoryImpl은 실제 구현을 사용한다.
class TaskRepositoryImplTest {
    private class FakeTaskApi(
        private val postponeResponse: Response<ApiResponse<Unit?>>
    ) : TaskApi {
        var lastTaskId: Long? = null
        var lastRequest: PostponeTaskRequest? = null

        override suspend fun getTask(taskId: Long): Response<ApiResponse<TaskDetailResponse>> = error("not used")
        override suspend fun getTaskCalendar(from: String?, to: String?): Response<ApiResponse<TaskCalendarResponse>> = error("not used")
        override suspend fun createTodo(request: CreateTodoRequest): Response<ApiResponse<TaskDetailResponse>> = error("not used")
        override suspend fun createRoutine(request: CreateRoutineRequest): Response<ApiResponse<TaskDetailResponse>> = error("not used")
        override suspend fun completeTask(taskId: Long, date: String): Response<ApiResponse<Unit?>> = error("not used")
        override suspend fun cancelTaskCompletion(taskId: Long, date: String): Response<ApiResponse<Unit?>> = error("not used")
        override suspend fun deleteTasks(request: DeleteTaskRequest): Response<ApiResponse<DeleteTaskResponse>> = error("not used")
        override suspend fun updateTodo(taskId: Long, request: CreateTodoRequest): Response<ApiResponse<TaskDetailResponse>> = error("not used")
        override suspend fun updateRoutine(taskId: Long, request: UpdateRoutineRequest): Response<ApiResponse<RoutineDetailResponse>> = error("not used")

        override suspend fun postponeTask(taskId: Long, request: PostponeTaskRequest): Response<ApiResponse<Unit?>> {
            lastTaskId = taskId
            lastRequest = request
            return postponeResponse
        }
    }

    private fun rawResponse(code: Int, message: String): OkHttpResponse {
        val request = Request.Builder()
            .url("http://localhost/api/task/1/postpone")
            .patch("{}".toRequestBody("application/json".toMediaType()))
            .build()
        return OkHttpResponse.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .build()
    }

    private fun successResponse(): Response<ApiResponse<Unit?>> =
        Response.success(ApiResponse("success", null), rawResponse(200, "OK"))

    private fun errorResponse(json: String, code: Int): Response<ApiResponse<Unit?>> =
        Response.error(json.toResponseBody("application/json".toMediaType()), rawResponse(code, "Error"))

    private fun repositoryWith(response: Response<ApiResponse<Unit?>>): Pair<TaskRepositoryImpl, FakeTaskApi> {
        val api = FakeTaskApi(response)
        return TaskRepositoryImpl(TaskRemoteDataSource(api)) to api
    }

    @Test fun postponeTaskSerializesDatesToIsoStringsAndKeepsNullsAsNull() = runTest {
        val (repository, api) = repositoryWith(successResponse())

        repository.postponeTask(
            taskId = 217L,
            originalDate = LocalDate.of(2026, 1, 1),
            newDate = LocalDate.of(2026, 1, 2),
            newTime = null
        )

        assertEquals(217L, api.lastTaskId)
        assertEquals(PostponeTaskRequest("2026-01-01", "2026-01-02", null), api.lastRequest)
    }

    @Test fun postponeTaskReturnsSuccessOn200() = runTest {
        val (repository, _) = repositoryWith(successResponse())

        val result = repository.postponeTask(taskId = 1L, newDate = LocalDate.of(2026, 1, 2))

        assertTrue(result.isSuccess)
    }

    @Test fun postponeTaskConvertsFieldErrorsToFieldValidationException() = runTest {
        val errorJson = """
            {
              "result": "fail",
              "detail": {
                "status": 400,
                "timestamp": "2026-01-01T00:00:00.000000+09:00",
                "errorCode": "INVALID_PARAMETER",
                "description": [
                  {"field": "originalDate", "message": "originalDate is required for routines"}
                ]
              }
            }
        """.trimIndent()
        val (repository, _) = repositoryWith(errorResponse(errorJson, 400))

        val result = repository.postponeTask(taskId = 1L, newDate = LocalDate.of(2026, 1, 2))

        val error = result.exceptionOrNull()
        assertTrue(error is FieldValidationException)
        // ApiCallExtensions.toApiException()이 ApiErrorMessageTranslator로 한국어로 치환해둔다.
        assertEquals(
            mapOf("originalDate" to "루틴을 미루기 위해서는 기존 일자가 필요합니다."),
            (error as FieldValidationException).fieldErrors
        )
    }

    // 404(NOT_FOUND) 응답처럼 field가 null인 description만 있는 경우, 기존
    // AuthRepositoryImpl과 동일한 로직을 그대로 따르기 때문에 fieldErrors가 비는 채로
    // FieldValidationException이 된다 — ViewModel의 "fieldErrors ?: error.message" 폴백으로
    // 메시지 자체는 여전히 정상 노출되므로 의도된 동작이다.
    @Test fun postponeTaskKeepsOverallMessageWhenFieldIsNull() = runTest {
        val errorJson = """
            {
              "result": "fail",
              "detail": {
                "status": 404,
                "timestamp": "2026-01-01T00:00:00.000000+09:00",
                "errorCode": "NOT_FOUND",
                "description": [
                  {"field": null, "message": "Not found schedule or routine."}
                ]
              }
            }
        """.trimIndent()
        val (repository, _) = repositoryWith(errorResponse(errorJson, 404))

        val result = repository.postponeTask(taskId = 999L, newDate = LocalDate.of(2026, 1, 2))

        val error = result.exceptionOrNull()
        assertTrue(error is FieldValidationException)
        assertTrue((error as FieldValidationException).fieldErrors.isEmpty())
        assertEquals("일정이나 루틴을 찾을 수 없습니다.", error.message)
    }

    // description이 아예 없는(필드별 사유가 없는) 일반 에러는 변환하지 않고 ApiException 그대로 둔다.
    // 과거 /api/{taskId}/postpone 엔드포인트 오타로 터졌던 "No static resource..." 500이 이 케이스다.
    @Test fun postponeTaskLeavesNonFieldErrorsAsApiException() = runTest {
        val errorJson = """
            {
              "result": "fail",
              "detail": {
                "status": 500,
                "timestamp": "2026-01-01T00:00:00.000000+09:00",
                "errorCode": "INTERNAL_SERVER_ERROR",
                "description": null
              }
            }
        """.trimIndent()
        val (repository, _) = repositoryWith(errorResponse(errorJson, 500))

        val result = repository.postponeTask(taskId = 1L, newDate = LocalDate.of(2026, 1, 2))

        val error = result.exceptionOrNull()
        assertTrue(error is ApiException)
        assertEquals(ApiErrorCode.InternalServerError, (error as ApiException).errorCode)
    }
}
