package com.oow.todowithspirit.dto.auth;

import com.oow.todowithspirit.domain.user.OAuthProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailCheckResponse {

    private final boolean registered;
    private final OAuthProvider provider;

    public static EmailCheckResponse notRegistered() {
        return new EmailCheckResponse(false, null);
    }

    public static EmailCheckResponse of(OAuthProvider provider) {
        return new EmailCheckResponse(true, provider);
    }
}