// 用户接口定义
export interface User {
  userId: number;
  username: string;
  email: string;
  avatar: string;
  nickname?: string;
  phone: string;
  role: string;
  status: number;
  enabled: boolean;
  lastLoginTime?: string;
  createdTime: string;
  updatedTime?: string;
}

// 用户登录请求参数
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

// 用户注册请求参数
export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword: string;
  email: string;
  nickname?: string;
  phone?: string;
}

// 修改密码请求参数
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// 用户列表查询参数
export interface UserQueryParams {
  current?: number;
  size?: number;
  username?: string;
  email?: string;
  role?: string;
  enabled?: boolean;
}
