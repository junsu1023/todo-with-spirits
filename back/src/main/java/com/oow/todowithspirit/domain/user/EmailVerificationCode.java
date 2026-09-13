package com.oow.todowithspirit.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static EmailVerificationCode create(Long userId, String email, String code, LocalDateTime expiresAt) {
        EmailVerificationCode evc = new EmailVerificationCode();
        evc.userId = userId;
        evc.email = email;
        evc.code = code;
        evc.expiresAt = expiresAt;
        evc.createdAt = LocalDateTime.now();
        return evc;
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