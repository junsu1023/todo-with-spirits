package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.auth.RefreshToken;
import com.oow.todowithspirit.domain.auth.RefreshTokenRepository;
import com.oow.todowithspirit.domain.user.*;
import com.oow.todowithspirit.dto.auth.*;
import com.oow.todowithspirit.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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
    public LoginResponse socialLogin(SocialLoginRequest request) {
        User user = findOrCreate(request);

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtProvider.generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.create(user.getId(), refreshTokenValue, refreshTokenExpiresAt()));

        log.info("[socialLogin] Login successful for userId={}", user.getId());
        return LoginResponse.of(accessToken, refreshTokenValue, user);
    }

    @Transactional
    public TokenRefreshResponse reissue(TokenRefreshRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));

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

    private User findOrCreate(SocialLoginRequest request) {
        String providerLower = request.getProvider().toLowerCase();
        validateProvider(providerLower);
        OAuthProvider provider = OAuthProvider.valueOf(providerLower.toUpperCase());

        log.debug("[findOrCreate] provider: {}, providerUserId: {}", provider, request.getProviderUserId());
        return userSocialAccountRepository
                .findByProviderAndProviderUserId(provider, request.getProviderUserId())
                .map(UserSocialAccount::getUser)
                .orElseGet(() -> {
                    log.info("[findOrCreate] No existing user found, creating new social user");
                    return createSocialUser(request, provider);
                });
    }

    private User createSocialUser(SocialLoginRequest request, OAuthProvider provider) {
        String nickname = StringUtils.hasText(request.getEmail())
                ? request.getEmail().split("@")[0]
                : generateDefaultNickname();
        User user = User.ofSocialSignup(request.getEmail(), nickname);
        userRepository.save(user);
        userSocialAccountRepository.save(new UserSocialAccount(user, provider, request.getProviderUserId()));
        spiritService.createDefaultSpirit(user);
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
            throw new ApiException(ErrorCode.MISSING_PROVIDER);
        }
        if (!provider.equals("google") && !provider.equals("kakao") && !provider.equals("apple")) {
            log.warn("[validateProvider] Invalid provider [{}]", provider);
            throw new ApiException(ErrorCode.INVALID_PROVIDER, "provider", provider);
        }
    }
}
