package com.gjaku1031.templateforagent.common.error.exception;

import com.gjaku1031.templateforagent.common.error.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 규칙 위반 예외
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

