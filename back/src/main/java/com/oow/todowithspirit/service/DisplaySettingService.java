package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.setting.UserSetting;
import com.oow.todowithspirit.domain.setting.UserSettingRepository;
import com.oow.todowithspirit.dto.setting.DisplaySettingResponse;
import com.oow.todowithspirit.dto.setting.DisplaySettingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisplaySettingService {

    private final UserSettingRepository userSettingRepository;

    @Transactional
    public DisplaySettingResponse getDisplaySettings(Long userId) {
        return DisplaySettingResponse.from(findOrCreate(userId));
    }

    @Transactional
    public DisplaySettingResponse updateDisplaySettings(Long userId, DisplaySettingUpdateRequest request) {
        UserSetting setting = findOrCreate(userId);

        setting.updateDisplay(
                request.getDarkMode(),
                request.getLanguage(),
                request.getDdayDisplayEnabled()
        );

        return DisplaySettingResponse.from(setting);
    }

    private UserSetting findOrCreate(Long userId) {
        return userSettingRepository.findByUserId(userId)
                .orElseGet(() -> userSettingRepository.save(UserSetting.createDefault(userId)));
    }
}