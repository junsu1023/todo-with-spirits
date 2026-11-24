package com.example.todowithspirits.feature.bestiary.state

import com.example.todowithspirits.feature.bestiary.model.BestiarySortOption
import com.example.todowithspirits.feature.bestiary.model.BestiaryTab

sealed interface BestiaryEvent {
    data object BackClicked : BestiaryEvent
    data class TabSelected(val tab: BestiaryTab) : BestiaryEvent
    data object UnownedOnlyToggled : BestiaryEvent
    data class SortOptionSelected(val option: BestiarySortOption) : BestiaryEvent
}
