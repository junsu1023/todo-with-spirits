package com.oow.todowithspirit.common.exception;

import com.oow.todowithspirit.common.response.FieldErrorInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<FieldErrorInfo> description;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.description = List.of(FieldErrorInfo.of(errorCode.getCode()));
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.description = List.of(FieldErrorInfo.of(message));
    }

    public ApiException(ErrorCode errorCode, String field, String message) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.description = List.of(FieldErrorInfo.of(field, message));
    }

    public ApiException(ErrorCode errorCode, List<FieldErrorInfo> description) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.description = description;
    }
}