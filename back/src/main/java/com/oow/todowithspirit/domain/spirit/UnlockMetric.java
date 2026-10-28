package com.oow.todowithspirit.domain.spirit;

public enum UnlockMetric {

    DEFAULT,          // 항상 해금 (기본 제공)
    HABIT_TYPE_COUNT, // 버릇 N종 발견 — 버릇 데이터 모델 구현 전까지는 자동 해금 평가 로직 없음
    MEMORY_COUNT,     // 추억 N장 수집 — 추억 데이터 모델 구현 전까지는 자동 해금 평가 로직 없음
    INTIMACY_STAGE    // 친밀도 N단계 — 친밀도 데이터 모델 구현 전까지는 자동 해금 평가 로직 없음
}
