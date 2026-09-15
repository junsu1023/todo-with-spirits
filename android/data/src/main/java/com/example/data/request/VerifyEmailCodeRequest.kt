package com.example.data.request

data class VerifyEmailCodeRequest(
    val email: String,
    val code: Int
)
