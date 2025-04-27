<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { noticesData } from "./data";
import NoticeList from "./components/NoticeList.vue";
import BellIcon from "@iconify-icons/ep/bell";
import { getRecentReminders } from "@/api/dashboard";
import type { Reminder } from "@/types/reminder";
import { formatDate } from "@/utils/date";

const noticesNum = ref(0);
const notices = ref(noticesData);
const activeKey = ref(noticesData[0]?.key);
const recentReminders = ref<Reminder[]>([]);
const loadingReminders = ref(false);

// 获取最近提醒
const fetchRecentReminders = async () => {
  try {
    loadingReminders.value = true;
    const response = await getRecentReminders(5);
    if (response.code === 200 && response.data) {
      recentReminders.value = response.data;
      
      // 将提醒转换为通知格式
      const reminderNotices = recentReminders.value.map(reminder => ({
        avatar: "",
        title: reminder.entityName || "提醒",
        description: reminder.content,
        datetime: formatDate(reminder.remindDate),
        type: "reminder",
        status: getReminderStatusType(reminder.status)
      }));
      
      // 更新提醒通知
      const reminderIndex = notices.value.findIndex(item => item.key === "1");
      if (reminderIndex !== -1) {
        notices.value[reminderIndex].list = reminderNotices;
      }
      
      // 更新通知总数
      updateNoticeCount();
    }
  } catch (error) {
    console.error("获取最近提醒失败:", error);
  } finally {
    loadingReminders.value = false;
  }
};

// 将提醒状态转换为通知状态类型
const getReminderStatusType = (status: string): "primary" | "success" | "warning" | "info" | "danger" => {
  const statusMap = {
    pending: "warning",
    sent: "info",
    processed: "success",
    ignored: "danger"
  };
  return statusMap[status] || "info";
};

// 更新通知总数
const updateNoticeCount = () => {
  noticesNum.value = 0;
  notices.value.forEach(v => {
    noticesNum.value += v.list.length;
  });
};

onMounted(() => {
  fetchRecentReminders();
});

const getLabel = computed(
  () => item =>
    item.name + (item.list.length > 0 ? `(${item.list.length})` : "")
);
</script>

<template>
  <el-dropdown trigger="click" placement="bottom-end">
    <span
      :class="[
        'dropdown-badge',
        'navbar-bg-hover',
        'select-none',
        Number(noticesNum) !== 0 && 'mr-[10px]'
      ]"
    >
      <el-badge :value="Number(noticesNum) === 0 ? '' : noticesNum" :max="99">
        <span class="header-notice-icon">
          <IconifyIconOffline :icon="BellIcon" />
        </span>
      </el-badge>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-tabs
          v-model="activeKey"
          :stretch="true"
          class="dropdown-tabs"
          :style="{ width: notices.length === 0 ? '200px' : '330px' }"
        >
          <el-empty
            v-if="notices.length === 0"
            description="暂无消息"
            :image-size="60"
          />
          <span v-else>
            <template v-for="item in notices" :key="item.key">
              <el-tab-pane :label="getLabel(item)" :name="`${item.key}`">
                <el-scrollbar max-height="330px">
                  <div class="noticeList-container">
                    <div v-if="item.key === '1' && loadingReminders" class="loading-container">
                      <el-skeleton :rows="3" animated />
                    </div>
                    <NoticeList :list="item.list" :emptyText="item.emptyText" />
                  </div>
                </el-scrollbar>
              </el-tab-pane>
            </template>
          </span>
        </el-tabs>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
.dropdown-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 48px;
  cursor: pointer;

  .header-notice-icon {
    font-size: 18px;
  }
}

.dropdown-tabs {
  .noticeList-container {
    padding: 15px 24px 0;
  }

  .loading-container {
    padding: 0 0 15px 0;
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap)::after {
    height: 1px;
  }

  :deep(.el-tabs__nav-wrap) {
    padding: 0 36px;
  }
}
</style>
