package com.example.todowithspirits.feature.login.state

import com.example.domain.model.SocialProvider

sealed interface LoginEffect {
    data class StartSocialAuth(val provider: SocialProvider) : LoginEffect
    data object NavigateToHome : LoginEffect
    data object NavigateToEmailLogin : LoginEffect
    data object NavigateToSignUp : LoginEffect
}
