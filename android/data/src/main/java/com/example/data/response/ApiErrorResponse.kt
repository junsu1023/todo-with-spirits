package com.example.data.response

data class ApiErrorResponse(
    val result: String,
    val detail: ApiErrorDetail
)

data class ApiErrorDetail(
    val status: Int,
    val timestamp: String,
    val errorCode: String,
    val description: List<ApiErrorField>? = null
)

data class ApiErrorField(
    val field: String? = null,
    val message: String
)
