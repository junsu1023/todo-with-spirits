package com.example.data.mapper

import com.example.data.response.LoginResponse
import com.example.data.response.TaskDetailResponse
import com.example.domain.model.LoginSession
import com.example.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
    memo = memo,
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

fun LoginResponse.toDomain(): LoginSession = LoginSession(
    userId = userId,
    email = email,
    nickname = nickname,
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType
)