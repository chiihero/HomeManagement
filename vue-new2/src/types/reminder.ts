import type { PageParams } from '@/types/entity'

/**
 * 提醒类型
 */
export type ReminderType = 'EXPIRATION' | 'MAINTENANCE' | 'CUSTOM'

/**
 * 提醒状态
 */
export type ReminderStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED'

/**
 * 通知方式
 */
export type NotificationMethod = 'SYSTEM' | 'EMAIL' | 'SMS'

/**
 * 重复周期
 */
export type RecurringCycle = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'

/**
 * 提醒实体
 */
export interface Reminder {
  /**
   * 提醒ID
   */
  id: number
  /**
   * 物品ID
   */
  itemId: number
  /**
   * 物品名称
   */
  itemName: string
  /**
   * 用户ID
   */
  userId: number
  /**
   * 提醒类型
   */
  type: ReminderType
  /**
   * 提醒日期
   */
  reminderDate: string
  /**
   * 提醒状态
   */
  status: ReminderStatus
  /**
   * 提醒内容
   */
  content: string
  /**
   * 通知方式
   */
  notificationMethods: NotificationMethod[]
  /**
   * 提前提醒天数
   */
  daysInAdvance: number
  /**
   * 是否重复提醒
   */
  isRecurring: boolean
  /**
   * 重复周期
   */
  recurringCycle?: RecurringCycle
  /**
   * 创建时间
   */
  createdAt: string
  /**
   * 更新时间
   */
  updatedAt: string
}

/**
 * 提醒查询参数
 */
export interface ReminderQueryParams {
  /**
   * 物品名称
   */
  itemName?: string
  /**
   * 提醒类型
   */
  type?: ReminderType
  /**
   * 提醒状态
   */
  status?: ReminderStatus
  /**
   * 日期范围
   */
  dateRange?: [string, string]
  /**
   * 用户ID
   */
  userId?: number
  /**
   * 页码
   */
  page: number
  /**
   * 每页大小
   */
  size: number
}

/**
 * 提醒表单数据类型
 */
export interface ReminderFormData {
  id?: number
  itemId: number
  itemName: string
  userId: number
  type: ReminderType
  reminderDate: string
  status: ReminderStatus
  content: string
  notificationMethods: NotificationMethod[]
  daysInAdvance: number
  isRecurring: boolean
  recurringCycle?: RecurringCycle
}

/**
 * 物品选项
 */
export interface ItemOption {
  id: number
  name: string
} 