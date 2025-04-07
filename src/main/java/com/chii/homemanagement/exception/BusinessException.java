package com.chii.homemanagement.exception;

/**
 * 业务异常
 */
public class BusinessException extends BaseException {
    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(Integer code, String message, Object... args) {
        super(code, message, args);
    }
} 