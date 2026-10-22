package com.example.todowithspirits.feature.calendar.model

import androidx.annotation.StringRes
import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.R

// domain의 displayName("마감 임박 순" 등)은 바꿀 수 없어, 캘린더 화면 표시 이름은 여기서 매핑한다.
@get:StringRes
val PlanSortOption.labelRes: Int
    get() = when (this) {
        PlanSortOption.DEADLINE -> R.string.sort_by_time
        PlanSortOption.IMPORTANT -> R.string.sort_by_important
        PlanSortOption.COMPLETE -> R.string.sort_by_complete
    }
