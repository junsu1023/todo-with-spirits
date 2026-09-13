package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.notification.NotificationRepository;
import com.oow.todowithspirit.domain.notification.NotificationCategory;
import com.oow.todowithspirit.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, int months) {
        if (months < 1) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "months", "months must be at least 1");
        }

        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        List<NotificationResponse> notifications = notificationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since).stream()
                .map(NotificationResponse::from)
                .toList();

        // TODO: 실제 알림 발행 로직(스케줄러/이벤트) 붙기 전까지 카테고리별 더미 데이터로 대체
        if (notifications.isEmpty()) {
            return buildDummyNotifications();
        }

        return notifications;
    }

    private List<NotificationResponse> buildDummyNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<NotificationResponse> dummies = List.of(
                NotificationResponse.builder()
                        .id(1L)
                        .category(NotificationCategory.SYSTEM.name())
                        .content("오늘 마감인 할 일이 3개 남아있어요.\n차근차근 끝내볼까요? 💪")
                        .isRead(false)
                        .createdAt(now.minusHours(1))
                        .build(),
                NotificationResponse.builder()
                        .id(2L)
                        .category(NotificationCategory.EVENT.name())
                        .content("루틴 5연속 성공 시 경험치가 두배")
                        .isRead(false)
                        .createdAt(now.minusHours(1))
                        .build(),
                NotificationResponse.builder()
                        .id(3L)
                        .category(NotificationCategory.SPIRIT.name())
                        .content("루미가 반딧불정령으로 진화했어요~\n지금 바로 만나러 가볼까요?")
                        .isRead(true)
                        .createdAt(now.minusDays(2))
                        .build(),
                NotificationResponse.builder()
                        .id(4L)
                        .category(NotificationCategory.SYSTEM.name())
                        .content("오늘 마감인 할 일이 1개 남아있어요.\n차근차근 끝내볼까요? 💪")
                        .isRead(true)
                        .createdAt(now.minusDays(1))
                        .build(),
                NotificationResponse.builder()
                        .id(5L)
                        .category(NotificationCategory.EVENT.name())
                        .content("루틴 5연속 성공 시 경험치가 두배")
                        .isRead(true)
                        .createdAt(now.minusDays(7))
                        .build()
        );

        return dummies.stream()
                .sorted(Comparator.comparing(NotificationResponse::getCreatedAt).reversed())
                .toList();
    }
}