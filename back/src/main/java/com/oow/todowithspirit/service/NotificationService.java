package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.notification.Notification;
import com.oow.todowithspirit.domain.notification.NotificationCategory;
import com.oow.todowithspirit.domain.notification.NotificationCursor;
import com.oow.todowithspirit.domain.notification.NotificationRepository;
import com.oow.todowithspirit.dto.notification.NotificationPageResponse;
import com.oow.todowithspirit.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public NotificationPageResponse getNotifications(Long userId, int size, String cursor) {
        if (size < 1) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "size", "size must be at least 1");
        }

        PageRequest pageRequest = PageRequest.of(0, size + 1);
        List<Notification> rows;
        if (cursor == null) {
            rows = notificationRepository.findFirstPage(userId, pageRequest);
        } else {
            NotificationCursor decoded = NotificationCursor.decode(cursor);
            rows = notificationRepository.findNextPage(userId, decoded.createdAt(), decoded.id(), pageRequest);
        }

        // ===== DUMMY_DATA_START: 실제 알림 발행 로직(스케줄러/이벤트) 붙으면 이 if 블록과
        //       buildDummyNotifications() 메서드를 통째로 삭제할 것 =====
        if (rows.isEmpty() && cursor == null) {
            List<NotificationResponse> dummyPage = buildDummyNotifications().stream()
                    .limit(size)
                    .toList();

            return NotificationPageResponse.builder()
                    .content(dummyPage)
                    .nextCursor(null)
                    .hasNext(false)
                    .build();
        }
        // ===== DUMMY_DATA_END =====

        boolean hasNext = rows.size() > size;
        List<Notification> page = hasNext ? rows.subList(0, size) : rows;

        String nextCursor = hasNext
                ? NotificationCursor.of(page.get(page.size() - 1)).encode()
                : null;

        return NotificationPageResponse.builder()
                .content(page.stream().map(NotificationResponse::from).toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this notification");
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    // ===== DUMMY_DATA_START: getNotifications()의 위 블록과 함께 삭제할 것 =====
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
    // ===== DUMMY_DATA_END =====
}