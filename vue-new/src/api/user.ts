import http from './http';
import { User } from '@/types/user';
import { ResponseResult } from '@/types/entity';

// 用户个人资料接口
export interface UserProfile {
  id?: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
}

// 用户通知设置接口
export interface UserNotificationSettings {
  emailNotification: boolean;
  expirationReminder: boolean;
  reminderDays: number;
}

// 用户设置接口
export interface UserSettings {
  id?: number;
  userId: number;
  key: string;
  value: string;
  updatedAt?: string;
}

// 获取当前用户信息
export function getUserInfo(): Promise<ResponseResult<Record<string, any>>> {
  return http.post<ResponseResult<Record<string, any>>>('/auth/info')
    .then(res => res.data);
}

// 修改密码
export function changePassword(data: {
  currentPassword: string;
  newPassword: string;
}): Promise<ResponseResult<boolean>> {
  return http.post<ResponseResult<boolean>>('/auth/change-password', data)
    .then(res => res.data);
}

// 更新用户信息
export function updateUserInfo(data: Partial<User>): Promise<ResponseResult<User>> {
  return http.post<ResponseResult<User>>('/auth/update-info', data)
    .then(res => res.data);
}

// 获取当前用户个人资料
export const getUserProfile = () => {
  return http.get<ResponseResult<UserProfile>>('/users/current')
    .then(res => res.data);
}

// 更新用户个人资料
export const updateUserProfile = (profile: Partial<UserProfile>) => {
  return http.put<ResponseResult<UserProfile>>('/users/profile', profile)
    .then(res => res.data);
}

// 更新用户密码
export const updateUserPassword = (params: {
  currentPassword: string;
  newPassword: string;
}) => {
  return http.put<ResponseResult<boolean>>('/users/password', params)
    .then(res => res.data);
}

// 上传用户头像
export const uploadUserAvatar = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return http.post<ResponseResult<{url: string}>>('/users/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => res.data);
}

// 删除用户头像
export const deleteUserAvatar = () => {
  return http.delete<ResponseResult<boolean>>('/users/avatar')
    .then(res => res.data);
}

// 更新用户通知设置
export const updateUserNotifications = (data: UserNotificationSettings) => {
  return http.put<ResponseResult<UserProfile>>('/user/notifications', data)
    .then(res => res.data);
}

// 获取用户设置
export const getUserSettings = () => {
  return http.get<ResponseResult<UserSettings[]>>('/settings/user')
    .then(res => res.data);
}

// 更新用户设置
export const updateUserSetting = (key: string, value: string) => {
  return http.put<ResponseResult<UserSettings>>(`/settings/user/${key}`, { value })
    .then(res => res.data);
} 