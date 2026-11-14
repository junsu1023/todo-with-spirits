package com.example.data.mapper

import com.example.data.request.CreateTodoRequest
import com.example.data.request.RoutineRequest
import com.example.data.response.CurrencyResponse
import com.example.data.response.CurrencyTransactionResponse
import com.example.data.response.DecorationPurchaseResponse
import com.example.data.response.DecorationResponse
import com.example.data.response.DisplaySettingResponse
import com.example.data.response.EmailAvailabilityResponse
import com.example.data.response.LoginResponse
import com.example.data.response.MeetSpiritResponse
import com.example.data.response.NotificationItemResponse
import com.example.data.response.NotificationPageResponse
import com.example.data.response.NotificationSettingResponse
import com.example.data.response.SignUpResponse
import com.example.data.response.SocialLoginResponse
import com.example.data.response.SpiritResponse
import com.example.data.response.SubscriptionResponse
import com.example.data.response.TaskCalendarResponse
import com.example.data.response.TaskCompletionResponse
import com.example.data.response.TaskDetailResponse
import com.example.data.response.TaskListItemResponse
import com.example.data.response.TaskListResponse
import com.example.data.response.UserProfileResponse
import com.example.domain.model.AlarmOption
import com.example.domain.model.AppLanguage
import com.example.domain.model.CurrencyHistory
import com.example.domain.model.CurrencyReason
import com.example.domain.model.CurrencyTransaction
import com.example.domain.model.Decoration
import com.example.domain.model.DecorationPurchase
import com.example.domain.model.DisplaySetting
import com.example.domain.model.EmailAvailability
import com.example.domain.model.LoginSession
import com.example.domain.model.LoginType
import com.example.domain.model.MetSpirit
import com.example.domain.model.NewRoutine
import com.example.domain.model.NewTodo
import com.example.domain.model.NotificationCategory
import com.example.domain.model.NotificationItem
import com.example.domain.model.NotificationPage
import com.example.domain.model.NotificationSetting
import com.example.domain.model.PlanType
import com.example.domain.model.SignUpResult
import com.example.domain.model.SocialLoginSession
import com.example.domain.model.SocialProvider
import com.example.domain.model.Spirit
import com.example.domain.model.Subscription
import com.example.domain.model.Task
import com.example.domain.model.TaskCalendar
import com.example.domain.model.TaskCompletionReward
import com.example.domain.model.TaskList
import com.example.domain.model.TaskSummary
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

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
    completedAt = completedAt?.let { LocalDateTime.parse(it) },
    createdAt = createdAt?.let { LocalDateTime.parse(it) },
    endDate = endDate?.let { LocalDate.parse(it) },
    endTime = endTime?.let { LocalTime.parse(it) },
    excludeHoliday = excludeHoliday ?: false,
    growthType = growthType,
    growthValue = growthValue ?: 0,
    isAllDay = isAllDay ?: false,
    isCompleted = isCompleted,
    isImportant = isImportant ?: false,
    notificationAt = notificationAt?.let { LocalDateTime.parse(it) },
    notificationMinutes = notificationMinutes,
    occurrenceDate = occurrenceDate?.let { LocalDate.parse(it) },
    repeatDaysOfMonth = repeatDaysOfMonth.orEmpty(),
    repeatDaysOfWeek = repeatDaysOfWeek.orEmpty().map { DayOfWeek.valueOf(it) },
    repeatEndDate = repeatEndDate?.let { LocalDate.parse(it) },
    repeatType = repeatType,
    taskId = taskId,
    taskType = taskType,
    title = title,
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun TaskListResponse.toDomain(): TaskList = TaskList(
    totalCount = totalCount,
    items = items.map { it.toDomain() }
)

fun TaskCompletionResponse.toDomain(): TaskCompletionReward = TaskCompletionReward(
    currencyEarned = currencyEarned,
    balance = balance
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
    completedAt = completedAt?.let { LocalDateTime.parse(it) },
    createdAt = LocalDateTime.parse(createdAt),
    endDate = endDate?.let { LocalDate.parse(it) },
    endTime = endTime?.let { LocalTime.parse(it) },
    excludeHoliday = excludeHoliday ?: false,
    growthType = growthType,
    growthValue = growthValue,
    isAllDay = isAllDay ?: false,
    isCompleted = isCompleted,
    isImportant = isImportant,
    notificationAt = notificationAt?.let { LocalDateTime.parse(it) },
    notificationMinutes = notificationMinutes,
    occurrenceDate = LocalDate.parse(occurrenceDate),
    repeatDaysOfMonth = repeatDaysOfMonth.orEmpty(),
    repeatDaysOfWeek = repeatDaysOfWeek.orEmpty().map { DayOfWeek.valueOf(it) },
    repeatEndDate = repeatEndDate?.let { LocalDate.parse(it) },
    repeatType = repeatType,
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
    notificationType = notificationType.toApiValue()
)

fun NewRoutine.toRequest(): RoutineRequest = RoutineRequest(
    title = title,
    repeatType = repeatType.name,
    repeatEndDate = repeatEndDate?.toString(),
    repeatDaysOfWeek = repeatDaysOfWeek.map { it.name },
    repeatDaysOfMonth = repeatDaysOfMonth,
    notificationType = notificationType.toApiValue(),
    excludeHoliday = excludeHoliday
)

fun NotificationPageResponse.toDomain(): NotificationPage = NotificationPage(
    items = content.map { it.toDomain() },
    nextCursor = nextCursor,
    hasNext = hasNext
)

fun NotificationItemResponse.toDomain(): NotificationItem = NotificationItem(
    id = id,
    category = NotificationCategory.fromApiValue(category),
    content = content,
    createdAt = LocalDateTime.parse(createdAt),
    read = read
)

fun DisplaySettingResponse.toDomain(): DisplaySetting = DisplaySetting(
    darkMode = darkMode,
    ddayDisplayEnabled = ddayDisplayEnabled,
    language = AppLanguage.fromApiValue(language)
)

fun EmailAvailabilityResponse.toDomain(): EmailAvailability = EmailAvailability(
    provider = provider?.let { value -> SocialProvider.entries.find { it.name == value } },
    registered = registered
)

fun UserProfileResponse.toDomain(): UserProfile = UserProfile(
    userId = userId,
    email = email,
    emailVerificationStatus = emailVerificationStatus,
    loginType = LoginType.entries.find { it.name == loginType } ?: LoginType.LOCAL,
    nickname = nickname,
    premium = premium,
    provider = provider?.let { value -> SocialProvider.entries.find { it.name == value } },
    representativeSpiritId = representativeSpiritId,
    role = UserRole.entries.find { it.name == role } ?: UserRole.USER,
    createdAt = LocalDateTime.parse(createdAt)
)

fun SubscriptionResponse.toDomain(): Subscription = Subscription(
    planType = PlanType.fromApiValue(planType),
    startedAt = startedAt?.let { OffsetDateTime.parse(it) },
    expiresAt = expiresAt?.let { OffsetDateTime.parse(it) },
    autoRenew = autoRenew,
    premium = premium
)

fun NotificationSettingResponse.toDomain(): NotificationSetting = NotificationSetting(
    reminderEnabled = reminderEnabled,
    postponedTaskEnabled = postponedTaskEnabled,
    routineReminderEnabled = routineReminderEnabled,
    streakSaveEnabled = streakSaveEnabled,
    nightPushEnabled = nightPushEnabled
)

// 대표 정령 조회 응답에는 representative가 없으므로 호출부에서 기본값을 정한다.
fun SpiritResponse.toDomain(defaultRepresentative: Boolean = false): Spirit = Spirit(
    id = id,
    spiritName = spiritName,
    stage = stage,
    exp = exp,
    focusExp = focusExp ?: 0,
    energyExp = energyExp ?: vitalityExp ?: 0,
    consistencyExp = consistencyExp ?: 0,
    creativityExp = creativityExp ?: 0,
    imageUrl = imageUrl.orEmpty(),
    representative = representative ?: defaultRepresentative
)

fun MeetSpiritResponse.toDomain(): MetSpirit = MetSpirit(
    id = id,
    name = name,
    variant = variant,
    balance = balance
)

fun DecorationResponse.toDomain(): Decoration = Decoration(
    id = id,
    code = code,
    name = name,
    price = price,
    imageKey = image?.key,
    imageUrl = image?.url,
    purchased = purchased,
    installed = installed
)

fun DecorationPurchaseResponse.toDomain(): DecorationPurchase = DecorationPurchase(
    decorationId = decorationId,
    balance = balance
)

fun CurrencyResponse.toDomain(): CurrencyHistory = CurrencyHistory(
    balance = balance,
    transactions = transactions.map { it.toDomain() }
)

fun CurrencyTransactionResponse.toDomain(): CurrencyTransaction = CurrencyTransaction(
    id = id,
    amount = amount,
    reason = CurrencyReason.fromApiValue(reason),
    createdAt = OffsetDateTime.parse(createdAt)
)

private fun AlarmOption.toApiValue(): String = this.toString()
