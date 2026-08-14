package com.example.data.request

data class SocialLoginRequest(
    val provider: String,
    val providerUserId: String,
    val email: String? = null
)
