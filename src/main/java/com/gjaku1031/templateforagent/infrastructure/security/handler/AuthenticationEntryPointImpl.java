package com.gjaku1031.templateforagent.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gjaku1031.templateforagent.common.error.ErrorCode;
import com.gjaku1031.templateforagent.presentation.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 인증 실패(401) 처리 엔트리포인트
 */
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED,
                request.getRequestURI(),
                "인증이 필요합니다. 올바른 인증 정보를 제공해주세요."
        );
        response.getWriter().write(mapper.writeValueAsString(body));
    }
}

