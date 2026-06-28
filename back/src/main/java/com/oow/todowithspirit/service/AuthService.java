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

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenExpirationMs() / 1000);
        refreshTokenRepository.save(RefreshToken.create(user.getId(), refreshTokenValue, expiresAt));

        return LoginResponse.of(accessToken, refreshTokenValue, user);
    }
}
