package com.gjaku1031.templateforagent.presentation.common;

import com.gjaku1031.templateforagent.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 공통 에러 응답 DTO
 * <p>에러 코드/메시지/요청 경로/타임스탬프를 포함.</p>
 */
@Getter
public class ErrorResponse {
    private final String code;
    private final String message;
    private final String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    @Builder
    private ErrorResponse(String code, String message, String path, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    }

    /**
     * ErrorResponse 생성 헬퍼
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, String overrideMessage) {
        return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(overrideMessage != null ? overrideMessage : errorCode.getMessage())
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

