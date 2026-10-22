package com.example.todowithspirits.feature.search.state

sealed interface SearchEffect {
    data object NavigateBack : SearchEffect
}
