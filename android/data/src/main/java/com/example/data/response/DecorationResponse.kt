package com.example.data.response

data class DecorationResponse(
    val id: Long,
    val code: String,
    val name: String,
    val price: Int,
    val image: AssetResponse?,
    val purchased: Boolean,
    val installed: Boolean
)

data class DecorationPurchaseResponse(
    val decorationId: Long,
    val balance: Int
)
