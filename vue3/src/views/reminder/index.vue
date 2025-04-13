<template>
  <div class="bg-gray-50 min-h-screen p-4 md:p-6">
    <!-- 头部 -->
    <el-card class="mb-6 border-0 shadow-sm">
      <div
        class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
      >
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0">
          <el-icon class="mr-2 text-primary"><AlarmClock /></el-icon
          >提醒事项管理
        </h1>
        <el-button
          type="primary"
          class="flex items-center gap-2"
          @click="openAddForm"
        >
          <el-icon><Plus /></el-icon>添加提醒
        </el-button>
      </div>
    </el-card>

    <!-- 搜索表单 -->
    <el-card class="mb-6 border-0 shadow-sm">
      <template #header>
        <div class="flex items-center">
          <span class="text-gray-700 font-medium">搜索条件</span>
        </div>
      </template>
      <el-form :model="searchForm" inline @submit.prevent>
        <div class="flex flex-wrap gap-4">
          <el-form-item label="物品名称">
            <el-input
              v-model="searchForm.entityName"
              placeholder="请输入物品名称"
              clearable
            />
          </el-form-item>
          <el-form-item label="提醒类型">
            <el-select
              v-model="searchForm.type"
              placeholder="请选择提醒类型"
              clearable
            >
              <el-option
                v-for="type in reminderTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
            >
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
          <div class="flex items-center ml-auto">
            <el-button
              type="primary"
              class="mr-2"
              @click="loadReminders(searchForm)"
            >
              <el-icon class="mr-1"><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetSearchForm">
              <el-icon class="mr-1"><Refresh /></el-icon>重置
            </el-button>
          </div>
        </div>
      </el-form>
    </el-card>

    <!-- 提醒列表 -->
    <el-card class="border-0 shadow-sm">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-gray-700 font-medium">提醒列表</span>
          <div class="text-sm text-gray-500">共 {{ total }} 项</div>
        </div>
      </template>
      <el-table v-loading="loading" :data="reminderList" border class="w-full">
        <el-table-column prop="entityName" label="物品名称" min-width="120" />
        <el-table-column prop="type" label="提醒类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeColor(row.type)">
              {{ getReminderTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remindDate" label="提醒日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="提醒内容"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="notificationMethods"
          label="通知方式"
          width="120"
        >
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag
                v-for="method in row.notificationMethods"
                :key="method"
                size="small"
                effect="plain"
              >
                {{ getNotificationMethodLabel(method) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              link
              size="small"
              @click="openEditForm(row.id)"
            >
              <el-icon class="mr-1"><Edit /></el-icon>编辑
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              link
              size="small"
              @click="handleComplete(row.id)"
            >
              <el-icon class="mr-1"><Check /></el-icon>完成
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(row.id)"
            >
              <el-icon class="mr-1"><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isAdding ? '添加提醒' : '编辑提醒'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="reminderFormRef"
        :model="reminderForm"
        :rules="rules"
        label-width="100px"
        status-icon
        class="max-w-3xl mx-auto"
      >
        <el-form-item label="物品" prop="entityId">
          <el-select
            v-model="reminderForm.entityId"
            filterable
            remote
            :remote-method="searchItems"
            :loading="itemLoading"
            placeholder="请选择物品"
            class="w-full"
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
          <el-select
            v-model="reminderForm.type"
            placeholder="请选择提醒类型"
            class="w-full"
          >
            <el-option
              v-for="type in reminderTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒日期" prop="remindDate">
          <el-date-picker
            v-model="reminderForm.remindDate"
            type="date"
            placeholder="请选择提醒日期"
            value-format="YYYY-MM-DD"
            class="w-full"
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
            <div class="flex flex-wrap gap-3">
              <el-checkbox
                v-for="method in notificationMethods"
                :key="method.value"
                :value="method.value"
              >
                {{ method.label }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="提前提醒" prop="daysInAdvance">
          <div class="flex items-center">
            <el-input-number
              v-model="reminderForm.daysInAdvance"
              :min="1"
              :max="30"
              class="w-32"
            />
            <span class="ml-2 text-gray-600">天</span>
          </div>
        </el-form-item>
        <el-form-item label="重复提醒">
          <div class="flex items-center gap-2">
            <el-switch v-model="reminderForm.isRecurring" />
            <el-select
              v-if="reminderForm.isRecurring"
              v-model="reminderForm.recurringCycle"
              placeholder="请选择重复周期"
              class="w-40"
            >
              <el-option
                v-for="cycle in recurringCycles"
                :key="cycle.value"
                :label="cycle.label"
                :value="cycle.value"
              />
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useReminderCRUD } from "./composables/useReminderCRUD";
import { useReminderForm } from "./composables/useReminderForm";
import { useReminderSearch } from "./composables/useReminderSearch";
import {
  ReminderType,
  ReminderStatus,
  NotificationMethod,
  RecurringCycle,
  Reminder
} from "@/types/reminder";
import { searchEntities } from "@/api/entity";
import { Entity } from "@/types/entity";
import { useUserStoreHook } from "@/store/modules/user";

import {
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Check,
  AlarmClock
} from "@element-plus/icons-vue";

defineOptions({
  name: "Reminder"
});
const authStore = useUserStoreHook();

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
} = useReminderCRUD();

const {
  reminderFormRef,
  reminderForm,
  rules,
  resetForm,
  fillFormWithReminder,
  validateForm
} = useReminderForm();

const {
  searchForm,
  resetSearchForm,
  handlePageChange,
  handleSizeChange,
  handleDateRangeChange
} = useReminderSearch();

// 提醒类型选项
const reminderTypes = [
  { label: "保修到期", value: "warranty" },
  { label: "维护提醒", value: "maintenance" },
  { label: "借出归还", value: "lending" },
  { label: "物品过期", value: "expiry" },
  { label: "其他提醒", value: "other" }
];

// 提醒状态选项
const reminderStatuses = [
  { label: "待提醒", value: "pending" },
  { label: "已发送", value: "sent" },
  { label: "已处理", value: "processed" },
  { label: "已忽略", value: "ignored" }
];

// 通知方式选项
const notificationMethods = [
  { label: "系统通知", value: "system" },
  { label: "邮件通知", value: "email" },
  { label: "短信通知", value: "sms" }
];

// 重复周期选项
const recurringCycles = [
  { label: "每天", value: "daily" },
  { label: "每周", value: "weekly" },
  { label: "每月", value: "monthly" },
  { label: "每年", value: "yearly" }
];

// 物品选项
const itemOptions = ref<Entity[]>([]);
const itemLoading = ref(false);

// 搜索物品
const searchItems = async (query: string) => {
  if (query) {
    itemLoading.value = true;
    try {
      const data = await searchEntities(authStore.userId, query);
      itemOptions.value = data.data;
    } catch (error) {
      console.error("搜索物品失败:", error);
    } finally {
      itemLoading.value = false;
    }
  } else {
    itemOptions.value = [];
  }
};

// 获取提醒类型标签
const getReminderTypeLabel = (type: ReminderType) => {
  return reminderTypes.find(item => item.value === type)?.label || type;
};

// 获取状态标签
const getStatusLabel = (status: ReminderStatus) => {
  return reminderStatuses.find(item => item.value === status)?.label || status;
};

// 获取状态类型
const getStatusType = (status: ReminderStatus) => {
  switch (status) {
    case "pending":
      return "warning";
    case "sent":
      return "success";
    case "processed":
      return "info";
    case "ignored":
      return "info";
    default:
      return "";
  }
};

// 获取提醒类型颜色
const getTypeColor = (type: ReminderType) => {
  switch (type) {
    case "warranty":
      return "danger";
    case "maintenance":
      return "warning";
    case "lending":
      return "primary";
    case "expiry":
      return "danger";
    case "other":
      return "info";
    default:
      return "info";
  }
};

// 获取通知方式标签
const getNotificationMethodLabel = (method: NotificationMethod) => {
  return (
    notificationMethods.find(item => item.value === method)?.label || method
  );
};

// 保存提醒
const handleSave = async () => {
  if (await validateForm()) {
    reminderForm.userId = authStore.userId;
    await saveReminder(reminderForm);
  }
};

// 对话框显示状态
const dialogVisible = computed<boolean>({
  get: () => isAdding.value || isEditing.value,
  set: value => {
    if (!value) {
      cancelEdit();
    }
  }
});

// 初始化
onMounted(() => {
  loadReminders(searchForm);
});
</script>
