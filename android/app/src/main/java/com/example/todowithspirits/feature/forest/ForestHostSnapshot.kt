package com.example.todowithspirits.feature.forest

import com.example.domain.model.TaskSummary
import java.time.LocalDate

internal data class ForestHostItem(
    val id: String,
    val title: String,
    val category: String,
    val isHabit: Boolean,
    val isCompleted: Boolean
)

/** Validation is atomic: never truncate a title or silently drop an occurrence. */
internal object ForestHostSnapshot {
    // 서버가 일정 분야(category)를 더 이상 내려주지 않아, 숲에는 '미정'으로 전달한다.
    private const val CATEGORY_NONE = "NONE"
    private const val REPEAT_NONE = "NONE"

    fun items(date: LocalDate, source: List<TaskSummary>): List<ForestHostItem> {
        require(source.size <= 50) { "오늘 일정이 50개를 넘어 숲에 연결하지 못했어요. 일정은 그대로 보관되어 있어요." }
        val ids = hashSetOf<String>()
        return source.map { row ->
            require(row.occurrenceDate == date) { "오늘 날짜와 다른 일정이 포함되어 있어요." }
            require(row.title.isNotBlank() && row.title.length <= 255 && row.title.none(Char::isISOControl)) { "일정 제목을 그대로 연결할 수 없어요. 제목을 확인해 주세요." }
            // 서버가 일정/루틴 구분(taskType)을 없앴다. 숲과 맞춘 id·루틴 여부 규약은 그대로 두고 반복 여부로 채운다.
            val isRepeating = row.repeatType != null && row.repeatType != REPEAT_NONE
            val id = "${if (isRepeating) "routine" else "schedule"}:${row.taskId}:$date"
            require(ids.add(id)) { "같은 일정이 중복해서 전달되었어요." }
            ForestHostItem(id, row.title, CATEGORY_NONE, isRepeating, row.isCompleted)
        }
    }

    fun nextRevision(previous: Long, clockMillis: Long): Long {
        require(previous < Long.MAX_VALUE) { "동기화 순서 번호를 갱신할 수 없어요." }
        return maxOf(previous + 1, clockMillis.coerceAtLeast(0))
    }
}
