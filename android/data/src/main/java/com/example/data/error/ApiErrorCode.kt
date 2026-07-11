package com.example.data.error

sealed class ApiErrorCode(val code: String) {
    data object MissingParameter : ApiErrorCode("MISSING_PARAMETER")
    data object InvalidParameter : ApiErrorCode("INVALID_PARAMETER")
    data object Unauthorized : ApiErrorCode("UNAUTHORIZED")
    data object TokenExpired : ApiErrorCode("TOKEN_EXPIRED")
    data object Forbidden : ApiErrorCode("FORBIDDEN")
    data object Conflict : ApiErrorCode("CONFLICT")
    data object InternalServerError : ApiErrorCode("INTERNAL_SERVER_ERROR")
    data object ServiceUnavailable : ApiErrorCode("SERVICE_UNAVAILABLE")
    // 문서상의 패턴은 {RESOURCE}_NOT_FOUND 이지만, 실제 API는 리소스 접두어 없이 NOT_FOUND만 내려주는 경우도 있다.
    data object NotFound : ApiErrorCode("NOT_FOUND")
    data class ResourceNotFound(val resource: String) : ApiErrorCode("${resource}_NOT_FOUND")
    data class Unknown(val rawCode: String) : ApiErrorCode(rawCode)

    companion object {
        private val knownCodes: List<ApiErrorCode> = listOf(
            MissingParameter, InvalidParameter, Unauthorized, TokenExpired,
            Forbidden, Conflict, InternalServerError, ServiceUnavailable, NotFound
        )

        fun from(code: String): ApiErrorCode {
            knownCodes.firstOrNull { it.code == code }?.let { return it }
            if (code.endsWith("_NOT_FOUND")) {
                return ResourceNotFound(code.removeSuffix("_NOT_FOUND"))
            }
            return Unknown(code)
        }
    }
}
