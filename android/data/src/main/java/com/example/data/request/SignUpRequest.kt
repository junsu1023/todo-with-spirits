package com.example.data.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String? = null
)
