package com.chii.homemanagement.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    // 成功
    SUCCESS("200", "成功"),
    
    // 默认失败
    COMMON_FAIL("999", "失败"),
    
    // 参数错误：1000～1999
    PARAM_NOT_VALID("1001", "参数无效"),
    PARAM_IS_BLANK("1002", "参数为空"),
    PARAM_TYPE_ERROR("1003", "参数类型错误"),
    PARAM_NOT_COMPLETE("1004", "参数缺失"),
    
    // 用户错误：2000～2999
    USER_NOT_LOGIN("2001", "用户未登录"),
    USER_ACCOUNT_EXPIRED("2002", "账号已过期"),
    USER_CREDENTIALS_ERROR("2003", "密码错误"),
    USER_CREDENTIALS_EXPIRED("2004", "密码过期"),
    USER_ACCOUNT_DISABLE("2005", "账号不可用"),
    USER_ACCOUNT_LOCKED("2006", "账号被锁定"),
    USER_ACCOUNT_NOT_EXIST("2007", "账号不存在"),
    USER_ACCOUNT_ALREADY_EXIST("2008", "账号已存在"),
    USER_ACCOUNT_USE_BY_OTHERS("2009", "账号下线"),
    REFRESH_TOKEN_INVALID("2010", "刷新令牌无效或已过期"),
    REFRESH_TOKEN_MISSING("2011", "刷新令牌缺失"),
    
    // 权限错误：3000～3999
    NO_PERMISSION("3001", "没有权限"),
    PERMISSION_NO_ACCESS("3002", "无访问权限"),
    PERMISSION_UNAUTHORIZED("3003", "未授权"),
    PERMISSION_DENIED("3004", "权限不足"),
    
    // 请求错误：4000～4999
    NOT_FOUND("4001", "请求的资源不存在"),
    METHOD_NOT_ALLOWED("4002", "请求方法不允许"),
    REQUEST_TIMEOUT("4003", "请求超时"),
    UNSUPPORTED_MEDIA_TYPE("4004", "不支持的媒体类型"),
    TOO_MANY_REQUESTS("4005", "请求频率过高"),
    
    // 系统错误：5000～5999
    SYSTEM_ERROR("5001", "系统内部错误"),
    SYSTEM_BUSY("5002", "系统繁忙"),
    DATABASE_ERROR("5003", "数据库错误"),
    IO_ERROR("5004", "IO错误"),
    FILE_NOT_FOUND("5005", "文件不存在"),
    FILE_UPLOAD_ERROR("5006", "文件上传错误"),
    FILE_DOWNLOAD_ERROR("5007", "文件下载错误"),
    
    // 业务错误：6000～6999
    BUSINESS_ERROR("6001", "业务逻辑错误"),
    DATA_NOT_EXIST("6002", "数据不存在"),
    DATA_ALREADY_EXIST("6003", "数据已存在"),
    DATA_HAS_BEEN_DELETED("6004", "数据已被删除"),
    DATA_HAS_BEEN_LOCKED("6005", "数据已被锁定"),
    DATA_VALIDATE_FAILED("6006", "数据验证失败");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取message
     *
     * @param code 状态码
     * @return 对应的消息
     */
    public static String getMessageByCode(String code) {
        for (ErrorCode ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getMessage();
            }
        }
        return null;
    }
    
    /**
     * 根据code获取枚举对象
     *
     * @param code 状态码
     * @return 对应的枚举对象
     */
    public static ErrorCode getByCode(String code) {
        for (ErrorCode ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele;
            }
        }
        return COMMON_FAIL;
    }
} 