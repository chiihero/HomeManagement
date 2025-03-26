package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.ResultCode;
import com.chii.homemanagement.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
@ControllerAdvice
public class GlobalExceptionResolver {
    private final Logger logger = LogManager.getLogger(GlobalExceptionResolver.class);
    
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;
    
    /**
     * 是否是开发环境
     */
    private boolean isDev() {
        return "dev".equals(activeProfile);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseInfo<Object> handleBusinessException(HttpServletRequest request, BusinessException e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("业务异常: {}, {}", e.getMessage(), requestInfo);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        // 如果有自定义错误码，使用自定义错误码
        if (e.getCode() != null && !e.getCode().isEmpty()) {
            responseInfo.setCode(Integer.parseInt(e.getCode()));
        } else {
            responseInfo.setCode(ResultCode.COMMON_FAIL.getCode());
        }
        responseInfo.setMsg(e.getMessage());
        return responseInfo;
    }
    
    /**
     * 处理数据访问异常
     */
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseInfo<Object> handleDatabaseException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        logger.error("数据库异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.COMMON_FAIL.getCode());
        responseInfo.setMsg("数据库操作异常，请稍后重试");
        
        // 在开发环境中提供更详细的错误信息
        if (isDev()) {
            responseInfo.setData(e.getMessage());
        }
        
        return responseInfo;
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseInfo<Object> handleValidationException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("参数验证异常: {}, {}", e.getMessage(), requestInfo);
        
        String errorMessage;
        if (e instanceof MethodArgumentNotValidException) {
            FieldError fieldError = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError();
            errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "参数校验错误";
        } else if (e instanceof BindException) {
            FieldError fieldError = ((BindException) e).getBindingResult().getFieldError();
            errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定错误";
        } else {
            errorMessage = "参数验证错误";
        }
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.PARAM_NOT_VALID.getCode());
        responseInfo.setMsg(errorMessage);
        return responseInfo;
    }
    
    /**
     * 处理参数类型异常
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseInfo<Object> handleParameterTypeException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("参数类型异常: {}, {}", e.getMessage(), requestInfo);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.PARAM_TYPE_ERROR.getCode());
        responseInfo.setMsg("参数类型错误");
        
        // 在开发环境中提供更详细的错误信息
        if (isDev()) {
            responseInfo.setData(e.getMessage());
        }
        
        return responseInfo;
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseInfo<Object> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("约束违反异常: {}, {}", e.getMessage(), requestInfo);
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMsg = violations.isEmpty() ? 
                "参数错误" : 
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                    
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.PARAM_NOT_VALID.getCode());
        responseInfo.setMsg(errorMsg);
        return responseInfo;
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseInfo<Object> handleAuthenticationException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("认证异常: {}, {}", e.getMessage(), requestInfo);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.USER_NOT_LOGIN.getCode());
        responseInfo.setMsg("认证失败，请重新登录");
        return responseInfo;
    }
    
    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseInfo<Object> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("授权异常: {}, {}", e.getMessage(), requestInfo);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.NO_PERMISSION.getCode());
        responseInfo.setMsg("无权访问");
        return responseInfo;
    }
    
    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseInfo<Object> handleNoHandlerFoundException(HttpServletRequest request, NoHandlerFoundException e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("资源未找到: {}, {}", e.getMessage(), requestInfo);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.COMMON_FAIL.getCode());
        responseInfo.setMsg("请求的资源不存在");
        return responseInfo;
    }
    
    /**
     * 处理上传文件过大异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseInfo<Object> handleMaxUploadSizeExceededException(HttpServletRequest request, MaxUploadSizeExceededException e) {
        String requestInfo = getRequestInfo(request);
        logger.warn("上传文件过大: {}, {}", e.getMessage(), requestInfo);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.COMMON_FAIL.getCode());
        responseInfo.setMsg("上传文件大小超过限制");
        return responseInfo;
    }
    
    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseInfo<Object> handleIOException(HttpServletRequest request, IOException e) {
        String requestInfo = getRequestInfo(request);
        logger.error("IO异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.COMMON_FAIL.getCode());
        responseInfo.setMsg("IO操作异常，请稍后重试");
        
        // 在开发环境中提供更详细的错误信息
        if (isDev()) {
            responseInfo.setData(e.getMessage());
        }
        
        return responseInfo;
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseInfo<Object> handleException(HttpServletRequest request, Exception e) {
        String requestInfo = getRequestInfo(request);
        logger.error("系统异常: {}, {}", e.getMessage(), requestInfo, e);
        
        ResponseInfo<Object> responseInfo = new ResponseInfo<>();
        responseInfo.setCode(ResultCode.COMMON_FAIL.getCode());
        responseInfo.setMsg("系统错误，请稍后重试");
        
        // 在开发环境中提供更详细的错误信息
        if (isDev()) {
            responseInfo.setData(e.getMessage());
        }
        
        return responseInfo;
    }
    
    /**
     * 获取请求信息，用于日志记录
     */
    private String getRequestInfo(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("URL:").append(request.getRequestURL());
        
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            sb.append("?").append(queryString);
        }
        
        sb.append(", Method:").append(request.getMethod());
        sb.append(", IP:").append(getClientIp(request));
        
        return sb.toString();
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
