package com.oow.todowithspirit.config;

import tools.jackson.databind.ObjectMapper;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.common.response.ApiResponse;
import com.oow.todowithspirit.common.response.ErrorDetail;
import com.oow.todowithspirit.common.response.FieldErrorInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        ErrorDetail errorDetail = ErrorDetail.of(
                401,
                ErrorCode.UNAUTHORIZED.getCode(),
                List.of(FieldErrorInfo.of("Authentication required"))
        );

        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(errorDetail)));
    }
}