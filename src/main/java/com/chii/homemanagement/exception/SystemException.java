package com.chii.homemanagement.exception;

/**
 * 系统异常
 */
public class SystemException extends BaseException {
    public SystemException(String code, String message) {
        super(code, message);
    }

    public SystemException(String code, String message, Object... args) {
        super(code, message, args);
    }

    public SystemException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
} 