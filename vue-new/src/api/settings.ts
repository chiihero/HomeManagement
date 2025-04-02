import http from './http'

export interface SystemSettings {
  id?: number;
  name: string;
  value: string;
  description?: string;
  group: string;
  type: 'string' | 'number' | 'boolean' | 'date' | 'object';
  updatedAt?: string;
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

// 获取系统设置
export const getSystemSettings = (group?: string) => {
  const params = group ? { group } : undefined
  return http.get<SystemSettings[]>('/settings/system', { params })
}

// 更新系统设置
export const updateSystemSetting = (id: number, value: string) => {
  return http.put<SystemSettings>(`/settings/system/${id}`, { value })
}

// 批量更新系统设置
export const updateSystemSettings = (settings: Array<{ id: number, value: string }>) => {
  return http.put<SystemSettings[]>('/settings/system', settings)
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
  return http.post<BackupInfo>('/settings/backup', { notes })
}

// 获取备份列表
export const getBackups = () => {
  return http.get<BackupInfo[]>('/settings/backup')
}

// 恢复备份
export const restoreBackup = (id: number) => {
  return http.post(`/settings/backup/${id}/restore`)
}

// 删除备份
export const deleteBackup = (id: number) => {
  return http.delete(`/settings/backup/${id}`)
}

// 下载备份
export const downloadBackup = (id: number) => {
  return http.get(`/settings/backup/${id}/download`, { responseType: 'blob' })
}

// 导入数据
export const importData = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/settings/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出数据
export const exportData = (type: 'json' | 'csv' | 'excel' = 'json') => {
  return http.get(`/settings/export?type=${type}`, { responseType: 'blob' })
} 