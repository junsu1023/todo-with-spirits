package com.example.data.request

data class UpdateUserProfileRequest(
    val nickname: String? = null,
    val representativeSpiritId: Long? = null
)
