package com.oow.todowithspirit.dto.subscription;

import com.oow.todowithspirit.domain.subscription.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionResponse {

    private String planType;        // FREE, MONTHLY, YEARLY
    private boolean isPremium;      // 유료 구독 활성 여부 (만료일 반영)
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private boolean autoRenew;

    public static SubscriptionResponse from(Subscription subscription) {
        return SubscriptionResponse.builder()
                .planType(subscription.getPlanType().name())
                .isPremium(subscription.isActive())
                .startedAt(subscription.getStartedAt())
                .expiresAt(subscription.getExpiresAt())
                .autoRenew(subscription.isAutoRenew())
                .build();
    }
}