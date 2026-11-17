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
    val imageUrl: String
)

// 정령도감의 한 칸 (정령 종류 1개). 미보유 종류는 spiritId, spiritName이 null이다.
data class SpiritCollectionItem(
    val speciesId: Long,
    val speciesName: String,
    val imageUrl: String?, // 웹용 에셋이 아직 없으면 null
    val isOwned: Boolean,
    val spiritId: Long?, // 보유한 정령 개체 id. 만난 순서대로 커진다
    val spiritName: String?,
    val affectionLevel: Int, // 호감도 단계 (1~5), 미보유면 0
    val isRepresentative: Boolean
)

// 새 정령 만나기 결과. 새로 만난 정령은 자동으로 대표 정령이 된다.
data class MetSpirit(
    val id: Long,
    val name: String,
    val variant: String,
    val balance: Int // 소비 후 잔액
)
