package com.example.domain.repository

import com.example.domain.model.AppLanguage
import com.example.domain.model.DisplaySetting

interface DisplaySettingRepository {
    // Optional 파라미터: 최소 1개 이상 전달, 나머지는 null(변경 없음)로 서버에 그대로 전달된다.
    suspend fun updateDisplaySetting(
        darkMode: Boolean? = null,
        ddayDisplayEnabled: Boolean? = null,
        language: AppLanguage? = null
    ): Result<DisplaySetting>
}
