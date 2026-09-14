package com.oow.todowithspirit.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByEmailAndCode(String email, String code);

    boolean existsByEmailAndVerifiedAtIsNotNull(String email);

    void deleteAllByEmail(String email);
}