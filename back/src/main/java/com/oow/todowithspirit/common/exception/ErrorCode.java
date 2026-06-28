package com.oow.todowithspirit.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER"),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),

    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN"),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND"),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");

    private final HttpStatus httpStatus;
    private final String code;
}
