package com.example.todowithspirits.feature.bestiary.state

import com.example.todowithspirits.feature.bestiary.model.BestiarySortOption

sealed interface BestiaryEvent {
    data object BackClicked : BestiaryEvent
    data object UnownedOnlyToggled : BestiaryEvent
    data class SortOptionSelected(val option: BestiarySortOption) : BestiaryEvent
}
