package com.gjaku1031.templateforagent.presentation.common;

import com.gjaku1031.templateforagent.common.error.ErrorCode;
import com.gjaku1031.templateforagent.common.error.exception.BusinessException;
import com.gjaku1031.templateforagent.common.error.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 * <p>도메인/검증/기타 예외를 표준 에러 응답으로 매핑.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.NOT_FOUND, req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorCode code = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.of(code, req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest req) {
        String msg = ex.getMessage();
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST, req.getRequestURI(), msg);
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(body);
    }
}

