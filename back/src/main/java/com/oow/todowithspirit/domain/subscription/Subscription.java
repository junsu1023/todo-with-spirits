package com.oow.todowithspirit.domain.subscription;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private PlanType planType;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    public static Subscription createDefault(Long userId) {
        Subscription subscription = new Subscription();
        subscription.userId = userId;
        subscription.planType = PlanType.FREE;
        subscription.autoRenew = false;
        return subscription;
    }

    public boolean isActive() {
        return planType != PlanType.FREE && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
}