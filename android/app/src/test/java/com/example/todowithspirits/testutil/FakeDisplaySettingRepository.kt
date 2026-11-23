package com.example.todowithspirits.testutil

import com.example.domain.model.AppLanguage
import com.example.domain.model.DisplaySetting
import com.example.domain.repository.DisplaySettingRepository

// 디스플레이 설정 테스트용 DisplaySettingRepository 더블.
class FakeDisplaySettingRepository(
    private val setting: DisplaySetting,
    private val updateFailure: Throwable? = null
) : DisplaySettingRepository {
    data class UpdateRequest(
        val darkMode: Boolean?,
        val ddayDisplayEnabled: Boolean?,
        val language: AppLanguage?
    )

    val updateRequests = mutableListOf<UpdateRequest>()

    override suspend fun getDisplaySetting(): Result<DisplaySetting> = Result.success(setting)

    override suspend fun updateDisplaySetting(
        darkMode: Boolean?,
        ddayDisplayEnabled: Boolean?,
        language: AppLanguage?
    ): Result<DisplaySetting> {
        updateRequests += UpdateRequest(darkMode, ddayDisplayEnabled, language)
        return updateFailure?.let { Result.failure(it) } ?: Result.success(setting)
    }
}
