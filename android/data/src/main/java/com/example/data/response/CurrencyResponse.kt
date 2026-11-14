package com.example.data.response

data class CurrencyResponse(
    val balance: Int,
    val transactions: List<CurrencyTransactionResponse> = emptyList()
)

data class CurrencyTransactionResponse(
    val id: Long,
    val amount: Int,
    val reason: String,
    val createdAt: String
)
