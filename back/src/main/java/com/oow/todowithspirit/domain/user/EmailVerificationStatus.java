package com.oow.todowithspirit.domain.user;

public enum EmailVerificationStatus {
    UNVERIFIED,          // 미인증
    VERIFIED,             // 인증완료
    NEEDS_REVERIFICATION  // 재인증필요
}