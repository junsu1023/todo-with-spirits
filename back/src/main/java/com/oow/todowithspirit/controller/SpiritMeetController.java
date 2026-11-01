package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.spirit.SpiritMeetResponse;
import com.oow.todowithspirit.dto.spirit.SpiritVariantResponse;
import com.oow.todowithspirit.service.SpiritMeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spirit")
@RequiredArgsConstructor
public class SpiritMeetController {

    private final SpiritMeetService spiritMeetService;

    @GetMapping("/variants")
    public ResponseEntity<ApiResponse<List<SpiritVariantResponse>>> getVariants(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritMeetService.getVariants(userId)));
    }

    @PostMapping("/meet")
    public ResponseEntity<ApiResponse<SpiritMeetResponse>> meet(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritMeetService.meet(userId)));
    }
}
