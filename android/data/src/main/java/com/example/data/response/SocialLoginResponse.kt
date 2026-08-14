package com.example.data.response

data class SocialLoginResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val premium: Boolean,
    val isNewUser: Boolean
)
