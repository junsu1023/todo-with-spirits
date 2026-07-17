package com.example.data.mapper

import com.example.data.request.CreateRoutineRequest
import com.example.data.request.CreateTodoRequest
import com.example.data.response.LoginResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import com.example.data.response.TaskListItemResponse
import com.example.domain.model.AlarmOption
import com.example.domain.model.LoginSession
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskSummary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun LoginResponse.toDomain(): LoginSession = LoginSession(
    userId = userId,
    email = email,
    nickname = nickname,
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType
)

fun TaskDetailResponse.toDomain(): Task = Task(
    category = category,
    completedAt = completedAt?.let { LocalDateTime.parse(it) },
    createdAt = LocalDateTime.parse(createdAt),
    endDate = endDate?.let { LocalDate.parse(it) },
    endTime = endTime?.let { LocalTime.parse(it) },
    growthType = growthType,
    growthValue = growthValue,
    isAllDay = isAllDay,
    isCompleted = isCompleted,
    isImportant = isImportant,
    isPublic = isPublic,
    memo = memo ?: "",
    notificationMinutes = notificationMinutes,
    repeatDaysOfWeek = repeatDaysOfWeek,
    repeatDaysOfMonth = repeatDaysOfMonth,
    repeatEndDate = repeatEndDate?.let { LocalDate.parse(it) },
    startDate = LocalDate.parse(startDate),
    startTime = startTime?.let { LocalTime.parse(it) },
    repeatType = repeatType,
    taskId = taskId,
    taskType = taskType,
    title = title,
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun TaskCalendarResponse.toDomain(): TaskCalendar = TaskCalendar(
    completedCount = completedCount,
    completedRoutineCount = completedRoutineCount,
    completedScheduleCount = completedScheduleCount,
    incompleteCount = incompleteCount,
    routineCount = routineCount,
    scheduleCount = scheduleCount,
    totalCount = totalCount,
    items = items.map { it.toDomain() }
)

fun TaskListItemResponse.toDomain(): TaskSummary = TaskSummary(
    category = category,
    endDate = endDate?.let { LocalDate.parse(it) },
    endTime = endTime?.let { LocalTime.parse(it) },
    isAllDay = isAllDay,
    isCompleted = isCompleted,
    isImportant = isImportant,
    isPublic = isPublic,
    memo = memo,
    notificationMinutes = notificationMinutes,
    repeatDaysOfWeek = repeatDaysOfWeek.orEmpty(),
    repeatDaysOfMonth = repeatDaysOfMonth.orEmpty(),
    repeatType = repeatType,
    repeatEndDate = repeatEndDate?.let { LocalDate.parse(it) },
    startDate = LocalDate.parse(startDate),
    startTime = startTime?.let { LocalTime.parse(it) },
    taskId = taskId,
    taskType = taskType,
    title = title,
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun NewTodo.toRequest(): CreateTodoRequest = CreateTodoRequest(
    title = title,
    isAllDay = isAllDay,
    endDateTime = endDateTime,
    isImportant = isImportant,
    notificationType = notificationType.toApiValue(),
    category = category,
    isPublic = isPublic,
    memo = memo
)

fun NewRoutine.toRequest(): CreateRoutineRequest = CreateRoutineRequest(
    title = title,
    repeatType = repeatType.name,
    repeatEndDate = repeatEndDate?.toString(),
    repeatDaysOfWeek = repeatDaysOfWeek.map { it.name }.ifEmpty { null },
    repeatDaysOfMonth = repeatDaysOfMonth.ifEmpty { null },
    notification = notification.toApiValue(),
    isPublic = isPublic,
    memo = memo
)

private fun AlarmOption.toApiValue(): String = when (this) {
    AlarmOption.NONE -> "NONE"
    AlarmOption.TEN_MIN_BEFORE -> "TEN_MINUTES"
    AlarmOption.THIRTY_MIN_BEFORE -> "THIRTY_MINUTES"
    AlarmOption.ONE_HOUR_BEFORE -> "ONE_HOUR"
}
