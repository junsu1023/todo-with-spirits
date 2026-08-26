package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.user.EmailVerificationToken;
import com.oow.todowithspirit.domain.user.EmailVerificationTokenRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${email-verification.token-expiration}")
    private long tokenExpirationMs;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public void sendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        // 이전에 발급된 미사용 토큰은 무효화
        log.info("[Verify email] Delete email tokens. userId: {}", userId);
        emailVerificationTokenRepository.deleteAllByUserId(userId);

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenExpirationMs / 1000);
        emailVerificationTokenRepository.save(EmailVerificationToken.create(userId, user.getEmail(), token, expiresAt));
        log.debug("[Verify email] Create new email veirification token. userId: {}, email: {}", userId, user.getEmail());

        sendMail(user.getEmail(), token);
    }

    @Transactional
    public void verifyToken(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Email is already verified");
        }
        if (verificationToken.isExpired()) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN, "Verification token has expired");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        // 토큰 발급 이후 이메일이 다시 변경된 경우 이 토큰은 더 이상 유효하지 않음
        if (!verificationToken.getEmail().equals(user.getEmail())) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Email has changed since this link was issued");
        }

        user.verifiedEmail();
        verificationToken.verify();
    }

    private void sendMail(String toEmail, String token) {
        String verificationLink = baseUrl + "/api/user/email/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[투두위드스피릿] 이메일 인증을 완료해주세요");
        message.setText("아래 링크를 클릭하여 이메일 인증을 완료해주세요.\n\n" + verificationLink
                + "\n\n본인이 요청하지 않았다면 이 메일을 무시하세요.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("[sendVerificationEmail] Failed to send verification email to {}", toEmail, e);
        }
    }
}