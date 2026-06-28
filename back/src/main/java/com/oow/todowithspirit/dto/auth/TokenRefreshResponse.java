package com.oow.todowithspirit.dto.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenRefreshResponse {

    private final String tokenType;
    private final String accessToken;
    private final String refreshToken;

    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse("Bearer", accessToken, refreshToken);
    }
}