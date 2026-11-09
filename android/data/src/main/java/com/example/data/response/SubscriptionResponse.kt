package com.example.data.response

data class SubscriptionResponse(
    val planType: String,
    val startedAt: String?,
    val expiresAt: String?,
    val autoRenew: Boolean,
    val premium: Boolean
)
