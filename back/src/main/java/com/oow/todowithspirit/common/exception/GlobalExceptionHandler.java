package com.oow.todowithspirit.common.exception;

import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.common.response.ErrorDetail;
import com.oow.todowithspirit.common.response.FieldErrorInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(
                        ErrorDetail.of(errorCode.getHttpStatus().value(), errorCode.getCode(), e.getDescription())
                ));
    }

    // @Valid, @Validated 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldErrorInfo> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorInfo.of(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        ErrorDetail.of(400, ErrorCode.INVALID_PARAMETER.getCode(), errors)
                ));
    }

    // 요청 body 파싱 실패 (JSON 문법 오류, enum에 없는 값 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        List<FieldErrorInfo> errors = List.of(
                FieldErrorInfo.of("Request body is missing or contains an invalid value")
        );

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        ErrorDetail.of(400, ErrorCode.INVALID_PARAMETER.getCode(), errors)
                ));
    }

    // @RequestParam 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMissingParameter(MissingServletRequestParameterException e) {
        List<FieldErrorInfo> errors = List.of(
                FieldErrorInfo.of(e.getParameterName(), e.getParameterName() + " is required")
        );

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        ErrorDetail.of(400, ErrorCode.MISSING_PARAMETER.getCode(), errors)
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleException(Exception e) {
        List<FieldErrorInfo> errors = List.of(FieldErrorInfo.of(e.getMessage()));

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(
                        ErrorDetail.of(500, ErrorCode.INTERNAL_SERVER_ERROR.getCode(), errors)
                ));
    }
}