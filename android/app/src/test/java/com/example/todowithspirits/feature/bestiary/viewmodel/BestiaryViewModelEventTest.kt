package com.example.todowithspirits.feature.bestiary.viewmodel

import com.example.domain.model.SpiritCollectionItem
import com.example.domain.usecase.GetSpiritCollectionUseCase
import com.example.todowithspirits.feature.bestiary.model.BestiarySortOption
import com.example.todowithspirits.feature.bestiary.model.BestiaryTab
import com.example.todowithspirits.feature.bestiary.state.BestiaryEvent
import com.example.todowithspirits.testutil.FakeSpiritRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// 정령도감 화면의 Event → UiState 흐름을 검증한다.
@OptIn(ExperimentalCoroutinesApi::class)
class BestiaryViewModelEventTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun spirit(speciesId: Long, affectionLevel: Int, isOwned: Boolean = true) = SpiritCollectionItem(
        speciesId = speciesId,
        speciesName = "종류$speciesId",
        imageUrl = null,
        isOwned = isOwned,
        spiritId = if (isOwned) speciesId else null,
        spiritName = null,
        affectionLevel = affectionLevel,
        isRepresentative = false
    )

    private val collection = listOf(
        spirit(1, affectionLevel = 2),
        spirit(2, affectionLevel = 0, isOwned = false),
        spirit(3, affectionLevel = 5),
        spirit(4, affectionLevel = 2)
    )

    private fun buildViewModel() = BestiaryViewModel(GetSpiritCollectionUseCase(FakeSpiritRepository(collection)))

    @Test fun affectionSortPutsHigherAffectionFirstAndKeepsBestiaryOrderForTies() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(BestiaryEvent.SortOptionSelected(BestiarySortOption.BESTIARY))
        viewModel.onEvent(BestiaryEvent.SortOptionSelected(BestiarySortOption.AFFECTION))

        assertEquals(listOf(3L, 1L, 4L, 2L), viewModel.uiState.value.visibleSpirits.map { it.speciesId })
    }

    @Test fun tabSelectedChangesSelectedTabOnly() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val before = viewModel.uiState.value.visibleSpirits

        viewModel.onEvent(BestiaryEvent.TabSelected(BestiaryTab.CATEGORY))

        assertEquals(BestiaryTab.CATEGORY, viewModel.uiState.value.selectedTab)
        assertEquals(before, viewModel.uiState.value.visibleSpirits)
    }

    @Test fun defaultSortIsAffection() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(BestiarySortOption.AFFECTION, viewModel.uiState.value.sortOption)
        assertEquals(listOf(3L, 1L, 4L, 2L), viewModel.uiState.value.visibleSpirits.map { it.speciesId })
    }
}
