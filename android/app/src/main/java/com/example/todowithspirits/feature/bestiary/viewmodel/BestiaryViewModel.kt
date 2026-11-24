package com.example.todowithspirits.feature.bestiary.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetSpiritCollectionUseCase
import com.example.todowithspirits.feature.bestiary.state.BestiaryEffect
import com.example.todowithspirits.feature.bestiary.state.BestiaryEvent
import com.example.todowithspirits.feature.bestiary.state.BestiaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BestiaryViewModel @Inject constructor(
    private val getSpiritCollectionUseCase: GetSpiritCollectionUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(BestiaryUiState())
    val uiState: StateFlow<BestiaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<BestiaryEffect>(Channel.BUFFERED)
    val effect: Flow<BestiaryEffect> = _effect.receiveAsFlow()

    init {
        loadSpirits()
    }

    fun onEvent(event: BestiaryEvent) {
        when (event) {
            BestiaryEvent.BackClicked -> sendEffect(BestiaryEffect.NavigateBack)
            is BestiaryEvent.TabSelected -> _uiState.update { it.copy(selectedTab = event.tab) }
            BestiaryEvent.UnownedOnlyToggled -> _uiState.update { it.copy(isUnownedOnly = !it.isUnownedOnly) }
            is BestiaryEvent.SortOptionSelected -> _uiState.update { it.copy(sortOption = event.option) }
        }
    }

    private fun loadSpirits() {
        viewModelScope.launchWithLoading {
            getSpiritCollectionUseCase()
                .onSuccess { spirits -> _uiState.update { it.copy(spirits = spirits) } }
                .onFailure { emitErrorMsg(it.localizedMessage ?: "정령도감을 불러오지 못했습니다") }
        }
    }

    private fun sendEffect(effect: BestiaryEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
