package com.oow.todowithspirit.dto.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingUpdateRequest {

    private Boolean reminderEnabled; // 리마인드 알림
    private Boolean postponedTaskEnabled; // 미룬 플랜 정기 알림
    private Boolean routineReminderEnabled; // 루틴 안내 알림
    private Boolean streakSaveEnabled; // 스트릭(연속 달성) 세이브 알림
    private Boolean nightPushEnabled; // 야간 푸시 알림 수신
}