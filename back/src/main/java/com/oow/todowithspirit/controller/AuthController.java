package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.domain.auth.KakaoTokenValidator;
import com.oow.todowithspirit.dto.auth.*;
import com.oow.todowithspirit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final KakaoTokenValidator kakaoTokenValidator;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/social/login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SocialLoginRequest request) {
        log.info("[socialLogin] provider={}, providerUserId={}", request.getProvider(), request.getProviderUserId());

        String providerToken = extractTokenFromHeader(authHeader);
        validateProviderToken(request.getProvider(), providerToken, request.getProviderUserId());

        LoginResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> reissue(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.reissue(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String extractTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("[extractTokenFromHeader] Invalid Authorization header format");
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return header.substring(7);
    }

    private void validateProviderToken(String provider, String providerToken, String providerUserId) {
        if ("kakao".equalsIgnoreCase(provider)) {
            kakaoTokenValidator.validate(providerToken, providerUserId);
        }
    }
}
