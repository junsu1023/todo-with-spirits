package com.example.todowithspirits.feature.add.state

sealed interface AddEffect {
    data object NavigateBack : AddEffect
}
