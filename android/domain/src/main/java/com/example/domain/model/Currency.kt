package com.example.domain.model

import java.time.OffsetDateTime

data class CurrencyHistory(
    val balance: Int,
    val transactions: List<CurrencyTransaction> // 최근 최대 50건
)

data class CurrencyTransaction(
    val id: Long,
    val amount: Int, // 양수=지급, 음수=소비
    val reason: CurrencyReason,
    val createdAt: OffsetDateTime
)

enum class CurrencyReason {
    TASK_COMPLETE, // task 완료 보상
    DECORATION_PURCHASE, // 장식 구매
    SPIRIT_MEET, // 정령 만나기
    UNKNOWN;

    companion object {
        fun fromApiValue(value: String): CurrencyReason =
            entries.find { it.name == value } ?: UNKNOWN
    }
}
