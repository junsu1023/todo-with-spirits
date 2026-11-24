package com.example.todowithspirits.feature.bestiary.model

import androidx.annotation.StringRes
import com.example.todowithspirits.R

enum class BestiarySortOption(@StringRes val labelRes: Int) {
    AFFECTION(R.string.sort_by_affection), // 호감도 높은 정령부터
    BESTIARY(R.string.sort_by_bestiary), // 서버가 내려준 도감 순서
    ACQUIRED(R.string.sort_by_acquired) // 최근에 만난 정령부터
}
