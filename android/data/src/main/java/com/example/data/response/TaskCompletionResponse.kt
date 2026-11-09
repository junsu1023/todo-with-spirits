package com.example.data.response

data class TaskCompletionResponse(
    val currencyEarned: Int, // 지급된 재화량. 이미 지급된 경우 0
    val balance: Int // 지급 후 최종 잔액
)
