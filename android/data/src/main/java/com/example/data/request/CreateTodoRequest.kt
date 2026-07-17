package com.example.data.request

data class CreateTodoRequest(
    val title: String,
    val isAllDay: Boolean,
    val endDateTime: String,
    val isImportant: Boolean = false,
    val notificationType: String = "NONE",
    val category: String = "NONE",
    val isPublic: Boolean = false,
    val memo: String? = null
)
