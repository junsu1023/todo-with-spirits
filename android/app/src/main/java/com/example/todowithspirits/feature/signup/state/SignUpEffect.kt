package com.example.todowithspirits.feature.signup.state

sealed interface SignUpEffect {
    data object NavigateBack : SignUpEffect
}
