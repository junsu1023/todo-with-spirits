package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.setting.DisplaySettingResponse;
import com.oow.todowithspirit.dto.setting.DisplaySettingUpdateRequest;
import com.oow.todowithspirit.service.DisplaySettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setting/display")
@RequiredArgsConstructor
public class DisplaySettingController {

    private final DisplaySettingService displaySettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<DisplaySettingResponse>> getDisplaySettings(
            @AuthenticationPrincipal Long userId) {
        DisplaySettingResponse response = displaySettingService.getDisplaySettings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}