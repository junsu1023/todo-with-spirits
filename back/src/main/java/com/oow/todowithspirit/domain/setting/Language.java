package com.oow.todowithspirit.domain.setting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Language {
    SYSTEM("system", "시스템"),
    KO("ko", "한국어"),
    EN("en", "영어"),
    JA("ja", "일본어"),
    ZH("zh", "중국어");

    @JsonValue
    private final String label;
    private final String description;

    Language(String label, String description) {
        this.label = label;
        this.description = description;
    }

    @JsonCreator
    public static Language fromLabel(String label) {
        for (Language language : values()) {
            if (language.label.equalsIgnoreCase(label)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown language: " + label);
    }
}