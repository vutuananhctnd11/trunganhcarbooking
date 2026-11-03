package com.example.trunganhcar.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public CustomException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = ErrorCode.BAD_REQUEST.getCode();
    }

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = status.name();
    }

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.status = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }

    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.status = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }

    public CustomException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR.getCode();
    }
}

