package com.example.todowithspirits.feature.search.state

import androidx.compose.runtime.Immutable
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import java.time.LocalDate

@Immutable
data class SearchUiState(
    val input: String = "",
    val query: String = "",
    // 날짜·시간 오름차순으로 정렬된 검색 결과
    val results: List<PlanItemData> = emptyList(),
    val hasSearched: Boolean = false
) {
    // 같은 날짜의 일정끼리 묶는다. (화면 왼쪽 날짜 열 하나에 카드 여러 개)
    val dateGroups: List<SearchDateGroup>
        get() = results
            .filter { it.endDate != null }
            .groupBy { it.endDate!! }
            .map { (date, items) -> SearchDateGroup(date, items) }
}

@Immutable
data class SearchDateGroup(
    val date: LocalDate,
    val items: List<PlanItemData>
)
