package com.example.data.response

data class UserProfileResponse(
    val userId: Long,
    val email: String?,
    val emailVerificationStatus: String,
    val loginType: String,
    val nickname: String,
    val premium: Boolean,
    val provider: String?,
    val representativeSpiritId: Long?,
    val role: String,
    val createdAt: String
)
