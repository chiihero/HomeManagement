import request from "@/utils/request";
import type {
  Reminder,
  ReminderFormData,
  ReminderQueryParams,
  ItemOption
} from "@/types/reminder";
import { useUserStoreHook } from "@/store/modules/user";

/**
 * 获取提醒列表
 * @param params 查询参数
 */
export function fetchReminders(params: ReminderQueryParams) {
  return request<{ list: Reminder[]; total: number }>({
    url: "/reminders",
    method: "get",
    params
  });
}

/**
 * 获取提醒详情
 * @param id 提醒ID
 */
export function fetchReminderDetail(id: number) {
  return request<Reminder>({
    url: `/reminders/${id}`,
    method: "get"
  });
}

/**
 * 创建提醒
 * @param data 提醒信息
 */
export function createReminder(data: ReminderFormData) {
  return request<Reminder>({
    url: "/reminders",
    method: "post",
    data
  });
}

/**
 * 更新提醒
 * @param id 提醒ID
 * @param data 提醒信息
 */
export function updateReminder(id: number, data: ReminderFormData) {
  return request<Reminder>({
    url: `/reminders/${id}`,
    method: "put",
    data
  });
}

/**
 * 删除提醒
 * @param id 提醒ID
 */
export function deleteReminder(id: number) {
  return request({
    url: `/reminders/${id}`,
    method: "delete"
  });
}

/**
 * 完成提醒
 * @param id 提醒ID
 */
export function completeReminder(id: number) {
  return request<Reminder>({
    url: `/reminders/${id}/complete`,
    method: "put"
  });
}

/**
 * 搜索物品
 * @param keyword 关键词
 */
export function searchItems(keyword: string) {
  return request<ItemOption[]>({
    url: "/items/search",
    method: "get",
    params: { keyword }
  });
}

/**
 * 搜索提醒
 * @param keyword 关键词
 */
export function searchReminders(keyword: string) {
  return request<Reminder[]>({
    url: "/reminders/search",
    method: "get",
    params: { keyword }
  });
}

/**
 * 获取即将到期的提醒
 * @param days 天数
 */
export function fetchUpcomingReminders(days: number = 7) {
  return request<Reminder[]>({
    url: "/reminders/upcoming",
    method: "get",
    params: { days }
  });
}

/**
 * 获取过期的提醒
 */
export function fetchExpiredReminders() {
  return request<Reminder[]>({
    url: "/reminders/expired",
    method: "get"
  });
}

/**
 * 批量更新提醒状态
 * @param ids 提醒ID数组
 * @param status 状态
 */
export function updateBatchReminderStatus(ids: number[], status: string) {
  return request<void>({
    url: "/reminders/batch/status",
    method: "patch",
    data: { ids, status }
  });
}

/**
 * 批量删除提醒
 * @param ids 提醒ID数组
 */
export function deleteBatchReminders(ids: number[]) {
  return request<void>({
    url: "/reminders/batch",
    method: "delete",
    data: { ids }
  });
}

/**
 * 获取今日提醒
 * @param userId 用户ID
 */
export const getTodayReminders = (
  userId: number
): Promise<ResponseResult<Reminder[]>> => {
  return request<ResponseResult<Reminder[]>>({
    url: "/reminders/today",
    method: "get",
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
): Promise<ResponseResult<Reminder[]>> => {
  return request<ResponseResult<Reminder[]>>({
    url: "/reminders/date-range",
    method: "get",
    params: { userId, startDate, endDate }
  });
};

/**
 * 获取物品的提醒
 * @param entityId 物品ID
 */
export const getRemindersByEntityId = (
  entityId: string
): Promise<ResponseResult<Reminder[]>> => {
  return request<ResponseResult<Reminder[]>>({
    url: `/reminders/entity/${entityId}`,
    method: "get"
  });
};

/**
 * 获取特定状态的提醒
 * @param userId 用户ID
 * @param status 状态
 */
export const getRemindersByStatus = (
  userId: number,
  status: string
): Promise<ResponseResult<Reminder[]>> => {
  return request<ResponseResult<Reminder[]>>({
    url: "/reminders/status",
    method: "get",
    params: { userId, status }
  });
};

/**
 * 为物品生成提醒
 * @param itemId 物品ID
 */
export const generateRemindersForItem = (
  itemId: string
): Promise<ResponseResult<void>> => {
  return request<ResponseResult<void>>({
    url: `/reminders/generate/${itemId}`,
    method: "post",
    data: {}
  });
};
