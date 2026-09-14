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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    // 회원가입 전 이메일 인증. 아직 users 테이블에 계정이 없는 상태에서 호출됨
    @Transactional
    public void sendSignupVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL, "email", "An account with this email already exists.");
        }
        issueAndSendCode(email);
    }

    // 로그인된 계정의 이메일 재인증 (예: 이메일 변경 후 재인증)
    @Transactional
    public void resendForAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        issueAndSendCode(user.getEmail());
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

        verificationCode.verify();

        // 이미 가입된 계정의 이메일 재인증인 경우 계정 상태도 함께 갱신
        userRepository.findByEmail(email).ifPresent(User::verifiedEmail);
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationCodeRepository.existsByEmailAndVerifiedAtIsNotNull(email);
    }

    @Transactional
    public void consumeVerification(String email) {
        emailVerificationCodeRepository.deleteAllByEmail(email);
    }

    private void issueAndSendCode(String email) {
        // 이전에 발급된 코드는 무효화하고 새로 발급
        log.info("[issueAndSendCode] Delete previous email verification codes. email: {}", email);
        emailVerificationCodeRepository.deleteAllByEmail(email);

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(codeExpirationMs / 1000);
        emailVerificationCodeRepository.save(EmailVerificationCode.create(email, code, expiresAt));
        log.debug("[issueAndSendCode] Created new verification code. email: {}", email);

        sendMail(email, code);
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