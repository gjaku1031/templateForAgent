package com.gjaku1031.templateforagent.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gjaku1031.templateforagent.common.error.ErrorCode;
import com.gjaku1031.templateforagent.presentation.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 인가 실패(403) 처리 핸들러
 */
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.FORBIDDEN,
                request.getRequestURI(),
                "접근 권한이 없습니다."
        );
        response.getWriter().write(mapper.writeValueAsString(body));
    }
}

