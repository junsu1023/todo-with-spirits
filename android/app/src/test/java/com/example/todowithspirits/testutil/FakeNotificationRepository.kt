package com.example.todowithspirits.testutil

import com.example.domain.model.NotificationPage
import com.example.domain.repository.NotificationRepository

// 알림 화면 테스트용 NotificationRepository 더블.
class FakeNotificationRepository(
    private val page: NotificationPage = NotificationPage(emptyList(), null, false)
) : NotificationRepository {
    var markAllReadCount = 0
        private set

    override suspend fun getNotifications(size: Int?, cursor: String?): Result<NotificationPage> = Result.success(page)

    override suspend fun markNotificationRead(alarmId: Long): Result<Unit> = error("이 테스트에서는 쓰지 않는다")

    override suspend fun markAllNotificationsRead(): Result<Unit> {
        markAllReadCount++
        return Result.success(Unit)
    }
}
