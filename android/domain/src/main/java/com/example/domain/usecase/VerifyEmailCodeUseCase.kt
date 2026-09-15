package com.example.domain.usecase

import com.example.domain.repository.AuthRepository
import javax.inject.Inject

// 메일로 발송된 6자리 인증코드를 서버에 검증 요청한다 (미인증유저 -> 인증유저)
class VerifyEmailCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: Int): Result<Unit> = authRepository.verifyEmailCode(email, code)
}
