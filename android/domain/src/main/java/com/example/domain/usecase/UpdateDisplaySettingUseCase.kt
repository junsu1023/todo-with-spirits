package com.example.domain.usecase

import com.example.domain.model.AppLanguage
import com.example.domain.model.DisplaySetting
import com.example.domain.repository.DisplaySettingRepository
import javax.inject.Inject

class UpdateDisplaySettingUseCase @Inject constructor(
    private val displaySettingRepository: DisplaySettingRepository
) {
    suspend operator fun invoke(
        darkMode: Boolean? = null,
        ddayDisplayEnabled: Boolean? = null,
        language: AppLanguage? = null
    ): Result<DisplaySetting> = displaySettingRepository.updateDisplaySetting(darkMode, ddayDisplayEnabled, language)
}
