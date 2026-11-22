package com.example.todowithspirits.feature.login.state

import com.example.domain.model.SocialProvider
import com.example.todowithspirits.client.SocialAuthUser

sealed interface LoginEvent {
    data object KakaoLoginClicked : LoginEvent
    data object GoogleLoginClicked : LoginEvent
    data object EmailLoginClicked : LoginEvent
    data object SignUpClicked : LoginEvent
    data class SocialAuthFinished(val provider: SocialProvider, val result: Result<SocialAuthUser>) : LoginEvent
}
