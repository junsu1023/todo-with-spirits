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

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SpiritHistoryResponse>>> getHistory(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritHistoryService.getHistory(userId)));
    }

    @GetMapping("/{spiritId}/daily-logs")
    public ResponseEntity<ApiResponse<List<SpiritDailyLogResponse>>> getDailyLogs(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long spiritId) {
        return ResponseEntity.ok(ApiResponse.success(spiritHistoryService.getDailyLogs(userId, spiritId)));
    }

    @GetMapping("/{spiritId}/todo-summary")
    public ResponseEntity<ApiResponse<SpiritTodoSummaryResponse>> getTodoSummary(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long spiritId) {
        return ResponseEntity.ok(ApiResponse.success(spiritHistoryService.getTodoSummary(userId, spiritId)));
    }

    @GetMapping("/letters")
    public ResponseEntity<ApiResponse<List<SpiritLetterResponse>>> getLetters(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritHistoryService.getLetters(userId)));
    }

    @PatchMapping("/letters/{letterId}/read")
    public ResponseEntity<ApiResponse<Void>> markLetterRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long letterId) {
        spiritHistoryService.markLetterRead(userId, letterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/discoveries")
    public ResponseEntity<ApiResponse<List<SpiritDiscoveryResponse>>> getDiscoveries(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritHistoryService.getDiscoveries(userId)));
    }
}
