package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.domain.task.CategoryType;
import com.oow.todowithspirit.dto.spirit.ActionPlaceResponse;
import com.oow.todowithspirit.dto.spirit.CategoryActionResponse;
import com.oow.todowithspirit.dto.spirit.HomeObjectResponse;
import com.oow.todowithspirit.service.SpiritContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spirit")
@RequiredArgsConstructor
public class SpiritContentController {

    private final SpiritContentService spiritContentService;

    @GetMapping("/action-places")
    public ResponseEntity<ApiResponse<List<ActionPlaceResponse>>> getActionPlaces() {
        return ResponseEntity.ok(ApiResponse.success(spiritContentService.getActionPlaces()));
    }

    @GetMapping("/category-actions")
    public ResponseEntity<ApiResponse<List<CategoryActionResponse>>> getCategoryActions(
            @RequestParam(required = false) CategoryType category) {
        return ResponseEntity.ok(ApiResponse.success(spiritContentService.getCategoryActions(category)));
    }

    @GetMapping("/home-objects")
    public ResponseEntity<ApiResponse<List<HomeObjectResponse>>> getHomeObjects(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spiritContentService.getHomeObjects(userId)));
    }

    @PostMapping("/home-objects/{objectId}/install")
    public ResponseEntity<ApiResponse<Void>> installHomeObject(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long objectId) {
        spiritContentService.installHomeObject(userId, objectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/home-objects/{objectId}/install")
    public ResponseEntity<ApiResponse<Void>> uninstallHomeObject(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long objectId) {
        spiritContentService.uninstallHomeObject(userId, objectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
