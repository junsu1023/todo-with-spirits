package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.decoration.DecorationPurchaseResponse;
import com.oow.todowithspirit.dto.decoration.DecorationResponse;
import com.oow.todowithspirit.service.DecorationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decorations")
@RequiredArgsConstructor
public class DecorationController {

    private final DecorationService decorationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DecorationResponse>>> getDecorations(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(decorationService.getDecorations(userId)));
    }

    @PostMapping("/{decorationId}/purchase")
    public ResponseEntity<ApiResponse<DecorationPurchaseResponse>> purchase(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long decorationId) {
        long balance = decorationService.purchase(userId, decorationId);
        return ResponseEntity.ok(ApiResponse.success(new DecorationPurchaseResponse(decorationId, balance)));
    }

    @PostMapping("/{decorationId}/install")
    public ResponseEntity<ApiResponse<Void>> install(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long decorationId) {
        decorationService.install(userId, decorationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{decorationId}/install")
    public ResponseEntity<ApiResponse<Void>> uninstall(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long decorationId) {
        decorationService.uninstall(userId, decorationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
