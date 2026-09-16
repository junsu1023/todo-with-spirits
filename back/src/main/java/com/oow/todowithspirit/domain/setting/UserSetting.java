package com.oow.todowithspirit.domain.setting;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "dark_mode", nullable = false)
    private boolean darkMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 10)
    private Language language;

    @Column(name = "dday_display_enabled", nullable = false)
    private boolean ddayDisplayEnabled;

    @Column(name = "auto_backup_enabled", nullable = false)
    private boolean autoBackupEnabled;

    @Builder
    public UserSetting(Long userId, Boolean darkMode, Language language,
                        Boolean ddayDisplayEnabled, Boolean autoBackupEnabled) {
        this.userId = userId;
        this.darkMode = darkMode != null ? darkMode : false;
        this.language = language != null ? language : Language.SYSTEM;
        this.ddayDisplayEnabled = ddayDisplayEnabled != null ? ddayDisplayEnabled : true;
        this.autoBackupEnabled = autoBackupEnabled != null ? autoBackupEnabled : false;
    }

    public static UserSetting createDefault(Long userId) {
        return UserSetting.builder()
                .userId(userId)
                .build();
    }

    public void updateDisplay(Boolean darkMode, Language language, Boolean ddayDisplayEnabled) {
        if (darkMode != null) this.darkMode = darkMode;
        if (language != null) this.language = language;
        if (ddayDisplayEnabled != null) this.ddayDisplayEnabled = ddayDisplayEnabled;
    }
}