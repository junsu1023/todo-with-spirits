package com.example.domain.model

data class Decoration(
    val id: Long,
    val code: String,
    val name: String,
    val price: Int,
    val imageKey: String?, // Unity 에셋 키
    val imageUrl: String?, // 웹용 에셋이 아직 없으면 null
    val purchased: Boolean,
    val installed: Boolean
)

data class DecorationPurchase(
    val decorationId: Long,
    val balance: Int // 구매 후 잔액
)
