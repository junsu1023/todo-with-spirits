package com.example.todowithspirits.feature.search.state

sealed interface SearchEvent {
    data object BackClicked : SearchEvent
    data class InputChanged(val input: String) : SearchEvent
    data object Submitted : SearchEvent
}
