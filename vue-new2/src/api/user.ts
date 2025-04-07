import http from '@/utils/http'
import type { User } from '@/types/user'
import type { ResponseResult } from '@/types/entity'
import { useUserStoreHook } from '@/store/modules/user'

// 用户个人资料接口
export interface UserProfile {
  id?: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
}

// 用户通知设置接口
export interface UserNotificationSettings {
  emailNotification: boolean
  expirationReminder: boolean
  reminderDays: number
}

// 用户设置接口
export interface UserSettings {
  id?: number
  userId: number
  key: string
  value: string
  updatedAt?: string
}

// 获取当前用户信息
export function getUserInfo(): Promise<ResponseResult<Record<string, any>>> {
  return http.post('/users/info')
}

// 修改密码
export function changePassword(data: {
  currentPassword: string
  newPassword: string
}): Promise<ResponseResult<boolean>> {
  return http.post('/auth/change-password', data)
}

// 更新用户信息
export function updateUserInfo(data: Partial<User>): Promise<ResponseResult<User>> {
  return http.post('/auth/update-info', data)
}

// 获取当前用户个人资料
export const getUserProfile = (): Promise<ResponseResult<UserProfile>> => {
  return http.get('/users/current')
}

// 更新用户个人资料
export const updateUserProfile = (profile: Partial<UserProfile>): Promise<ResponseResult<UserProfile>> => {
  return http.put('/users/profile', profile)
}

// 更新用户密码
export const updateUserPassword = (params: {
  currentPassword: string
  newPassword: string
}): Promise<ResponseResult<boolean>> => {
  return http.put('/users/password', params)
}

// 上传用户头像
export const uploadUserAvatar = (file: File): Promise<ResponseResult<string>> => {
  const formData = new FormData()
  formData.append('file', file)
  
  return http.post('/users/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除用户头像
export const deleteUserAvatar = (): Promise<ResponseResult<boolean>> => {
  return http.delete('/users/avatar')
}

// 更新用户通知设置
export const updateUserNotifications = (data: UserNotificationSettings): Promise<ResponseResult<UserNotificationSettings>> => {
  return http.put('/user/notifications', data)
}

// 获取用户设置
export const getUserSettings = (): Promise<ResponseResult<UserSettings[]>> => {
  return http.get('/settings/user')
}

// 更新用户设置
export const updateUserSetting = (key: string, value: string): Promise<ResponseResult<UserSettings>> => {
  return http.put(`/settings/user/${key}`, { value })
}
