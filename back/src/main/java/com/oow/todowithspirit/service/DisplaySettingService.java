package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.setting.UserSetting;
import com.oow.todowithspirit.domain.setting.UserSettingRepository;
import com.oow.todowithspirit.dto.setting.DisplaySettingResponse;
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

    private UserSetting findOrCreate(Long userId) {
        return userSettingRepository.findByUserId(userId)
                .orElseGet(() -> userSettingRepository.save(UserSetting.createDefault(userId)));
    }
}