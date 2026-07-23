package com.example.data.response

data class ApiResponse<T>(
    val result: String,
    val detail: T
)
