package com.oow.todowithspirit.dto.user;

import com.oow.todowithspirit.domain.user.OAuthProvider;
import com.oow.todowithspirit.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileResponse {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final String role;
    private final boolean isPremium;
    private final Long representativeSpiritId;
    private final String loginType;   // "LOCAL" | "SOCIAL"
    private final String provider;    // social login -> provider, email login -> null
    private final String emailVerificationStatus; // 이메일 인증 상태
    private final LocalDateTime createdAt;

    public static UserProfileResponse of(User user, List<OAuthProvider> providers, boolean isPremium) {
        String provider = providers.isEmpty() ? null : providers.get(0).name();
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                isPremium,
                user.getRepresentativeSpiritId(),
                provider == null ? "LOCAL" : "SOCIAL",
                provider,
                user.getEmailVerificationStatus().name(),
                user.getCreatedAt()
        );
    }
}