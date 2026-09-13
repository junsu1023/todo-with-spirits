package com.oow.todowithspirit.domain.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.userId = :userId
            ORDER BY n.createdAt DESC, n.id DESC
            """)
    List<Notification> findFirstPage(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.userId = :userId
              AND (n.createdAt < :cursorCreatedAt
                   OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
            ORDER BY n.createdAt DESC, n.id DESC
            """)
    List<Notification> findNextPage(
            @Param("userId") Long userId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}