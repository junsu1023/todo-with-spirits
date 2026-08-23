package com.oow.todowithspirit.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static EmailVerificationToken create(Long userId, String email, String token, LocalDateTime expiresAt) {
        EmailVerificationToken evt = new EmailVerificationToken();
        evt.userId = userId;
        evt.email = email;
        evt.token = token;
        evt.expiresAt = expiresAt;
        evt.createdAt = LocalDateTime.now();
        return evt;
    }

    public boolean isUsed() {
        return verifiedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public void verify() {
        this.verifiedAt = LocalDateTime.now();
    }
}