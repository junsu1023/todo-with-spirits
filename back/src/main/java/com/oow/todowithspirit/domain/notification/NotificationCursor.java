package com.oow.todowithspirit.domain.notification;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public record NotificationCursor(LocalDateTime createdAt, Long id) {

    private static final String DELIMITER = "_";

    public static NotificationCursor of(Notification notification) {
        return new NotificationCursor(notification.getCreatedAt(), notification.getId());
    }

    public static NotificationCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int delimiterIndex = raw.lastIndexOf(DELIMITER);
            LocalDateTime createdAt = LocalDateTime.parse(raw.substring(0, delimiterIndex));
            Long id = Long.valueOf(raw.substring(delimiterIndex + 1));
            return new NotificationCursor(createdAt, id);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "cursor", "Invalid cursor");
        }
    }

    public String encode() {
        String raw = createdAt.toString() + DELIMITER + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}