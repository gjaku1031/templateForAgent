package com.gjaku1031.templateforagent.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 정의
 * <p>HTTP 상태, 내부 코드, 기본 메시지로 구성.</p>
 */
@Getter
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E4010", "Unauthorized"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E4000", "Invalid request"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E4030", "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E4040", "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "E4090", "Conflict occurred"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E5000", "Internal server error");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

