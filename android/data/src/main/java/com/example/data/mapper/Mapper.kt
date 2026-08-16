package com.example.data.mapper

import com.example.data.request.CreateRoutineRequest
import com.example.data.request.CreateTodoRequest
import com.example.data.request.UpdateRoutineRequest
import com.example.data.response.LoginResponse
import com.example.data.response.RoutineDetailResponse
import com.example.data.response.SignUpResponse
import com.example.data.response.SocialLoginResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskDetailResponse
import com.example.data.response.TaskListItemResponse
import com.example.domain.model.AlarmOption
import com.example.domain.model.CategoryOption
import com.example.domain.model.LoginSession
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.Routine
import com.example.domain.model.SignUpResult
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskSummary
import java.time.DayOfWeek
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

fun SignUpResponse.toDomain(): SignUpResult = SignUpResult(
    email = email,
    nickname = nickname,
    userId = userId
)

fun SocialLoginResponse.toDomain(): SocialLoginSession = SocialLoginSession(
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
    repeatDaysOfMonth = repeatDaysOfMonth ?: emptyList(),
    repeatDaysOfWeek = repeatDaysOfWeek.orEmpty().map { DayOfWeek.valueOf(it) },
    repeatEndDate = repeatEndDate?.let { LocalDate.parse(it) },
    repeatType = repeatType,
    startDate = startDate?.let { LocalDate.parse(it) },
    startTime = startTime?.let { LocalTime.parse(it) },
    taskId = taskId,
    taskType = taskType,
    title = title,
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun TaskCalendarResponse.toDomain(): TaskCalendar = TaskCalendar(
    completedCount = completedCount,
    completedRoutineCount = completedRoutineCount,
    completedScheduleCount = completedScheduleCount,
    inCompleteCount = inCompleteCount,
    routineCount = routineCount,
    scheduleCount = scheduleCount,
    totalCount = totalCount,
    items = items.map { it.toDomain() }
)

fun TaskListItemResponse.toDomain(): TaskSummary = TaskSummary(
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
    memo = memo,
    notificationAt = notificationAt?.let { LocalDateTime.parse(it) },
    notificationMinutes = notificationMinutes,
    occurrenceDate = LocalDate.parse(occurrenceDate),
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
    category = category.toApiValue(),
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

fun NewRoutine.toUpdateRequest(): UpdateRoutineRequest = UpdateRoutineRequest(
    title = title,
    repeatType = repeatType.name,
    category = category.toApiValue(),
    repeatEndDate = repeatEndDate?.toString(),
    repeatDaysOfWeek = repeatDaysOfWeek.map { it.name }.ifEmpty { null },
    repeatDaysOfMonth = repeatDaysOfMonth.ifEmpty { null },
    notificationType = notification.toApiValue(),
    isPublic = isPublic,
    excludeHoliday = excludeHoliday,
    memo = memo
)

fun RoutineDetailResponse.toDomain(): Routine = Routine(
    category = category,
    completedAt = completedAt?.let { LocalDateTime.parse(it) },
    createdAt = LocalDateTime.parse(createdAt),
    excludeHoliday = excludeHoliday,
    growthType = growthType,
    growthValue = growthValue,
    isCompleted = isCompleted,
    isPublic = isPublic,
    memo = memo ?: "",
    notificationAt = notificationAt?.let { LocalDateTime.parse(it) },
    notificationMinutes = notificationMinutes,
    repeatDaysOfMonth = repeatDaysOfMonth,
    repeatDaysOfWeek = repeatDaysOfWeek.map { DayOfWeek.valueOf(it) },
    repeatEndDate = repeatEndDate?.let { LocalDate.parse(it) },
    repeatType = repeatType,
    taskId = taskId,
    taskType = taskType,
    title = title,
    updatedAt = LocalDateTime.parse(updatedAt)
)

private fun AlarmOption.toApiValue(): String = this.toString()

private fun CategoryOption.toApiValue(): String = this.toString()