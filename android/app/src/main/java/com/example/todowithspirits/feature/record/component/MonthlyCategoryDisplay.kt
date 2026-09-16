package com.example.todowithspirits.feature.record.component

import com.example.domain.model.CategoryOption

// 서버 Category enum 문자열을 화면에 표시할 한글 라벨로 변환
fun String.toCategoryDisplayName(): String =
    (CategoryOption.entries.find { it.name == this } ?: CategoryOption.NONE).displayName
