package com.example.todowithspirits.feature.bestiary.model

import androidx.annotation.StringRes
import com.example.todowithspirits.R

enum class BestiaryTab(@StringRes val labelRes: Int) {
    ALL(R.string.bestiary_tab_all),
    // TODO: 도감 데이터에 카테고리가 생기면 카테고리별 목록을 연결한다. 지금은 탭 선택만 바뀐다.
    CATEGORY(R.string.bestiary_tab_category)
}
