package com.gjaku1031.templateforagent.common.error.exception;

/**
 * 인증 실패/토큰 무효 등 인증 관련 예외
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

