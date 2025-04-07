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
  return http.post('/users/info');
}

// 修改密码
export function changePassword(data: {
  currentPassword: string;
  newPassword: string;
}): Promise<ResponseResult<boolean>> {
  return http.post('/auth/change-password', data);
}

// 更新用户信息
export function updateUserInfo(data: Partial<User>): Promise<ResponseResult<User>> {
  return http.post('/auth/update-info', data);
}

// 获取当前用户个人资料
export const getUserProfile = () => {
  return http.get('/users/current');
}

// 更新用户个人资料
export const updateUserProfile = (profile: Partial<UserProfile>) => {
  return http.put('/users/profile', profile);
}

// 更新用户密码
export const updateUserPassword = (params: {
  currentPassword: string;
  newPassword: string;
}) => {
  return http.put('/users/password', params);
}

// 上传用户头像
export const uploadUserAvatar = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return http.post('/users/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 删除用户头像
export const deleteUserAvatar = () => {
  return http.delete<ResponseResult<boolean>>('/users/avatar');
}

// 更新用户通知设置
export const updateUserNotifications = (data: UserNotificationSettings) => {
  return http.put('/user/notifications', data);
}

// 获取用户设置
export const getUserSettings = () => {
  return http.get('/settings/user');
}

// 更新用户设置
export const updateUserSetting = (key: string, value: string) => {
  return http.put(`/settings/user/${key}`, { value });
} 