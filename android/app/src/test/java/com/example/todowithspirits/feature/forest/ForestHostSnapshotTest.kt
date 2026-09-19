package com.example.todowithspirits.feature.forest

import com.example.domain.model.TaskSummary
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ForestHostSnapshotTest {
    private val today = LocalDate.of(2026, 9, 18)
    private fun task(id: Long = 1, title: String = "책 읽기", category: String = "GROWTH", type: String = "SCHEDULE", date: LocalDate = today, completed: Boolean = false) = TaskSummary(
        category = category, completedAt = null, createdAt = today.atStartOfDay(), endDate = today, endTime = null,
        growthType = null, growthValue = 0, isAllDay = true, isCompleted = completed, isImportant = false, isPublic = false,
        memo = null, notificationAt = null, notificationMinutes = null, occurrenceDate = date, taskId = id,
        taskType = type, title = title, updatedAt = today.atStartOfDay())

    @Test fun retainsOriginalTitleCompletionAndRoutineIdentity() {
        val rows = ForestHostSnapshot.items(today, listOf(task(title = "가".repeat(255)), task(type = "ROUTINE", category = "NONE", completed = true)))
        assertEquals(255, rows[0].title.length)
        assertFalse(rows[0].isCompleted)
        assertTrue(rows[1].isHabit)
        assertTrue(rows[1].isCompleted)
        assertEquals("NONE", rows[1].category)
        assertNotEquals(rows[0].id, rows[1].id)
    }
    @Test fun emptyIsAValidSuccessfulSnapshot() { assertTrue(ForestHostSnapshot.items(today, emptyList()).isEmpty()) }
    @Test fun acceptsFiftyAndRejectsOverflowWithoutDroppingRows() {
        assertEquals(50, ForestHostSnapshot.items(today, (1L..50L).map { task(it) }).size)
        assertThrows(IllegalArgumentException::class.java) { ForestHostSnapshot.items(today, (1L..51L).map { task(it) }) }
    }
    @Test fun rejectsDuplicateWrongDateAndUnsupportedContent() {
        listOf(listOf(task(), task()), listOf(task(date = today.minusDays(1))), listOf(task(category = "UNKNOWN")), listOf(task(type = "UNKNOWN")), listOf(task(title = "가".repeat(256))), listOf(task(title = "첫 줄\n다음 줄"))).forEach { rows ->
            assertThrows(IllegalArgumentException::class.java) { ForestHostSnapshot.items(today, rows) }
        }
    }
    @Test fun revisionAdvancesAcrossClockRollbackAndRefusesOverflow() {
        assertEquals(101, ForestHostSnapshot.nextRevision(100, 50))
        assertEquals(1000, ForestHostSnapshot.nextRevision(100, 1000))
        assertThrows(IllegalArgumentException::class.java) { ForestHostSnapshot.nextRevision(Long.MAX_VALUE, 50) }
    }
}
