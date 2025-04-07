import http from './http'
import { ResponseResult } from '@/types/entity';

export interface SystemSettings {
  id?: number;
  name: string;
  value: string;
  description?: string;
  group: string;
  type: 'string' | 'number' | 'boolean' | 'date' | 'object';
  updatedAt?: string;
  systemName: string;
  logo: string;
  version: string;
}

export interface UserSettings {
  id?: number;
  userId: number;
  key: string;
  value: string;
  updatedAt?: string;
}

export interface BackupInfo {
  id: number;
  filename: string;
  size: number;
  createdAt: string;
  notes?: string;
}

// 用户个人资料接口
export interface UserProfile {
  id?: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
}

// 响应格式接口
export interface ResponseData<T> {
  code: number;
  message: string;
  data: T;
}

// 用户通知设置接口
export interface UserNotificationSettings {
  emailNotification: boolean;
  expirationReminder: boolean;
  reminderDays: number;
}

// 获取系统设置
export const getSystemSettings = () => {
  return http.get('/settings/system');
}

// 更新系统设置
export const updateSystemSettings = (data: Partial<SystemSettings>) => {
  return http.put('/settings/system', data);
}

// 获取用户设置
export const getUserSettings = () => {
  return http.get<UserSettings[]>('/settings/user')
}

// 更新用户设置
export const updateUserSetting = (key: string, value: string) => {
  return http.put<UserSettings>(`/settings/user/${key}`, { value })
}

// 创建备份
export const createBackup = (notes?: string) => {
  return http.post('/settings/backup', { notes });
}

// 获取备份列表
export const getBackups = () => {
  return http.get('/settings/backup');
}

// 恢复备份
export const restoreBackup = (id: number) => {
  return http.post(`/settings/backup/${id}/restore`);
}

// 删除备份
export const deleteBackup = (id: number) => {
  return http.delete<ResponseResult<null>>(`/settings/backup/${id}`);
}

// 下载备份
export const downloadBackup = (id: number) => {
  return http.get(`/settings/backup/${id}/download`, { responseType: 'blob' });
}

// 导入数据
export const importData = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/settings/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 导出数据
export const exportData = (type: 'json' | 'csv' | 'excel' = 'json') => {
  return http.get(`/settings/export?type=${type}`, { responseType: 'blob' });
}

// 更新用户通知设置
export const updateUserNotifications = (data: UserNotificationSettings) => {
  return http.put<ResponseData<UserProfile>>('/user/notifications', data)
}

// 上传系统Logo
export const uploadSystemLogo = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return http.post('/settings/logo', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
} 