package com.oow.todowithspirit.domain.user;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_verification_status", nullable = false, length = 30)
    private EmailVerificationStatus emailVerificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean isPremium;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "representative_spirit_id")
    private Long representativeSpiritId;

    public static User ofLocalSignup(String email, String encodedPassword, String nickname) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.nickname = nickname;
        user.role = UserRole.USER;
        user.isPremium = false;
        // 회원가입 이전에 이메일 인증을 완료해야 하므로 가입 시점에는 항상 인증된 상태
        user.emailVerificationStatus = EmailVerificationStatus.VERIFIED;
        return user;
    }

    public static User ofSocialSignup(String email, String nickname, boolean isVerified) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.role = UserRole.USER;
        user.isPremium = false;
        user.emailVerificationStatus = isVerified ? EmailVerificationStatus.VERIFIED : EmailVerificationStatus.UNVERIFIED;
        return user;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateEmail(String email) {
        this.email = email;
        this.emailVerificationStatus = EmailVerificationStatus.NEEDS_REVERIFICATION;
    }

    public void verifiedEmail() {
        this.emailVerificationStatus = EmailVerificationStatus.VERIFIED;
    }

    public void updateProfile(String nickname, Long representativeSpiritId) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (representativeSpiritId != null) {
            this.representativeSpiritId = representativeSpiritId;
        }
    }
}