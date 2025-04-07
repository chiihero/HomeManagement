import http from './http';
import { RegisterRequest } from '@/types/user';
import { ResponseResult } from '@/types/entity';

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
export function login(username: string, password: string, rememberMe: boolean = false): Promise<ResponseResult<LoginResponse>> {
  return http.post<ResponseResult<LoginResponse>>('/auth/login', {
    username,
    password,
    rememberMe
  }).then(res => res.data);
}

// 注册
export function register(data: RegisterRequest): Promise<ResponseResult<boolean>> {
  return http.post<ResponseResult<boolean>>('/auth/register', data)
    .then(res => res.data);
}

// 退出登录
export function logout(): Promise<ResponseResult<null>> {
  return http.post<ResponseResult<null>>('/auth/logout')
    .then(res => res.data);
}

// 发送忘记密码邮件
export function forgotPassword(email: string): Promise<ResponseResult<boolean>> {
  return http.post<ResponseResult<boolean>>('/auth/forgot-password', { email })
    .then(res => res.data);
}

// 重置密码
export function resetPassword(token: string, newPassword: string): Promise<ResponseResult<boolean>> {
  return http.post<ResponseResult<boolean>>('/auth/reset-password', {
    token,
    newPassword
  }).then(res => res.data);
}

// 刷新Token
export function refreshToken(refreshTokenValue: string): Promise<ResponseResult<RefreshTokenResponse>> {
  return http.post<ResponseResult<RefreshTokenResponse>>('/auth/refresh-token', { 
    refreshToken: refreshTokenValue 
  }).then(res => res.data);
}