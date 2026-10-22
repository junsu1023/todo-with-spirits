package com.example.todowithspirits.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/*
캘린더 검색 패널과 일정 검색 화면이 함께 쓰는 최근 검색어.
data 모듈을 바꾸지 않기 위해 앱 메모리에만 두며, 앱을 다시 켜면 비워진다.
 */
@Singleton
class RecentSearchStore @Inject constructor() {
    private val _searches = MutableStateFlow<List<String>>(emptyList())
    val searches: StateFlow<List<String>> = _searches.asStateFlow()

    // 맨 앞에 추가하고, 같은 검색어는 한 번만, 최대 MAX_RECENT_SEARCHES개까지 남긴다.
    fun add(query: String) {
        _searches.update { (listOf(query) + it.filterNot { recent -> recent == query }).take(MAX_RECENT_SEARCHES) }
    }

    fun remove(query: String) {
        _searches.update { it - query }
    }

    fun clear() {
        _searches.update { emptyList() }
    }
}

private const val MAX_RECENT_SEARCHES = 10
