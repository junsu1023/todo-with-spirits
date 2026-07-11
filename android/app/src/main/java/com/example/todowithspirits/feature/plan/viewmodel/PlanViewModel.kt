package com.example.todowithspirits.feature.plan.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.plan.state.PlanUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(): BaseViewModel() {
    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> get() = _uiState.asStateFlow()

    fun setSelectedDate(date: LocalDate) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(selectedDate = date)
            }
        }
    }

    fun setSelectedTab(tab: String) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(selectedTab = tab)
            }
        }
    }

    fun setHiddenState(hidden: Boolean) {
        viewModelScope.launchWithLoading {
            _uiState.update {
                it.copy(isHidden = hidden)
            }
        }
    }
}