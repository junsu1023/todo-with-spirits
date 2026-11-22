package com.example.todowithspirits.feature.login.state

sealed interface EmailLoginEffect {
    data object NavigateBack : EmailLoginEffect
    data object NavigateToHome : EmailLoginEffect
    data object NavigateToFindPassword : EmailLoginEffect
}
