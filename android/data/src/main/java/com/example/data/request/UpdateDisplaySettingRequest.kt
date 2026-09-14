package com.example.data.request

data class UpdateDisplaySettingRequest(
    val darkMode: Boolean? = null,
    val ddayDisplayEnabled: Boolean? = null,
    val language: String? = null
)
