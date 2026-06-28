package com.oow.todowithspirit.dto.auth;

import com.oow.todowithspirit.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {

    private final String tokenType;
    private final String accessToken;
    private final String refreshToken;
    private final Long userId;
    private final String email;
    private final String nickname;

    public static LoginResponse of(String accessToken, String refreshToken, User user) {
        return new LoginResponse("Bearer", accessToken, refreshToken,
                user.getId(), user.getEmail(), user.getNickname());
    }
}