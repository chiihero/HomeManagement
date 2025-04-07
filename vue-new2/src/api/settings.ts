import  http from "@/utils/http";
import type { ResponseResult } from '@/types/entity'

/**
 * 系统设置接口
 */
export interface SystemSettings {
  id?: number
  name: string
  value: string
  description?: string
  group: string
  type: 'string' | 'number' | 'boolean' | 'date' | 'object'
  updatedAt?: string
  systemName: string
  logo: string
  version: string
}

/**
 * 用户设置接口
 */
export interface UserSettings {
  id?: number
  userId: number
  key: string
  value: string
  updatedAt?: string
}

/**
 * 备份信息接口
 */
export interface BackupInfo {
  id: number
  filename: string
  size: number
  createdAt: string
  notes?: string
}

/**
 * 用户通知设置接口
 */
export interface UserNotificationSettings {
  emailNotification: boolean
  expirationReminder: boolean
  reminderDays: number
}

/**
 * 获取系统设置
 */
export function getSystemSettings(): Promise<ResponseResult<SystemSettings[]>> {
  return http.get('/settings/system')
}

/**
 * 更新系统设置
 * @param data 系统设置数据
 */
export function updateSystemSettings(data: Partial<SystemSettings>): Promise<ResponseResult<SystemSettings>> {
  return http.put('/settings/system', data)
}

/**
 * 获取用户设置
 */
export function getUserSettings(): Promise<ResponseResult<UserSettings[]>> {
  return http.get('/settings/user')
}

/**
 * 更新用户设置
 * @param key 设置键
 * @param value 设置值
 */
export function updateUserSetting(key: string, value: string): Promise<ResponseResult<UserSettings>> {
  return http.put(`/settings/user/${key}`, { value })
}

/**
 * 创建备份
 * @param notes 备份说明
 */
export function createBackup(notes?: string): Promise<ResponseResult<BackupInfo>> {
  return http.post('/settings/backup', { notes })
}

/**
 * 获取备份列表
 */
export function getBackups(): Promise<ResponseResult<BackupInfo[]>> {
  return http.get('/settings/backup')
}

/**
 * 恢复备份
 * @param id 备份ID
 */
export function restoreBackup(id: number): Promise<ResponseResult<boolean>> {
  return http.post(`/settings/backup/${id}/restore`)
}

/**
 * 删除备份
 * @param id 备份ID
 */
export function deleteBackup(id: number): Promise<ResponseResult<null>> {
  return http.delete(`/settings/backup/${id}`)
}

/**
 * 下载备份
 * @param id 备份ID
 */
export function downloadBackup(id: number): Promise<Blob> {
  return http.get(`/settings/backup/${id}/download`, { responseType: 'blob' })
}

/**
 * 导入数据
 * @param file 导入文件
 */
export function importData(file: File): Promise<ResponseResult<boolean>> {
  const formData = new FormData()
  formData.append('file', file)
  
  return http.post('/settings/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出数据
 * @param type 导出类型
 */
export function exportData(type: 'json' | 'csv' | 'excel' = 'json'): Promise<Blob> {
  return http.get(`/settings/export?type=${type}`, { responseType: 'blob' })
}

/**
 * 更新用户通知设置
 * @param data 通知设置数据
 */
export function updateUserNotifications(data: UserNotificationSettings): Promise<ResponseResult<UserNotificationSettings>> {
  return http.put('/user/notifications', data)
}

/**
 * 上传系统Logo
 * @param file 图片文件
 */
export function uploadSystemLogo(file: File): Promise<ResponseResult<string>> {
  const formData = new FormData()
  formData.append('file', file)
  
  return http.post('/settings/logo', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 