import { defineStore } from 'pinia'
import type { Reminder, ReminderFormData, ReminderQueryParams } from '@/types/reminder'
import {
  getRemindersByDateRange,
  getReminderDetail,
  createReminder,
  updateReminder,
  deleteReminder,
  completeReminder
} from '@/api/reminder'

interface ReminderState {
  loading: boolean
  reminderList: Reminder[]
  total: number
  currentReminder: Reminder | null
}

export const useReminderStore = defineStore('reminder', {
  state: (): ReminderState => ({
    loading: false,
    reminderList: [],
    total: 0,
    currentReminder: null
  }),

  getters: {
    // 获取提醒列表
    getReminderList: (state) => state.reminderList,
    // 获取提醒总数
    getTotal: (state) => state.total,
    // 获取当前提醒
    getCurrentReminder: (state) => state.currentReminder
  },

  actions: {
    // 设置加载状态
    setLoading(loading: boolean) {
      this.loading = loading
    },

    // 设置当前提醒
    setCurrentReminder(reminder: Reminder | null) {
      this.currentReminder = reminder
    },

    // 获取提醒列表
    async fetchReminders(params: ReminderQueryParams) {
      this.setLoading(true)
      try {
        const res = await getRemindersByDateRange(params)
        this.reminderList = res.data
        this.total = res.total || 0
      } catch (error) {
        console.error('获取提醒列表失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 获取提醒详情
    async fetchReminderDetail(id: number) {
      this.setLoading(true)
      try {
        const res = await getReminderDetail(id)
        this.currentReminder = res.data
      } catch (error) {
        console.error('获取提醒详情失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 创建提醒
    async createReminder(data: ReminderFormData) {
      this.setLoading(true)
      try {
        const res = await createReminder(data)
        return res.data
      } catch (error) {
        console.error('创建提醒失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 更新提醒
    async updateReminder(id: number, data: ReminderFormData) {
      this.setLoading(true)
      try {
        const res = await updateReminder(id, data)
        if (this.currentReminder?.id === id) {
          this.currentReminder = res.data
        }
        return res.data
      } catch (error) {
        console.error('更新提醒失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 删除提醒
    async deleteReminder(id: number) {
      this.setLoading(true)
      try {
        await deleteReminder(id)
        if (this.currentReminder?.id === id) {
          this.currentReminder = null
        }
        this.reminderList = this.reminderList.filter(item => item.id !== id)
      } catch (error) {
        console.error('删除提醒失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 完成提醒
    async completeReminder(id: number) {
      this.setLoading(true)
      try {
        const res = await completeReminder(id)
        if (this.currentReminder?.id === id) {
          this.currentReminder = res.data
        }
        const index = this.reminderList.findIndex(item => item.id === id)
        if (index !== -1) {
          this.reminderList[index] = res.data
        }
        return res.data
      } catch (error) {
        console.error('完成提醒失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    }
  }
}) 