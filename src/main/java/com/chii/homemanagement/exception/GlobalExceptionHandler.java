package com.chii.homemanagement.exception;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * 是否是开发环境
     */
    private boolean isDev() {
        return "dev".equals(activeProfile);
    }

    /**
     * 获取请求信息
     */
    private String getRequestInfo(HttpServletRequest request) {
        return String.format("URL: %s, Method: %s, IP: %s", 
            request.getRequestURL().toString(),
            request.getMethod(),
            request.getRemoteAddr());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleBusinessException(HttpServletRequest request, BusinessException e) {
        String requestInfo = getRequestInfo(request);
        log.warn("业务异常: {}, {}", e.getMessage(), requestInfo);
        
        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleSystemException(HttpServletRequest request, SystemException e) {
        String requestInfo = getRequestInfo(request);
        log.error("系统异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        log.warn("参数验证异常: {}, {}", e.getMessage(), requestInfo);
        
        String errorMessage;
        if (e instanceof MethodArgumentNotValidException) {
            FieldError fieldError = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError();
            errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "参数校验错误";
        } else {
            FieldError fieldError = ((BindException) e).getBindingResult().getFieldError();
            errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定错误";
        }
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.PARAM_TYPE_ERROR.getCode(), errorMessage);
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        String requestInfo = getRequestInfo(request);
        log.warn("约束违反异常: {}, {}", e.getMessage(), requestInfo);
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.isEmpty() ?
                "参数错误" :
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.PARAM_TYPE_ERROR.getCode(), errorMessage);
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理参数类型异常
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleParameterTypeException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        log.warn("参数类型异常: {}, {}", e.getMessage(), requestInfo);
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.PARAM_TYPE_ERROR.getCode(), "参数类型错误");
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleAuthenticationException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        log.warn("认证异常: {}, {}", e.getMessage(), requestInfo);
        return ApiResponse.error(ErrorCode.PERMISSION_UNAUTHORIZED.getCode(), "认证失败，请重新登录");
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        String requestInfo = getRequestInfo(request);
        log.warn("授权异常: {}, {}", e.getMessage(), requestInfo);
        return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问");
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleNoHandlerFoundException(HttpServletRequest request, NoHandlerFoundException e) {
        String requestInfo = getRequestInfo(request);
        log.warn("资源未找到: {}, {}", e.getMessage(), requestInfo);
        return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "请求的资源不存在");
    }

    /**
     * 处理上传文件过大异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<Object> handleMaxUploadSizeExceededException(HttpServletRequest request, MaxUploadSizeExceededException e) {
        String requestInfo = getRequestInfo(request);
        log.warn("上传文件过大: {}, {}", e.getMessage(), requestInfo);
        return ApiResponse.error(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传文件大小超过限制");
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleDatabaseException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        log.error("数据库异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.DATABASE_ERROR.getCode(), "数据库操作异常，请稍后重试");
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleIOException(HttpServletRequest request, IOException e) {
        String requestInfo = getRequestInfo(request);
        log.error("IO异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "IO操作异常，请稍后重试");
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        log.error("未捕获的异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常，请稍后重试");
        if (isDev()) {
            response.setData(e.getMessage());
        }
        return response;
    }
} 