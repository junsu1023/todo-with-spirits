package com.example.todowithspirits.feature.today.viewmodel

import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.today.state.SpiritInfo
import com.example.todowithspirits.feature.today.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(): BaseViewModel() {
    private val _uiState =
        MutableStateFlow(TodayUiState(spiritInfo = SpiritInfo("루미", 99, 5555, 9999, 999)))
    val uiState: StateFlow<TodayUiState> get() = _uiState.asStateFlow()

}