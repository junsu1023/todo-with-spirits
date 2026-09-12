package com.oow.todowithspirit.domain.setting;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "reminder_enabled", nullable = false)
    private boolean reminderEnabled;

    @Column(name = "postponed_task_enabled", nullable = false)
    private boolean postponedTaskEnabled;

    @Column(name = "routine_reminder_enabled", nullable = false)
    private boolean routineReminderEnabled;

    @Column(name = "streak_save_enabled", nullable = false)
    private boolean streakSaveEnabled;

    @Column(name = "night_push_enabled", nullable = false)
    private boolean nightPushEnabled;

    @Builder
    public NotificationSetting(Long userId, Boolean reminderEnabled, Boolean postponedTaskEnabled,
                               Boolean routineReminderEnabled, Boolean streakSaveEnabled,
                               Boolean nightPushEnabled, LocalTime routineRderTime) {
        this.userId = userId;
        this.reminderEnabled = reminderEnabled != null ? reminderEnabled : true;
        this.postponedTaskEnabled = postponedTaskEnabled != null ? postponedTaskEnabled : false;
        this.routineReminderEnabled = routineReminderEnabled != null ? routineReminderEnabled : true;
        this.streakSaveEnabled = streakSaveEnabled != null ? streakSaveEnabled : true;
        this.nightPushEnabled = nightPushEnabled != null ? nightPushEnabled : false;
    }

    public static NotificationSetting createDefault(Long userId) {
        return NotificationSetting.builder()
                .userId(userId)
                .reminderEnabled(true)
                .postponedTaskEnabled(false)
                .routineReminderEnabled(true)
                .streakSaveEnabled(true)
                .nightPushEnabled(false)
                .build();
    }

    public void update(Boolean reminderEnabled, Boolean postponedTaskEnabled,
                       Boolean routineReminderEnabled, Boolean streakSaveEnabled,
                       Boolean nightPushEnabled) {
        if (reminderEnabled != null) this.reminderEnabled = reminderEnabled;
        if (postponedTaskEnabled != null) this.postponedTaskEnabled = postponedTaskEnabled;
        if (routineReminderEnabled != null) this.routineReminderEnabled = routineReminderEnabled;
        if (streakSaveEnabled != null) this.streakSaveEnabled = streakSaveEnabled;
        if (nightPushEnabled != null) this.nightPushEnabled = nightPushEnabled;
    }
}