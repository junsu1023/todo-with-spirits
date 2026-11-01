package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.domain.auth.GoogleTokenValidator;
import com.oow.todowithspirit.domain.auth.KakaoTokenValidator;
import com.oow.todowithspirit.dto.auth.*;
import com.oow.todowithspirit.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;
    private final KakaoTokenValidator kakaoTokenValidator;
    private final GoogleTokenValidator googleTokenValidator;

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(
            @Email(message = "Invalid email format") @NotBlank(message = "Email is required") @RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(authService.checkEmail(email)));
    }

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
        log.info("[socialLogin] provider: {}, providerUserId: {}", request.getProvider(), request.getProviderUserId());

        String providerToken = extractTokenFromHeader(authHeader);
        String verifiedEmail = validateProviderToken(request.getProvider(), providerToken, request.getProviderUserId());
        LoginResponse response = authService.socialLogin(request, verifiedEmail);
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

    private String validateProviderToken(String provider, String providerToken, String providerUserId) {
        if ("kakao".equalsIgnoreCase(provider)) {
            return kakaoTokenValidator.validate(providerToken, providerUserId);
        }
        if ("google".equalsIgnoreCase(provider)) {
            return googleTokenValidator.validate(providerToken, providerUserId);
        }
        return null;
    }
}
