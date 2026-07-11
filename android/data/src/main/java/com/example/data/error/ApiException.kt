package com.example.data.error

import com.example.data.response.ApiErrorField

class ApiException(
    val httpStatus: Int,
    val errorCode: ApiErrorCode,
    val fieldErrors: List<ApiErrorField> = emptyList(),
    message: String
) : Exception(message)
