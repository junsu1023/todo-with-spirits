package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.spirit.*;
import com.oow.todowithspirit.service.SpiritEggService;
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
    private final SpiritEggService spiritEggService;

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

    @PatchMapping("/{spiritId}/name")
    public ResponseEntity<ApiResponse<Void>> updateSpiritName(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long spiritId,
            @Valid @RequestBody UpdateSpiritNameRequest request) {

        spiritService.updateSpiritName(userId, spiritId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/egg-options")
    public ResponseEntity<ApiResponse<List<EggOfferResponse>>> getEggOptions(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritEggService.getOrCreateOffers(userId)));
    }

    @PostMapping("/egg-options/{offerId}")
    public ResponseEntity<ApiResponse<RepresentativeSpiritResponse>> selectEggOption(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long offerId) {
        return ResponseEntity.ok(ApiResponse.success(spiritEggService.selectEgg(userId, offerId)));
    }
}
