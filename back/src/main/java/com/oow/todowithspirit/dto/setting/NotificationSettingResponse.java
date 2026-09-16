package com.oow.todowithspirit.dto.setting;

import com.oow.todowithspirit.domain.setting.NotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingResponse {

    private boolean reminderEnabled;          // 리마인드 알림
    private boolean postponedTaskEnabled;     // 미룬 플랜 정기 알림
    private boolean routineReminderEnabled;   // 루틴 안내 알림
    private boolean streakSaveEnabled;        // 스트릭(연속 달성) 세이브 알림
    private boolean nightPushEnabled;         // 야간 푸시 알림 수신

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .reminderEnabled(setting.isReminderEnabled())
                .postponedTaskEnabled(setting.isPostponedTaskEnabled())
                .routineReminderEnabled(setting.isRoutineReminderEnabled())
                .streakSaveEnabled(setting.isStreakSaveEnabled())
                .nightPushEnabled(setting.isNightPushEnabled())
                .build();
    }
}