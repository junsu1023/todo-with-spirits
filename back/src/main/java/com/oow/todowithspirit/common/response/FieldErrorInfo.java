package com.oow.todowithspirit.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FieldErrorInfo {

    private final String field;
    private final String message;

    public static FieldErrorInfo of(String field, String message) {
        return new FieldErrorInfo(field, message);
    }

    public static FieldErrorInfo of(String message) {
        return new FieldErrorInfo(null, message);
    }
}