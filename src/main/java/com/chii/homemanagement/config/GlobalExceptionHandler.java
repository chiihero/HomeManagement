package com.chii.homemanagement.config;

import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.ResultCode;
import com.chii.homemanagement.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于RESTful API的全局异常处理器
 * 与GlobalExceptionResolver配合使用，处理非ResponseInfo类型的REST响应
 */
@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest()
                .body(ResponseInfo.errorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(ResponseInfo.errorResponse("系统错误，请稍后重试"));
    }
} 