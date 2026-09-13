package com.oow.todowithspirit.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NotificationPageResponse {

    private List<NotificationResponse> content;
    private String nextCursor; // 더 없으면 null
    private boolean hasNext;
}