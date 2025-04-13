import { http } from "@/utils/http";
import type { RegisterRequest } from "@/types/user";
import type { ResponseResult } from "@/types/http";

// 登录响应数据类型
export interface LoginResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  user: Record<string, any>;
  loginTime: string;
}

// 刷新令牌响应数据类型
export interface RefreshTokenResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
}

// 登录
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

// 注册
export const register=(data: {
  username: string;
  email: string;
  password: string;
  confirmPassword?: string;
}): Promise<ResponseResult<boolean>> =>{
  return http.post("/auth/register", {data});
}

// 退出登录
export const logout=(): Promise<ResponseResult<null>> =>{
  return http.post("/auth/logout");
}

// 发送忘记密码邮件
export const forgotPassword=(
  email: string
): Promise<ResponseResult<boolean>> =>{
  return http.post("/auth/forgot-password", { data : { email } });
}

// 重置密码
export const resetPassword=(
  token: string,
  newPassword: string
): Promise<ResponseResult<boolean>>=> {
  return http.post("/auth/reset-password", { data : {
    token,
    newPassword
  }});
}

// 刷新Token
export const refreshToken=(
  refreshTokenValue: string
): Promise<ResponseResult<RefreshTokenResponse>> => {
  return http.post("/auth/refresh-token", { data : {
    refreshToken: refreshTokenValue
  }});
}
