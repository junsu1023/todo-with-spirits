package com.example.domain.usecase

import com.example.domain.model.DisplaySetting
import com.example.domain.repository.DisplaySettingRepository
import javax.inject.Inject

class GetDisplaySettingUseCase @Inject constructor(
    private val displaySettingRepository: DisplaySettingRepository
) {
    suspend operator fun invoke(): Result<DisplaySetting> = displaySettingRepository.getDisplaySetting()
}
