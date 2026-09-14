package com.oow.todowithspirit.dto.auth;

import com.oow.todowithspirit.domain.user.OAuthProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailCheckResponse {

    private final boolean registered;
    private final boolean hasPassword;
    private final List<OAuthProvider> socialProviders;

    public static EmailCheckResponse notRegistered() {
        return new EmailCheckResponse(false, false, List.of());
    }

    public static EmailCheckResponse of(boolean hasPassword, List<OAuthProvider> socialProviders) {
        return new EmailCheckResponse(true, hasPassword, socialProviders);
    }
}