// import type { PageParams } from "@/types/entity";

/**
 * 提醒类型
 */
export type ReminderType = "warranty" | "maintenance" | "lending" | "expiry" | "other";

/**
 * 提醒状态
 */
export type ReminderStatus = "pending" | "sent" | "processed" | "ignored";

/**
 * 通知方式
 */
export type NotificationMethod = "system" | "email" | "sms" | "wx";

/**
 * 重复周期
 */
export type RecurringCycle = "daily" | "weekly" | "monthly" | "yearly";

/**
 * 提醒实体
 */
export interface Reminder {
  /**
   * 提醒ID
   */
  id: string;
  /**
   * 物品ID
   */
  entityId: string;
  /**
   * 物品名称
   */
  entityName?: string;
  /**
   * 用户ID
   */
  userId: string;
  /**
   * 提醒类型
   */
  type: ReminderType;
  /**
   * 提醒日期
   */
  remindDate: string;
  /**
   * 提醒状态
   */
  status: ReminderStatus;
  /**
   * 提醒内容
   */
  content: string;
  /**
   * 通知方式 (逗号分隔的字符串)
   */
  notificationMethods: string;
  /**
   * 提前提醒天数
   */
  daysInAdvance: number;
  /**
   * 是否重复提醒
   */
  isRecurring: boolean;
  /**
   * 重复周期
   */
  recurringCycle?: RecurringCycle;
  /**
   * 创建者ID
   */
  createUserId: number;
  /**
   * 创建时间
   */
  createTime: string;
  /**
   * 更新时间
   */
  updateTime: string;
}

/**
 * 提醒查询参数
 */
export interface ReminderQueryParams {
  /**
   * 物品名称
   */
  entityId?: number;
    /**
   * 物品名称
   */
  entityName: string;
  /**
   * 提醒类型
   */
  type?: ReminderType;
  /**
   * 提醒状态
   */
  status?: ReminderStatus;
  /**
   * 日期范围
   */
  dateRange?: [string, string];
  /**
   * 用户ID
   */
  userId?: number;
}

/**
 * 物品选项
 */
export interface ItemOption {
  id: number;
  name: string;
}
