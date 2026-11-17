package com.example.data.response

// 장식·정령 이미지 공용. key는 Unity 에셋 키, url은 웹용 에셋이 아직 없으면 null이다.
data class AssetResponse(
    val key: String?,
    val url: String?
)
