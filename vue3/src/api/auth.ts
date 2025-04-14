/**
 * 认证相关API接口
 * 处理用户登录、注册、登出、令牌刷新等身份验证相关功能
 */
import { http } from "@/utils/http";
import type { RegisterRequest } from "@/types/user";
import type { ResponseResult } from "@/types/http";

/**
 * 登录响应数据接口
 */
export interface LoginResponse {
  /** 访问令牌 */
  token: string;
  /** 刷新令牌 */
  refreshToken: string;
  /** 令牌有效期（秒） */
  expiresIn: number;
  /** 用户信息 */
  user: Record<string, any>;
  /** 登录时间 */
  loginTime: string;
}

/**
 * 刷新令牌响应数据接口
 */
export interface RefreshTokenResponse {
  /** 新的访问令牌 */
  token: string;
  /** 新的刷新令牌 */
  refreshToken: string;
  /** 新令牌有效期（秒） */
  expiresIn: number;
}

/**
 * 用户登录
 * @param username - 用户名
 * @param password - 密码
 * @param rememberMe - 是否记住登录状态
 * @returns 登录结果和用户信息
 */
export const login = (
  username: string,
  password: string,
  rememberMe: boolean = false
): Promise<ResponseResult<LoginResponse>> => {
  return http.post("/auth/login", { data : {
    username,
    password,
    rememberMe
  }});
}

/**
 * 用户注册
 * @param data - 注册信息对象
 * @returns 注册结果
 */
export const register=(data: {
  username: string;
  email: string;
  password: string;
  confirmPassword?: string;
}): Promise<ResponseResult<boolean>> =>{
  return http.post("/auth/register", {data});
}

/**
 * 用户登出
 * @returns 登出结果
 */
export const logout=(): Promise<ResponseResult<null>> =>{
  return http.post("/auth/logout");
}

/**
 * 发送忘记密码邮件
 * @param email - 用户注册邮箱
 * @returns 发送结果
 */
export const forgotPassword=(
  email: string
): Promise<ResponseResult<boolean>> =>{
  return http.post("/auth/forgot-password", { data : { email } });
}

/**
 * 重置密码
 * @param token - 重置密码令牌（通过邮件获取）
 * @param newPassword - 新密码
 * @returns 重置结果
 */
export const resetPassword=(
  token: string,
  newPassword: string
): Promise<ResponseResult<boolean>>=> {
  return http.post("/auth/reset-password", { data : {
    token,
    newPassword
  }});
}

/**
 * 刷新访问令牌
 * @param refreshTokenValue - 刷新令牌
 * @returns 新的令牌信息
 */
export const refreshToken=(
  refreshTokenValue: string
): Promise<ResponseResult<RefreshTokenResponse>> => {
  return http.post("/auth/refresh-token", { data : {
    refreshToken: refreshTokenValue
  }});
}
