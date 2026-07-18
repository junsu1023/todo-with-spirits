package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.spirit.SpiritResponse;
import com.oow.todowithspirit.service.SpiritService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spirit")
@RequiredArgsConstructor
public class SpiritController {

    private final SpiritService spiritService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<SpiritResponse>>> getMySpirits(
            @AuthenticationPrincipal Long userId) {
        List<SpiritResponse> response = spiritService.getUserSpirits(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
