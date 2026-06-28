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

    // 409
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL"),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN"),

    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN"),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND"),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");

    private final HttpStatus httpStatus;
    private final String code;
}
