package com.example.todowithspirits.feature.bestiary.state

import androidx.compose.runtime.Immutable
import com.example.domain.model.SpiritCollectionItem
import com.example.todowithspirits.feature.bestiary.model.BestiarySortOption
import com.example.todowithspirits.feature.bestiary.model.BestiaryTab

@Immutable
data class BestiaryUiState(
    // 서버가 내려준 도감 순서
    val spirits: List<SpiritCollectionItem> = emptyList(),
    val selectedTab: BestiaryTab = BestiaryTab.ALL,
    val isUnownedOnly: Boolean = false,
    val sortOption: BestiarySortOption = BestiarySortOption.AFFECTION
) {
    val tabs: List<BestiaryTab> = BestiaryTab.entries
    val sortOptions: List<BestiarySortOption> = BestiarySortOption.entries

    val visibleSpirits: List<SpiritCollectionItem>
        get() {
            val filtered = if (isUnownedOnly) spirits.filterNot { it.isOwned } else spirits

            return when (sortOption) {
                BestiarySortOption.BESTIARY -> filtered
                // 획득 일자가 응답에 없어, 만난 순서대로 커지는 spiritId로 정렬한다. 미보유는 도감 순서로 뒤에 둔다.
                BestiarySortOption.ACQUIRED -> filtered.sortedWith(
                    compareByDescending<SpiritCollectionItem> { it.isOwned }
                        .thenByDescending { it.spiritId ?: 0L }
                )
                // 미보유는 호감도가 0이라 뒤로 간다. 같은 호감도는 도감 순서를 유지한다.
                BestiarySortOption.AFFECTION -> filtered.sortedByDescending { it.affectionLevel }
            }
        }
}
