import http from './http'
import type { ResponseResult, PageResult } from '@/types/common'
import type { Reminder, ReminderQueryParams } from '@/types/reminder'

/**
 * 获取提醒列表（根据状态或其他条件查询）
 * @param params 查询参数
 */
// export const getReminders = (params: { userId: number, status?: string, type?: string }): Promise<ResponseResult<Reminder[]>> => {
//   return http.get('/reminders', { params })
// }

/**
 * 获取提醒详情
 * @param id 提醒ID
 */
export const getReminderDetail = (id: string): Promise<ResponseResult<Reminder>> => {
  return http.get(`/reminders/${id}`)
}

/**
 * 添加提醒
 * @param reminder 提醒信息
 */
export const addReminder = (reminder: Omit<Reminder, 'id' | 'status' | 'createdTime' | 'updatedTime'>): Promise<ResponseResult<Reminder>> => {
  return http.post('/reminders', reminder)
}

/**
 * 更新提醒
 * @param reminder 提醒信息
 */
export const updateReminder = (reminder: Partial<Reminder> & { id: string }): Promise<ResponseResult<Reminder>> => {
  return http.put(`/reminders/${reminder.id}`, reminder)
}

/**
 * 删除提醒
 * @param id 提醒ID
 */
export const deleteReminder = (id: string): Promise<ResponseResult<null>> => {
  return http.delete(`/reminders/${id}`)
}

/**
 * 标记提醒为已完成
 * @param id 提醒ID
 */
export const completeReminder = (id: string): Promise<ResponseResult<Reminder>> => {
  return http.put(`/reminders/${id}/process`, {})
}

/**
 * 获取今日提醒
 * @param userId 用户ID
 */
export const getTodayReminders = (userId: number): Promise<ResponseResult<Reminder[]>> => {
  return http.get(`/reminders/today`, { params: { userId } })
}

/**
 * 获取日期范围内提醒
 * @param userId 用户ID
 * @param startDate 开始日期
 * @param endDate 结束日期
 */
export const getRemindersByDateRange = (userId: number, startDate: string, endDate: string): Promise<ResponseResult<Reminder[]>> => {
  return http.get(`/reminders/date-range`, { params: { userId, startDate, endDate } })
}

/**
 * 获取物品的提醒
 * @param entityId 物品ID
 */
export const getRemindersByEntityId = (entityId: string): Promise<ResponseResult<Reminder[]>> => {
  return http.get(`/reminders/entity/${entityId}`)
}

/**
 * 获取特定状态的提醒
 * @param userId 用户ID
 * @param status 状态
 */
export const getRemindersByStatus = (userId: number, status: string): Promise<ResponseResult<Reminder[]>> => {
  return http.get(`/reminders/status`, { params: { userId, status } })
}

/**
 * 为物品生成提醒
 * @param itemId 物品ID
 */
export const generateRemindersForItem = (itemId: string): Promise<ResponseResult<void>> => {
  return http.post(`/reminders/generate/${itemId}`, {})
} 