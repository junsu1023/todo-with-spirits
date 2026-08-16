package com.example.data.response

data class SocialLoginResponse(
    val userId: Long,
    val email: String?,
    val nickname: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
