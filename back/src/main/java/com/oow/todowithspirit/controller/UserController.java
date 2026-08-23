package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.dto.user.EmailUpdateRequest;
import com.oow.todowithspirit.dto.user.UserProfileResponse;
import com.oow.todowithspirit.dto.user.UserProfileUpdateRequest;
import com.oow.todowithspirit.service.EmailVerificationService;
import com.oow.todowithspirit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, request)));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyEmail(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody EmailUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateEmail(userId, request)));
    }

    @PostMapping("/me/email/verify/resend")
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(
            @AuthenticationPrincipal Long userId) {
        emailVerificationService.sendVerificationEmail(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyToken(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}