package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.notification.NotificationPageResponse;
import com.oow.todowithspirit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor) {
        NotificationPageResponse response = notificationService.getNotifications(userId, size, cursor);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}