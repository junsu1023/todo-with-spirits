package com.oow.todowithspirit.dto.setting;

import com.oow.todowithspirit.domain.setting.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisplaySettingUpdateRequest {

    private Boolean darkMode;           // 다크모드
    private Boolean ddayDisplayEnabled; // 플랜 디데이 표시
    private Language language;          // 언어
}