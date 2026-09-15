package com.example.domain.usecase

import com.example.domain.repository.AuthRepository
import javax.inject.Inject

// 회원가입 전(비로그인) 단계에서 이메일 인증 메일을 발송한다.
// 로그인 상태에서의 재발송은 ResendEmailVerificationUseCase를 사용한다.
class SendEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> = authRepository.sendEmailVerification(email)
}
