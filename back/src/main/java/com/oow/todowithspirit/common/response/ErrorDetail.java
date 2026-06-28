package com.oow.todowithspirit.common.response;

import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class ErrorDetail {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

    private final int status;
    private final String timestamp;
    private final String errorCode;
    private final List<FieldErrorInfo> description;

    private ErrorDetail(int status, String errorCode, List<FieldErrorInfo> description) {
        this.status = status;
        this.timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(FORMATTER);
        this.errorCode = errorCode;
        this.description = description;
    }

    public static ErrorDetail of(int status, String errorCode, List<FieldErrorInfo> description) {
        return new ErrorDetail(status, errorCode, description);
    }
}