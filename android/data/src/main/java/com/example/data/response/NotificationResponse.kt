package com.example.data.response

data class NotificationPageResponse(
    val content: List<NotificationItemResponse>,
    val nextCursor: String?,
    val hasNext: Boolean
)

data class NotificationItemResponse(
    val id: Long,
    val category: String,
    val content: String,
    val createdAt: String,
    val read: Boolean
)
