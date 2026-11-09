package com.example.domain.model

import java.time.OffsetDateTime

data class Subscription(
    val planType: PlanType,
    val startedAt: OffsetDateTime?,
    val expiresAt: OffsetDateTime?,
    val autoRenew: Boolean,
    val premium: Boolean
)

enum class PlanType {
    FREE,
    MONTHLY,
    YEARLY;

    companion object {
        fun fromApiValue(value: String?): PlanType =
            entries.find { it.name == value } ?: FREE
    }
}
