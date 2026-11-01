package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.spirit.*;
import com.oow.todowithspirit.service.SpiritHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spirit")
@RequiredArgsConstructor
public class SpiritHistoryController {

    private final SpiritHistoryService spiritHistoryService;

    @GetMapping("/{spiritId}/daily-logs")
    public ResponseEntity<ApiResponse<List<SpiritDailyLogResponse>>> getDailyLogs(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long spiritId) {
        return ResponseEntity.ok(ApiResponse.success(spiritHistoryService.getDailyLogs(userId, spiritId)));
    }
}
