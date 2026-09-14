package com.example.data.repository

import com.example.data.datasource.SettingRemoteDataSource
import com.example.data.mapper.toDomain
import com.example.domain.model.AppLanguage
import com.example.domain.model.DisplaySetting
import com.example.domain.repository.DisplaySettingRepository
import javax.inject.Inject

class DisplaySettingRepositoryImpl @Inject constructor(
    private val settingRemoteDataSource: SettingRemoteDataSource
) : DisplaySettingRepository {
    override suspend fun getDisplaySetting(): Result<DisplaySetting> =
        settingRemoteDataSource.getDisplaySetting().mapCatching { it.toDomain() }

    override suspend fun updateDisplaySetting(
        darkMode: Boolean?,
        ddayDisplayEnabled: Boolean?,
        language: AppLanguage?
    ): Result<DisplaySetting> =
        settingRemoteDataSource.updateDisplaySetting(darkMode, ddayDisplayEnabled, language?.apiValue)
            .mapCatching { it.toDomain() }
}
