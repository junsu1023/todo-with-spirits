package com.example.domain.model

import java.time.LocalDateTime

data class UserProfile(
    val userId: Long,
    val email: String?, // 소셜로그인 시 nullable
    val emailVerificationStatus: String, // Enum EmailVerificationStatus (예: VERIFIED). 전체 값 집합이 확인되지 않아 원문 그대로 보관
    val loginType: LoginType,
    val nickname: String,
    val premium: Boolean,
    val provider: SocialProvider?, // 소셜로그인 시에만 값 존재
    val representativeSpiritId: Long?,
    val role: UserRole,
    val createdAt: LocalDateTime
)

enum class LoginType {
    LOCAL, // 이메일 로그인
    SOCIAL // 소셜 로그인
}

enum class UserRole {
    USER,
    ADMIN
}
