package com.example.domain.model

data class LoginSession(
    val userId: Long,
    val email: String,
    val nickname: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)