<template>
  <div class="reminder-container">
    <!-- 头部 -->
    <div class="header">
      <h2>提醒事项管理</h2>
      <el-button type="primary" @click="openAddForm">添加提醒</el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :model="searchForm" inline class="search-form">
      <el-form-item label="物品名称">
        <el-input v-model="searchForm.itemName" placeholder="请输入物品名称" clearable />
      </el-form-item>
      <el-form-item label="提醒类型">
        <el-select v-model="searchForm.type" placeholder="请选择提醒类型" clearable>
          <el-option
            v-for="type in reminderTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="status in reminderStatuses"
            :key="status.value"
            :label="status.label"
            :value="status.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="提醒日期">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadReminders(searchForm)">搜索</el-button>
        <el-button @click="resetSearchForm">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 提醒列表 -->
    <el-table
      v-loading="loading"
      :data="reminderList"
      border
      style="width: 100%"
    >
      <el-table-column prop="itemName" label="物品名称" min-width="120" />
      <el-table-column prop="type" label="提醒类型" width="100">
        <template #default="{ row }">
          {{ getReminderTypeLabel(row.type) }}
        </template>
      </el-table-column>
      <el-table-column prop="reminderDate" label="提醒日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="提醒内容" min-width="200" />
      <el-table-column prop="notificationMethods" label="通知方式" width="120">
        <template #default="{ row }">
          <el-tag
            v-for="method in row.notificationMethods"
            :key="method"
            size="small"
            class="mr-1"
          >
            {{ getNotificationMethodLabel(method) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            link
            @click="openEditForm(row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            type="success"
            link
            @click="handleComplete(row.id)"
          >
            完成
          </el-button>
          <el-button
            type="danger"
            link
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isAdding ? '添加提醒' : '编辑提醒'"
      width="600px"
    >
      <el-form
        ref="reminderFormRef"
        :model="reminderForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="物品" prop="itemId">
          <el-select
            v-model="reminderForm.itemId"
            filterable
            remote
            :remote-method="searchItems"
            :loading="itemLoading"
            placeholder="请选择物品"
            style="width: 100%"
          >
            <el-option
              v-for="item in itemOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒类型" prop="type">
          <el-select v-model="reminderForm.type" placeholder="请选择提醒类型">
            <el-option
              v-for="type in reminderTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒日期" prop="reminderDate">
          <el-date-picker
            v-model="reminderForm.reminderDate"
            type="date"
            placeholder="请选择提醒日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="reminderForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入提醒内容"
          />
        </el-form-item>
        <el-form-item label="通知方式" prop="notificationMethods">
          <el-checkbox-group v-model="reminderForm.notificationMethods">
            <el-checkbox
              v-for="method in notificationMethods"
              :key="method.value"
              :label="method.value"
            >
              {{ method.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="提前提醒" prop="daysInAdvance">
          <el-input-number
            v-model="reminderForm.daysInAdvance"
            :min="1"
            :max="30"
          />
          <span class="ml-2">天</span>
        </el-form-item>
        <el-form-item label="重复提醒">
          <el-switch v-model="reminderForm.isRecurring" />
          <el-select
            v-if="reminderForm.isRecurring"
            v-model="reminderForm.recurringCycle"
            class="ml-2"
            placeholder="请选择重复周期"
          >
            <el-option
              v-for="cycle in recurringCycles"
              :key="cycle.value"
              :label="cycle.label"
              :value="cycle.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useReminderCRUD } from './composables/useReminderCRUD'
import { useReminderForm } from './composables/useReminderForm'
import { useReminderSearch } from './composables/useReminderSearch'
import { ReminderType, ReminderStatus, NotificationMethod, RecurringCycle } from '@/types/reminder'
import { searchItems as searchItemsApi } from '@/api/reminder'

defineOptions({
  // name 作为一种规范最好必须写上并且和路由的name保持一致
  name: "Reminder"
});

// 使用组合式函数
const {
  loading,
  saving,
  reminderList,
  total,
  currentReminder,
  isEditing,
  isAdding,
  loadReminders,
  loadReminderDetail,
  openEditForm,
  openAddForm,
  cancelEdit,
  saveReminder,
  handleDelete,
  handleComplete
} = useReminderCRUD()

const {
  reminderFormRef,
  reminderForm,
  rules,
  resetForm,
  fillFormWithReminder,
  validateForm
} = useReminderForm()

const {
  searchForm,
  resetSearchForm,
  handlePageChange,
  handleSizeChange,
  handleDateRangeChange
} = useReminderSearch()

// 提醒类型选项
const reminderTypes = [
  { label: '到期提醒', value: 'EXPIRATION' },
  { label: '维护提醒', value: 'MAINTENANCE' },
  { label: '自定义提醒', value: 'CUSTOM' }
]

// 提醒状态选项
const reminderStatuses = [
  { label: '待提醒', value: 'PENDING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

// 通知方式选项
const notificationMethods = [
  { label: '系统通知', value: 'SYSTEM' },
  { label: '邮件通知', value: 'EMAIL' },
  { label: '短信通知', value: 'SMS' }
]

// 重复周期选项
const recurringCycles = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' },
  { label: '每年', value: 'YEARLY' }
]

// 物品选项
const itemOptions = ref<Array<{ id: number; name: string }>>([])
const itemLoading = ref(false)

// 搜索物品
const searchItems = async (query: string) => {
  if (query) {
    itemLoading.value = true
    try {
      const data = await searchItemsApi(query)
      itemOptions.value = data
    } catch (error) {
      console.error('搜索物品失败:', error)
    } finally {
      itemLoading.value = false
    }
  } else {
    itemOptions.value = []
  }
}

// 获取提醒类型标签
const getReminderTypeLabel = (type: ReminderType) => {
  return reminderTypes.find(item => item.value === type)?.label || type
}

// 获取状态标签
const getStatusLabel = (status: ReminderStatus) => {
  return reminderStatuses.find(item => item.value === status)?.label || status
}

// 获取状态类型
const getStatusType = (status: ReminderStatus) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'info'
    default:
      return ''
  }
}

// 获取通知方式标签
const getNotificationMethodLabel = (method: NotificationMethod) => {
  return notificationMethods.find(item => item.value === method)?.label || method
}

// 保存提醒
const handleSave = async () => {
  if (await validateForm()) {
    await saveReminder(reminderForm)
  }
}

// 对话框显示状态
const dialogVisible = computed<boolean>({
  get: () => isAdding.value || isEditing.value,
  set: (value) => {
    if (!value) {
      cancelEdit()
    }
  }
})

// 初始化
onMounted(() => {
  loadReminders(searchForm)
})
</script>

<style scoped>
.reminder-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mr-1 {
  margin-right: 4px;
}

.ml-2 {
  margin-left: 8px;
}
</style> 