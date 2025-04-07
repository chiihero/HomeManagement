package com.chii.homemanagement.exception;

import lombok.Getter;

/**
 * 基础异常类
 */
@Getter
public class BaseException extends RuntimeException {
    private final Integer code;
    private final String message;
    private final Object[] args;

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = new Object[0];
    }

    public BaseException(Integer code, String message, Object... args) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = args;
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.args = new Object[0];
    }
} 