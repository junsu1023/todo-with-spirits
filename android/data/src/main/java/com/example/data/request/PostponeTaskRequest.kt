package com.example.data.request

data class PostponeTaskRequest(
    // routine은 required, schedule은 무시됨 — 미루려는 발생(occurrence)의 변경 전 날짜.
    val originalDate: String?,
    val newDate: String?, // null이면 기존 값 유지
    val newTime: String?  // null이면 기존 값 유지
)
