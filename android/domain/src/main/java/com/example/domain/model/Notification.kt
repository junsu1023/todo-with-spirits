package com.example.domain.model

import java.time.LocalDateTime

data class NotificationPage(
    val items: List<NotificationItem>,
    val nextCursor: String?,
    val hasNext: Boolean
)

data class NotificationItem(
    val id: Long,
    val category: NotificationCategory,
    val content: String,
    val createdAt: LocalDateTime,
    val read: Boolean
)

enum class NotificationCategory(val displayName: String) {
    SYSTEM("시스템 알림"),
    EVENT("이벤트"),
    SPIRIT("정령의 숲");

    companion object {
        fun fromApiValue(value: String): NotificationCategory =
            entries.find { it.name == value } ?: SYSTEM
    }
}
