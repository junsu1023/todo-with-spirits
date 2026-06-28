package com.oow.todowithspirit.dto.auth;

import com.oow.todowithspirit.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupResponse {

    private final Long userId;
    private final String email;
    private final String nickname;

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}