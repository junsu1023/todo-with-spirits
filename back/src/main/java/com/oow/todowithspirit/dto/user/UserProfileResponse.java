package com.oow.todowithspirit.dto.user;

import com.oow.todowithspirit.domain.user.OAuthProvider;
import com.oow.todowithspirit.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileResponse {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final String role;
    private final boolean isPremium;
    private final Long representativeSpiritId;
    private final String loginType;   // "LOCAL" | "SOCIAL"
    private final String provider;    // social login -> provider, email login -> null
    private final String fullname; // 본명
    private final String gender; // 성별
    private final Date birthday; // 생년월일
    private final LocalDateTime createdAt;

    public static UserProfileResponse of(User user, List<OAuthProvider> providers) {
        String provider = providers.isEmpty() ? null : providers.get(0).name();
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.isPremium(),
                user.getRepresentativeSpiritId(),
                provider == null ? "LOCAL" : "SOCIAL",
                provider,
                null,
                null,
                null,
                user.getCreatedAt()
        );
    }
}