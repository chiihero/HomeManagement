package com.chii.homemanagement.exception;

/**
 * 系统异常
 */
public class SystemException extends BaseException {
    public SystemException(Integer code, String message) {
        super(code, message);
    }

    public SystemException(Integer code, String message, Object... args) {
        super(code, message, args);
    }

    public SystemException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
} 