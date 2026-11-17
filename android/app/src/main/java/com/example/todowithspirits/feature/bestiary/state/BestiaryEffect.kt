package com.example.todowithspirits.feature.bestiary.state

sealed interface BestiaryEffect {
    data object NavigateBack : BestiaryEffect
}
