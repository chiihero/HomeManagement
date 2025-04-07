package com.chii.homemanagement.exception;

/**
 * 参数验证异常
 */
public class ValidationException extends BaseException {
    public ValidationException(Integer code, String message) {
        super(code, message);
    }

    public ValidationException(Integer code, String message, Object... args) {
        super(code, message, args);
    }
} 