package com.example.data.response

// 대표 정령 조회 응답.
data class SpiritResponse(
    val id: Long,
    val spiritName: String,
    val stage: Int,
    val exp: Int,
    val focusExp: Int?,
    val vitalityExp: Int?,
    val consistencyExp: Int?,
    val creativityExp: Int?,
    val imageUrl: String?
)

// 정령도감의 한 칸 (정령 종류 1개). 미보유 종류는 정령 개체 관련 값이 모두 null이다.
data class SpiritCollectionResponse(
    val speciesId: Long,
    val speciesName: String,
    val speciesDescription: String?,
    val image: AssetResponse?,
    val isOwned: Boolean,
    val spiritId: Long?,
    val spiritName: String?,
    val affection: Int?,
    val affectionLevel: Int?,
    val isImportant: Boolean?,
    val isRepresentative: Boolean?
)

data class MeetSpiritResponse(
    val id: Long,
    val name: String,
    val variant: String,
    val balance: Int
)
