import { http } from "@/utils/http";
import type {
  Reminder,
  ReminderQueryParams
} from "@/types/reminder";
import type { ResponseResult } from "@/types/http";

/**
 * 获取提醒列表
 * @param params 查询参数
 */
export const fetchReminders = (params: ReminderQueryParams) => {
  // 适配后端分页参数
  const queryParams = {
    userId: params.userId,
    entityName: params.entityName,
    type: params.type,
    status: params.status,
    page: params.page,
    size: params.size
  };
  
  // 处理日期范围参数，如果需要使用日期范围，应该调用getRemindersByDateRange方法
  
  return http.get<ResponseResult<Reminder[]>,ReminderQueryParams>("/reminders", { params: queryParams });
};

/**
 * 获取提醒详情
 * @param id 提醒ID
 */
export const fetchReminderDetail = (id: number) => {
  return http.get<ResponseResult<Reminder>,number>(`/reminders/${id}`);
};

/**
 * 创建提醒
 * @param data 提醒信息
 */
export const createReminder = (data: Reminder) => {
  return http.post<ResponseResult<Reminder>,Reminder>("/reminders", {data:data});
};

/**
 * 更新提醒
 * @param id 提醒ID
 * @param data 提醒信息
 */
export const updateReminder = (id: number, data: Reminder) => {
  return http.put<ResponseResult<Reminder>,object>(`/reminders/${id}`, {data:data});
};

/**
 * 删除提醒
 * @param id 提醒ID
 */
export const deleteReminder = (id: number) => {
  return http.delete<ResponseResult<Boolean>,number>(`/reminders/${id}`);
};

/**
 * 处理提醒（标记为已处理）
 * @param id 提醒ID
 */
export const processReminder = (id: number) => {
  return http.put<ResponseResult<Reminder>,number>(`/reminders/${id}/process`);
};

/**
 * 搜索提醒
 * @param keyword 关键词
 */
export const searchReminders = (keyword: string) => {
  return http.get<ResponseResult<Reminder[]>,string>("/reminders/search", {
    params: { keyword }
  });
};

/**
 * 获取即将到期的提醒
 * @param days 天数
 */
export const fetchUpcomingReminders = (days: number = 7) => {
  return http.get<ResponseResult<Reminder[]>,number>("/reminders/upcoming", {
    params: { days }
  });
};

/**
 * 获取过期的提醒
 */
export const fetchExpiredReminders = () => {
  return http.get<ResponseResult<Reminder[]>,void>("/reminders/expired");
};



/**
 * 批量删除提醒
 * @param ids 提醒ID数组
 */
export const deleteBatchReminders = (ids: number[]) => {
  return http.delete<ResponseResult<Boolean>, number[]>("/reminders/batch", {
    data: { ids }
  });
};

/**
 * 获取今日提醒
 * @param userId 用户ID
 */
export const getTodayReminders = (userId: string) => {
  return http.get<ResponseResult<Reminder[]>,number>("/reminders/today", {
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
  userId: string,
  startDate: string,
  endDate: string
) => {
  return http.get<ResponseResult<Reminder[]>,object>("/reminders/date-range", {
    params: { userId, startDate, endDate }
  });
};

/**
 * 获取物品的提醒
 * @param entityId 物品ID
 */
export const getRemindersByEntityId = (entityId: number) => {
  return http.get<ResponseResult<Reminder[]>,number>(`/reminders/entity/${entityId}`);
};

/**
 * 获取特定状态的提醒
 * @param userId 用户ID
 * @param status 状态
 */
export const getRemindersByStatus = (userId: string, status: string) => {
  return http.get<ResponseResult<Reminder[]>,object>("/reminders/status", {
    params: { userId, status }
  });
};

/**
 * 为物品生成提醒
 * @param itemId 物品ID
 */
export const generateRemindersForItem = (itemId: number) => {
  return http.post<ResponseResult<void>,number>(`/reminders/generate/${itemId}`, {});
};
