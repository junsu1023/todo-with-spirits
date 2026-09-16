package com.example.domain.usecase

import com.example.domain.model.LoginMethod
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class GetLoginMethodUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): LoginMethod? = authRepository.getLoginMethod()
}
