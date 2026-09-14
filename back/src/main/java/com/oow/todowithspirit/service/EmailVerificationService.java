package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final JavaMailSender mailSender;

    @Value("${email-verification.code-expiration-ms}")
    private long codeExpirationMs;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public void sendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        // 이전에 발급된 미사용 코드는 무효화
        log.info("[sendVerificationEmail] Delete previous email verification codes. userId: {}", userId);
        emailVerificationCodeRepository.deleteAllByUserId(userId);

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(codeExpirationMs / 1000);
        emailVerificationCodeRepository.save(EmailVerificationCode.create(userId, user.getEmail(), code, expiresAt));
        log.debug("[sendVerificationEmail] Created new verification code. userId: {}, email: {}", userId, user.getEmail());

        sendMail(user.getEmail(), code);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        EmailVerificationCode verificationCode = emailVerificationCodeRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Invalid verification code"));

        if (verificationCode.isUsed()) {
            throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Email is already verified");
        }
        if (verificationCode.isExpired()) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN, "Verification code has expired");
        }

        User user = userRepository.findById(verificationCode.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        // 코드 발급 이후 이메일이 다시 변경된 경우 이 코드는 더 이상 유효하지 않음
        if (!verificationCode.getEmail().equals(user.getEmail())) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Email has changed since this code was issued");
        }

        user.verifiedEmail();
        verificationCode.verify();
    }

    /**
     * 인증 만료 시간이 지났는데도 인증되지 않은 회원가입 건은 계정 자체를 삭제한다.
     * (소셜 로그인 유저는 인증 코드가 발급되지 않으므로 영향 없음)
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deleteExpiredUnverifiedUsers() {
        List<Long> userIds = emailVerificationCodeRepository.findUserIdsWithExpiredUnverifiedCode(LocalDateTime.now());
        if (userIds.isEmpty()) {
            return;
        }

        List<User> usersToDelete = userRepository.findAllById(userIds).stream()
                .filter(user -> user.getEmailVerificationStatus() != EmailVerificationStatus.VERIFIED)
                .toList();

        if (usersToDelete.isEmpty()) {
            return;
        }

        userRepository.deleteAll(usersToDelete);
        log.info("[deleteExpiredUnverifiedUsers] Deleted {} unverified user(s) past code expiration", usersToDelete.size());
    }

    private String generateCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    private void sendMail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[Todo with Spirit] 이메일 인증번호를 확인해주세요");
        message.setText("아래 인증번호를 입력하여 이메일 인증을 완료해주세요.\n\n" + code
                + "\n\n인증번호는 5분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시하세요.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("[sendMail] Failed to send verification email to {}", toEmail, e);
        }
    }
}