package com.example.domain.model

data class NewTodo(
    val title: String,
    val isAllDay: Boolean,
    val endDateTime: String,
    val isImportant: Boolean = false,
    val notificationType: AlarmOption = AlarmOption.NONE,
    val category: String = "NONE",
    val isPublic: Boolean = false,
    val memo: String? = null
)
