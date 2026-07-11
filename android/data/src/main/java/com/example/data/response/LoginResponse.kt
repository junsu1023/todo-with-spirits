package com.example.data.response

data class LoginResponse(
    val accessToken: String,
    val email: String,
    val nickname: String,
    val refreshToken: String,
    val tokenType: String,
    val userId: Long
)