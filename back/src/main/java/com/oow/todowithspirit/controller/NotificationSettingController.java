package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.setting.NotificationSettingResponse;
import com.oow.todowithspirit.dto.setting.NotificationSettingUpdateRequest;
import com.oow.todowithspirit.service.NotificationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setting/notification")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getNotificationSettings(
            @AuthenticationPrincipal Long userId) {
        NotificationSettingResponse response = notificationSettingService.getNotificationSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}