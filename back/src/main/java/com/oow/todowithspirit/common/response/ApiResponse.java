package com.oow.todowithspirit.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final String result;
    private final T detail;

    public static <T> ApiResponse<T> success(T detail) {
        return new ApiResponse<>("success", detail);
    }

    public static ApiResponse<ErrorDetail> fail(ErrorDetail detail) {
        return new ApiResponse<>("fail", detail);
    }
}