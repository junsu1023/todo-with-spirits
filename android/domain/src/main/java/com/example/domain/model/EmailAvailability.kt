package com.example.domain.model

data class EmailAvailability(
    val provider: SocialProvider?, // 소셜 계정으로 가입된 경우에만 값이 있음
    val registered: Boolean // 이미 가입된 이메일인지 여부
)
