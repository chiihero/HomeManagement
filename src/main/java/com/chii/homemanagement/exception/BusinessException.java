package com.chii.homemanagement.exception;

/**
 * 业务异常
 */
public class BusinessException extends BaseException {
    public BusinessException(String code, String message) {
        super(code, message);
    }

    public BusinessException(String code, String message, Object... args) {
        super(code, message, args);
    }
} 