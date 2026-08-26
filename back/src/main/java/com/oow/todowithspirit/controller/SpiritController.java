package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.spirit.RepresentativeSpiritResponse;
import com.oow.todowithspirit.dto.spirit.SpiritResponse;
import com.oow.todowithspirit.dto.spirit.UpdateRepresentativeSpiritRequest;
import com.oow.todowithspirit.service.SpiritService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<RepresentativeSpiritResponse>> getRepresentativeSpirit(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                ApiResponse.success(spiritService.getRepresentativeSpirit(userId))
        );
    }

    @PatchMapping("/current")
    public ResponseEntity<ApiResponse<Void>> updateRepresentativeSpirit(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateRepresentativeSpiritRequest request) {

        spiritService.updateRepresentativeSpirit(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
