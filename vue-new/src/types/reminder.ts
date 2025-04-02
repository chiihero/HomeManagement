import type { PageParams } from './common'

/**
 * 提醒类型枚举
 */
export type ReminderType = 'EXPIRATION' | 'MAINTENANCE' | 'OTHER'

/**
 * 提醒状态枚举
 */
export type ReminderStatus = 'PENDING' | 'NOTIFIED' | 'COMPLETED' | 'EXPIRED' 

/**
 * 提醒周期枚举
 */
export type ReminderCycle = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'

/**
 * 通知方式枚举
 */
export type NotificationMethod = 'EMAIL' | 'SYSTEM' | 'SMS'

/**
 * 提醒实体
 */
export interface Reminder {
  /**
   * 提醒ID
   */
  id: string
  /**
   * 物品ID
   */
  itemId: string
  /**
   * 物品名称
   */
  itemName: string
  /**
   * 提醒类型
   */
  type: ReminderType
  /**
   * 提醒日期
   */
  reminderDate: string
  /**
   * 提前提醒天数
   */
  daysInAdvance: number
  /**
   * 提醒内容
   */
  content: string
  /**
   * 提醒状态
   */
  status: ReminderStatus
  /**
   * 通知方式
   */
  notificationMethods: NotificationMethod[]
  /**
   * 是否重复提醒
   */
  isRecurring: boolean
  /**
   * 重复周期
   */
  recurringCycle?: ReminderCycle
  /**
   * 创建时间
   */
  createdTime: string
  /**
   * 更新时间
   */
  updatedTime: string
}

/**
 * 提醒查询参数
 */
export interface ReminderQueryParams extends PageParams {
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
   * 开始日期
   */
  startDate?: string
  /**
   * 结束日期
   */
  endDate?: string
  /**
   * 日期范围
   */
  dateRange?: string[]
} 