package com.chii.homemanagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 通用响应信息类
 * 
 * @param <T> 响应数据的类型
 */
@Schema(description = "通用响应信息")
public class ResponseInfo<T> {
    /**
     * 响应状态码
     * 200: 成功
     * 0: 业务失败
     */
    @Schema(description = "响应状态码: 200-成功, 0-业务失败")
    private Integer code = 200;
    
    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private String msg = "SUCCESS";
    
    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 无参构造函数
     */
    public ResponseInfo() {
    }

    /**
     * 根据结果枚举构造响应信息
     * 
     * @param resultEnum 结果枚举
     */
    public ResponseInfo(ResultCode resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 设置成功响应数据
     * 
     * @param data 响应数据
     */
    public void success(T data) {
        this.data = data;
        this.code = ResultCode.SUCCESS.getCode();
        this.msg = ResultCode.SUCCESS.getMessage();
    }

    /**
     * 设置失败响应信息
     * 
     * @param msg 失败消息
     */
    public void failure(String msg) {
        this.msg = msg;
        this.code = 0;
    }

    /**
     * 根据结果码设置响应信息
     * 
     * @param resultCode 结果枚举
     */
    public void setResultCode(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMessage();
    }

    /**
     * 创建成功的响应（带数据）
     * 
     * @param <E> 响应数据类型
     * @param data 响应数据
     * @return 响应信息对象
     */
    public static <E> ResponseInfo<E> successResponse(E data) {
        ResponseInfo<E> response = new ResponseInfo<>();
        response.success(data);
        return response;
    }

    /**
     * 创建无数据的成功响应
     * 
     * @param <E> 响应数据类型
     * @return 响应信息对象
     */
    public static <E> ResponseInfo<E> successResponse() {
        return new ResponseInfo<>();
    }

    /**
     * 创建错误响应
     * 
     * @param <E> 响应数据类型
     * @param message 错误消息
     * @return 响应信息对象
     */
    public static <E> ResponseInfo<E> errorResponse(String message) {
        ResponseInfo<E> response = new ResponseInfo<>();
        response.failure(message);
        return response;
    }

    /**
     * 创建基于结果码的响应
     * 
     * @param <E> 响应数据类型
     * @param resultCode 结果码
     * @return 响应信息对象
     */
    public static <E> ResponseInfo<E> response(ResultCode resultCode) {
        return new ResponseInfo<>(resultCode);
    }

    /**
     * 创建基于结果码的响应，并包含数据
     * 
     * @param <E> 响应数据类型
     * @param resultCode 结果码
     * @param data 响应数据
     * @return 响应信息对象
     */
    public static <E> ResponseInfo<E> response(ResultCode resultCode, E data) {
        ResponseInfo<E> response = new ResponseInfo<>(resultCode);
        response.setData(data);
        return response;
    }
}