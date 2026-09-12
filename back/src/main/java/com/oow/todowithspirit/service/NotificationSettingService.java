package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.setting.NotificationSetting;
import com.oow.todowithspirit.domain.setting.NotificationSettingRepository;
import com.oow.todowithspirit.dto.setting.NotificationSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public NotificationSettingResponse getNotificationSettings(Long userId) {
        return NotificationSettingResponse.from(findOrCreate(userId));
    }

    private NotificationSetting findOrCreate(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.createDefault(userId)));
    }
}