package com.example.todowithspirits.feature.search.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import com.example.todowithspirits.feature.calendar.model.toPlanItemData
import com.example.todowithspirits.feature.search.state.SearchEffect
import com.example.todowithspirits.feature.search.state.SearchEvent
import com.example.todowithspirits.feature.search.state.SearchUiState
import com.example.todowithspirits.feature.search.model.RecentSearchStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

const val SEARCH_QUERY_ARG = "query"

// domain에 검색 API가 없어, 오늘 기준 앞뒤 1년 일정을 불러와 제목으로 거른다.
private const val SEARCH_RANGE_YEARS = 1L

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskCalendarUseCase: GetTaskCalendarUseCase,
    private val recentSearchStore: RecentSearchStore
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> get() = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect: Flow<SearchEffect> = _effect.receiveAsFlow()

    init {
        // 캘린더 화면에서 넘어온 검색어는 이미 최근 검색에 들어가 있다.
        val query = savedStateHandle.get<String>(SEARCH_QUERY_ARG).orEmpty()
        _uiState.update { it.copy(input = query) }
        search(query, addToRecent = false)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            SearchEvent.BackClicked -> viewModelScope.launch { _effect.send(SearchEffect.NavigateBack) }
            is SearchEvent.InputChanged -> _uiState.update { it.copy(input = event.input) }
            SearchEvent.Submitted -> search(_uiState.value.input, addToRecent = true)
        }
    }

    private fun search(input: String, addToRecent: Boolean) {
        val query = input.trim()
        if (query.isEmpty()) return
        if (addToRecent) recentSearchStore.add(query)

        _uiState.update { it.copy(query = query) }

        viewModelScope.launchWithLoading {
            val today = LocalDate.now()

            getTaskCalendarUseCase(today.minusYears(SEARCH_RANGE_YEARS), today.plusYears(SEARCH_RANGE_YEARS))
                .onSuccess { calendar ->
                    val results = calendar.items
                        .filter { it.title.contains(query, ignoreCase = true) }
                        .map { it.toPlanItemData() }
                        .sortedWith(
                            compareBy<PlanItemData> { it.endDate ?: LocalDate.MAX }
                                .thenBy { it.endTime ?: LocalTime.MAX }
                        )

                    _uiState.update { it.copy(results = results, hasSearched = true) }
                }
                .onFailure {
                    emitErrorMsg(it.localizedMessage ?: "일정을 검색하지 못했습니다")
                }
        }
    }
}
