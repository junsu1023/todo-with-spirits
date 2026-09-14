package com.oow.todowithspirit.dto.setting;

import com.oow.todowithspirit.domain.setting.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisplaySettingResponse {

    private boolean darkMode;           // 다크모드
    private boolean ddayDisplayEnabled; // 플랜 디데이 표시
    private String language;            // 언어

    public static DisplaySettingResponse from(UserSetting setting) {
        return DisplaySettingResponse.builder()
                .darkMode(setting.isDarkMode())
                .ddayDisplayEnabled(setting.isDdayDisplayEnabled())
                .language(setting.getLanguage().getLabel())
                .build();
    }
}