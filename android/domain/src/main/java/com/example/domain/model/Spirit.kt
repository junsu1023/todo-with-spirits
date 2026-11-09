package com.example.domain.model

data class Spirit(
    val id: Long,
    val spiritName: String,
    val stage: Int, // 성장단계
    val exp: Int,
    // 정령 속성 별 성장값
    val focusExp: Int,
    val energyExp: Int,
    val consistencyExp: Int,
    val creativityExp: Int,
    val imageUrl: String,
    val representative: Boolean // 대표정령 여부. 대표 정령 조회 응답에는 없어서 true로 채운다
)

// 새 정령 만나기 결과. 새로 만난 정령은 자동으로 대표 정령이 된다.
data class MetSpirit(
    val id: Long,
    val name: String,
    val variant: String,
    val balance: Int // 소비 후 잔액
)
