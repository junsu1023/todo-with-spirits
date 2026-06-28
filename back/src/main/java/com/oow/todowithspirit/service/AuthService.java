package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.auth.RefreshToken;
import com.oow.todowithspirit.domain.auth.RefreshTokenRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.auth.LoginRequest;
import com.oow.todowithspirit.dto.auth.LoginResponse;
import com.oow.todowithspirit.dto.auth.SignupRequest;
import com.oow.todowithspirit.dto.auth.SignupResponse;
import com.oow.todowithspirit.dto.auth.TokenRefreshRequest;
import com.oow.todowithspirit.dto.auth.TokenRefreshResponse;
import com.oow.todowithspirit.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL, "email", "Email already in use");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.ofLocalSignup(request.getEmail(), encodedPassword, request.getNickname());
        return SignupResponse.from(userRepository.save(user));
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

    private LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpirationMs() / 1000);
    }
}
