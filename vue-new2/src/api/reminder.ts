import http from "@/utils/http";
import type {
  Reminder,
  ReminderFormData,
  ReminderQueryParams
} from "@/types/reminder";
import type { ResponseResult } from "@/types/http";

/**
 * 获取提醒列表
 * @param params 查询参数
 */
export const fetchReminders = (params: ReminderQueryParams) => {
  return http.get<ResponseResult<{ list: Reminder[]; total: number }>>(
    "/reminders",
    { params }
  );
};

/**
 * 获取提醒详情
 * @param id 提醒ID
 */
export const fetchReminderDetail = (id: number) => {
  return http.get<ResponseResult<Reminder>>(`/reminders/${id}`);
};

/**
 * 创建提醒
 * @param data 提醒信息
 */
export const createReminder = (data: ReminderFormData) => {
  return http.post<ResponseResult<Reminder>>("/reminders", data);
};

/**
 * 更新提醒
 * @param id 提醒ID
 * @param data 提醒信息
 */
export const updateReminder = (id: number, data: ReminderFormData) => {
  return http.put<ResponseResult<Reminder>>(`/reminders/${id}`, data);
};

/**
 * 删除提醒
 * @param id 提醒ID
 */
export const deleteReminder = (id: number) => {
  return http.delete<ResponseResult<void>>(`/reminders/${id}`);
};

/**
 * 完成提醒
 * @param id 提醒ID
 */
export const completeReminder = (id: number) => {
  return http.put<ResponseResult<Reminder>>(`/reminders/${id}/complete`);
};



/**
 * 搜索提醒
 * @param keyword 关键词
 */
export const searchReminders = (keyword: string) => {
  return http.get<ResponseResult<Reminder[]>>("/reminders/search", {
    params: { keyword }
  });
};

/**
 * 获取即将到期的提醒
 * @param days 天数
 */
export const fetchUpcomingReminders = (days: number = 7) => {
  return http.get<ResponseResult<Reminder[]>>("/reminders/upcoming", {
    params: { days }
  });
};

/**
 * 获取过期的提醒
 */
export const fetchExpiredReminders = () => {
  return http.get<ResponseResult<Reminder[]>>("/reminders/expired");
};

/**
 * 批量更新提醒状态
 * @param ids 提醒ID数组
 * @param status 状态
 */
export const updateBatchReminderStatus = (ids: number[], status: string) => {
  return http.patch<ResponseResult<void>>("/reminders/batch/status", {
    ids,
    status
  });
};

/**
 * 批量删除提醒
 * @param ids 提醒ID数组
 */
export const deleteBatchReminders = (ids: number[]) => {
  return http.delete<ResponseResult<void>>("/reminders/batch", {
    data: { ids }
  });
};

/**
 * 获取今日提醒
 * @param userId 用户ID
 */
export const getTodayReminders = (userId: number) => {
  return http.get<ResponseResult<Reminder[]>>("/reminders/today", {
    params: { userId }
  });
};

/**
 * 获取日期范围内提醒
 * @param userId 用户ID
 * @param startDate 开始日期
 * @param endDate 结束日期
 */
export const getRemindersByDateRange = (
  userId: number,
  startDate: string,
  endDate: string
) => {
  return http.get<ResponseResult<Reminder[]>>("/reminders/date-range", {
    params: { userId, startDate, endDate }
  });
};

/**
 * 获取物品的提醒
 * @param entityId 物品ID
 */
export const getRemindersByEntityId = (entityId: string) => {
  return http.get<ResponseResult<Reminder[]>>(`/reminders/entity/${entityId}`);
};

/**
 * 获取特定状态的提醒
 * @param userId 用户ID
 * @param status 状态
 */
export const getRemindersByStatus = (userId: number, status: string) => {
  return http.get<ResponseResult<Reminder[]>>("/reminders/status", {
    params: { userId, status }
  });
};

/**
 * 为物品生成提醒
 * @param itemId 物品ID
 */
export const generateRemindersForItem = (itemId: string) => {
  return http.post<ResponseResult<void>>(`/reminders/generate/${itemId}`, {});
};
