package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.auth.RefreshToken;
import com.oow.todowithspirit.domain.auth.RefreshTokenRepository;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.auth.*;
import com.oow.todowithspirit.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SpiritRepository spiritRepository;
    private final SpiritService spiritService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL, "email", "Email already in use");
        }

        String nickname = StringUtils.hasText(request.getNickname())
                ? request.getNickname()
                : generateDefaultNickname();

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.ofLocalSignup(request.getEmail(), encodedPassword, nickname);
        userRepository.save(user);

        // 2. 기본 정령 생성 및 저장
        spiritService.createDefaultSpirit(user);

        return SignupResponse.from(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtProvider.generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.create(user.getId(), refreshTokenValue, refreshTokenExpiresAt()));

        return LoginResponse.of(accessToken, refreshTokenValue, user);
    }

    @Transactional
    public TokenRefreshResponse reissue(TokenRefreshRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));

        // 이미 revoke된 토큰 → 탈취 가능성, 해당 유저의 모든 토큰 폐기
        if (oldToken.isRevoked()) {
            refreshTokenRepository.deleteAllByUserId(oldToken.getUserId());
            throw new ApiException(ErrorCode.REVOKED_TOKEN);
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new ApiException(ErrorCode.EXPIRED_TOKEN);
        }

        User user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        // 기존 토큰 폐기 후 새 토큰 발급 (rotation)
        oldToken.revoke();

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtProvider.generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.create(user.getId(), newRefreshToken, refreshTokenExpiresAt()));

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public User findOrCreate(SocialLoginRequest request) {
        String provider = request.getProvider().toLowerCase();
        String providerUserId = request.getProviderUserId();

        validateProvider(provider);
        if (!StringUtils.hasText(providerUserId)) {
            throw new BusinessException(ErrorCode.MISSING_PROVIDER_USER_ID, "providerUserId", ErrorCode.MISSING_PROVIDER_USER_ID.getMessage());
        }

        log.debug("[findOrCreate] Searching for exisiting user. provider = {}, providerUserId = {}", provider, providerUserId);
        Users user = usersRepository.findByProviderAndProviderUserId(provider, providerUserId);

        if (user == null) {
            log.info("[findOrCreate] User not found. Create new user");
            user = insertOne(request);
        }
        log.info("[findOrCreate] Proceeding to login. userId = {}", user.getUserId());
        return user;
    }


    private String generateDefaultNickname() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "정령유저_" + suffix;
    }

    private LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpirationMs() / 1000);
    }

    private void validateProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new ApiException(ErrorCode.MISSING_PROVIDER, "provider", ErrorCode.MISSING_PROVIDER.getMessage());
        }

        if (!provider.equals("google") && !provider.equals("kakao") && !provider.equals("apple")) {
            log.warn("[validProvider] Invalid provider [{}]", provider);
            throw new ApiException(ErrorCode.INVALID_PROVIDER, "provider", provider);
        }
    }

    public void updateToken(UUID userId, String refreshToken) {
        log.debug("[updateToken] Updating refresh token. userId = {}", userId);
        UserToken userToken = tokenRepository.findByUserId(userId);
        if (userToken != null) {
            userToken.setRefreshToken(refreshToken);
            userToken.setExpireDate(LocalDateTime.now().plusDays(7)); // 7-days
            log.info("[updateToken] Tokens updated successfully. userId = {}", userId);
        } else {
            log.error("[updateToken] Failed to update tokens. UserToken record not found. userId = {}", userId);
        }
    }
}
