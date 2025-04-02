import http from './http';
import { User, LoginRequest, RegisterRequest, ChangePasswordRequest } from '@/types/user';
import { ResponseResult } from '@/types/entity';

// 登录响应数据类型
interface LoginResponse {
  token: string;
  user: User;
  loginTime: string;
}

// 登录
export function login(username: string, password: string, rememberMe: boolean = false) {
  return http.post<ResponseResult<LoginResponse>>('/auth/login', {
    username,
    password,
    rememberMe
  });
}

// 注册
export function register(data: RegisterRequest) {
  return http.post<ResponseResult<User>>('/auth/register', data);
}

// 退出登录
export function logout() {
  return http.post<ResponseResult<null>>('/auth/logout');
}

// 获取当前用户信息
export function getUserInfo() {
  return http.get<ResponseResult<User>>('/auth/info');
}

// 修改密码
export function changePassword(data: ChangePasswordRequest) {
  return http.post<ResponseResult<boolean>>('/auth/change-password', data);
}

// 发送重置密码邮件
export function sendResetPasswordEmail(email: string) {
  return http.post<ResponseResult<boolean>>('/auth/send-reset-password-email', { email });
}

// 重置密码
export function resetPassword(token: string, newPassword: string) {
  return http.post<ResponseResult<boolean>>('/auth/reset-password', {
    token,
    newPassword
  });
}

// 更新用户信息
export function updateUserInfo(data: Partial<User>) {
  return http.post<ResponseResult<User>>('/auth/update-info', data);
}

// 上传用户头像
export function uploadAvatar(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  
  return http.post<ResponseResult<{ avatarUrl: string }>>('/auth/upload-avatar', formData);
} 