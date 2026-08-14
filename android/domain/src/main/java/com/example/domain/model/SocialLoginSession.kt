package com.example.domain.model

data class SocialLoginSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val isPremium: Boolean,
    val isNewUser: Boolean
)
