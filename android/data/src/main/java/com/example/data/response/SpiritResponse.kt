package com.example.data.response

// 보유 정령 목록 / 대표 정령 조회 응답 공용.
// 활력 경험치 필드명이 목록(energyExp)과 대표 정령(vitalityExp) 문서에서 달라 둘 다 받는다.
// representative는 목록 응답에만 있다.
data class SpiritResponse(
    val id: Long,
    val spiritName: String,
    val stage: Int,
    val exp: Int,
    val focusExp: Int?,
    val energyExp: Int?,
    val vitalityExp: Int?,
    val consistencyExp: Int?,
    val creativityExp: Int?,
    val imageUrl: String?,
    val representative: Boolean?
)

data class MeetSpiritResponse(
    val id: Long,
    val name: String,
    val variant: String,
    val balance: Int
)
