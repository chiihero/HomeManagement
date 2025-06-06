<template>
  <div class="">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <el-card class="mb-4" shadow="hover">
          <div class="flex justify-between items-center">
            <div>
              <h2 class="m-0">
                欢迎回来，{{ userInfo.nickname || userInfo.username }}
              </h2>
              <p class="mt-2 mb-0 text-gray-500">
                今天是 {{ currentDate }}，{{ welcomeMessage }}
              </p>
            </div>
            <el-button
              type="primary"
              :icon="useRenderIcon(Refresh)"
              circle
              @click="refreshData"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon bg-primary">
              <el-icon><IconifyIconOffline :icon="Box" /></el-icon>
            </div>
            <div class="card-data">
              <div class="data-title">物品总数</div>
              <div class="data-value">{{ statistics.totalItems }}</div>
              <div class="data-info">
                其中 {{ statistics.availableItems }} 个可用
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon bg-primary">
              <el-icon><IconifyIconOffline :icon="CircleClose" /></el-icon>
            </div>
            <div class="card-data">
              <div class="data-title">即将过期</div>
              <div class="data-value">{{ statistics.expiringItems }}</div>
              <div class="data-info">
                {{ statistics.expiredItems }} 个已过期
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon bg-primary">
              <el-icon><IconifyIconOffline :icon="Wallet" /></el-icon>
            </div>
            <div class="card-data">
              <div class="data-title">总价值</div>
              <div class="data-value">
                ¥{{ formatNumber(statistics.totalValue) }}
              </div>
              <div class="data-info">
                {{ statistics.categoriesCount }} 个分类
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :md="24" :lg="16">
        <el-card class="h-full" shadow="hover">
          <template #header>
            <div class="flex justify-between items-center">
              <span>物品分类统计</span>
              <el-radio-group v-model="chartViewType" size="small">
                <el-radio-button value="category">物品分类</el-radio-button>
                <el-radio-button value="status">状态分布</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <component
              :is="currentChartComponent"
              :chart-data="currentChartData"
              :height="300"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="24" :lg="8">
        <el-card class="h-full" shadow="hover">
          <template #header>
            <div class="flex justify-between items-center">
              <span>最近提醒</span>
              <el-button type="primary" link @click="navigateToReminders"
                >查看全部</el-button
              >
            </div>
          </template>
          <el-skeleton v-if="loading.reminders" :rows="3" animated />
          <div v-else>
            <el-empty
              v-if="recentReminders.length === 0"
              description="暂无提醒"
            />
            <ul v-else class="reminder-list">
              <li
                v-for="(reminder, index) in recentReminders"
                :key="reminder.id"
                class="reminder-item"
              >
                <el-tag
                  size="small"
                  :type="getReminderTypeColor(reminder.type)"
                >
                  {{ getReminderTypeText(reminder.type) }}
                </el-tag>
                <div class="reminder-info">
                  <div class="reminder-title">{{ reminder.itemName }}</div>
                  <div class="reminder-time">
                    提醒日期: {{ formatDate(reminder.reminderDate) }}
                    <el-tag
                      size="small"
                      :type="getReminderStatusColor(reminder.status)"
                      class="ml-2"
                    >
                      {{ getReminderStatusText(reminder.status) }}
                    </el-tag>
                  </div>
                  <div class="reminder-content">{{ reminder.content }}</div>
                </div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="24">
        <el-card shadow="hover">
          <template #header>
            <div class="flex justify-between items-center">
              <span>最近添加的物品</span>
              <el-button type="primary" link @click="navigateToEntities"
                >查看全部</el-button
              >
            </div>
          </template>
          <el-skeleton v-if="loading.recentItems" :rows="5" animated />
          <div v-else>
            <el-empty
              v-if="recentItems.length === 0"
              description="暂无物品记录"
            />
            <div v-else class="table-responsive">
              <el-table :data="recentItems" style="width: 100%">
                <el-table-column prop="name" label="物品名称" min-width="120" />
                <el-table-column prop="type" label="类型" width="120" />
                <el-table-column prop="price" label="价格" width="100">
                  <template #default="scope">
                    ¥{{ formatNumber(scope.row.price) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ getStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import Box from "@iconify-icons/ep/box";
import Wallet from "@iconify-icons/ep/wallet";
import CircleClose from "@iconify-icons/ep/circle-close";
import Refresh from "@iconify-icons/ep/refresh";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";

import dayjs from "dayjs";
import Pie from "@/components/charts/Pie.vue";
import Bar from "@/components/charts/Bar.vue";
import {
  getDashboardStatistics,
  getRecentReminders,
  getRecentEntities
} from "@/api/dashboard";
import { useUserStoreHook } from "@/store/modules/user";
import type { Entity } from "@/types/entity";
import type { Reminder } from "@/types/reminder";
import { formatDate } from "@/utils/date";

const router = useRouter();
const userStore = useUserStoreHook();
const userInfo = computed(
  () => userStore.userId || { nickname: "", username: "" }
);

// 当前日期和欢迎语
const currentDate = computed(() => dayjs().format("YYYY年MM月DD日"));
const welcomeMessage = computed(() => {
  const hour = dayjs().hour();
  if (hour < 6) {
    return "夜深了，注意休息！";
  } else if (hour < 9) {
    return "早上好，祝你有个美好的一天！";
  } else if (hour < 12) {
    return "上午好，工作顺利！";
  } else if (hour < 14) {
    return "中午好，记得午休哦！";
  } else if (hour < 18) {
    return "下午好，工作效率高！";
  } else if (hour < 22) {
    return "晚上好，辛苦了！";
  } else {
    return "夜深了，注意休息！";
  }
});

// 统计数据
const statistics = reactive({
  totalItems: 0,
  availableItems: 0,
  expiringItems: 0,
  expiredItems: 0,
  totalValue: 0,
  categoriesCount: 0
});

// 图表相关
const chartViewType = ref("category");
const categoryChartData = ref({
  labels: [] as string[],
  datasets: [
    {
      backgroundColor: [] as string[],
      data: [] as number[]
    }
  ]
});
const statusChartData = ref({
  labels: [] as string[],
  datasets: [
    {
      backgroundColor: [] as string[],
      data: [] as number[]
    }
  ]
});

const currentChartData = computed(() => {
  return chartViewType.value === "category"
    ? categoryChartData.value
    : statusChartData.value;
});

const currentChartComponent = computed(() => {
  return chartViewType.value === "category" ? Pie : Bar;
});

// 提醒、物品和借出记录
const recentReminders = ref<Reminder[]>([]);
const recentItems = ref<Entity[]>([]);

// 加载状态
const loading = reactive({
  statistics: true,
  reminders: true,
  recentItems: true
});

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchStatistics(),
    fetchRecentReminders(),
    fetchRecentItems()
  ]);
});

// 刷新数据
const refreshData = async () => {
  loading.statistics = true;
  loading.reminders = true;
  loading.recentItems = true;

  await Promise.all([
    fetchStatistics(),
    fetchRecentReminders(),
    fetchRecentItems()
  ]);
};

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await getDashboardStatistics();
    if (response.code === 200 && response.data) {
      const data = response.data;
      statistics.totalItems = data.statistics.totalItems;
      statistics.availableItems = data.statistics.availableItems;
      statistics.expiringItems = data.statistics.expiringItems;
      statistics.expiredItems = data.statistics.expiredItems;
      statistics.totalValue = data.statistics.totalValue;
      statistics.categoriesCount = data.statistics.categoriesCount;


      // 更新分类图表数据
      if (
        data.categoryDistribution &&
        Array.isArray(data.categoryDistribution)
      ) {
        categoryChartData.value = {
          labels: data.categoryDistribution.map(item => item.name),
          datasets: [
            {
              backgroundColor: data.categoryDistribution.map(
                item => item.color
              ),
              data: data.categoryDistribution.map(item => item.count)
            }
          ]
        };
      } else {
        categoryChartData.value = {
          labels: [],
          datasets: [{ backgroundColor: [], data: [] }]
        };
      }

      // 更新状态图表数据
      if (data.statusDistribution && Array.isArray(data.statusDistribution)) {
        statusChartData.value = {
          labels: data.statusDistribution.map(item => item.name),
          datasets: [
            {
              backgroundColor: data.statusDistribution.map(item => item.color),
              data: data.statusDistribution.map(item => item.count)
            }
          ]
        };
      } else {
        statusChartData.value = {
          labels: [],
          datasets: [{ backgroundColor: [], data: [] }]
        };
      }
    }
  } catch (error) {
    console.error("Failed to fetch statistics:", error);
  } finally {
    loading.statistics = false;
  }
};

// 获取最近提醒
const fetchRecentReminders = async () => {
  try {
    const response = await getRecentReminders(
      5);
    if (response.code === 200 && response.data.length > 0) {
      recentReminders.value = response.data;
    }
  } catch (error) {
    console.error("Failed to fetch recent reminders:", error);
  } finally {
    loading.reminders = false;
  }
};

// 获取最近物品
const fetchRecentItems = async () => {
  try {
    const response = await getRecentEntities(5);
    if (response?.code === 200 && response?.data) {
      recentItems.value = response.data;
    }
  } catch (error) {
    console.error("Failed to fetch recent items:", error);
  } finally {
    loading.recentItems = false;
  }
};

// 工具函数
const formatNumber = (num: number) => {
  if (num === undefined || num === 0) {
    return "0.00";
  }
  return num.toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};


const getReminderTypeColor = (type: string) => {
  const colors = {
    EXPIRATION: "danger",
    MAINTENANCE: "warning",
    OTHER: "info"
  };
  return colors[type] || "info";
};

const getReminderTypeText = (type: string) => {
  const texts = {
    EXPIRATION: "过期提醒",
    MAINTENANCE: "维护提醒",
    OTHER: "其他提醒"
  };
  return texts[type] || "其他提醒";
};

const getReminderStatusColor = (status: string) => {
  const colors = {
    PENDING: "warning",
    NOTIFIED: "info",
    COMPLETED: "success",
    EXPIRED: "danger"
  };
  return colors[status] || "info";
};

const getReminderStatusText = (status: string) => {
  const texts = {
    PENDING: "待处理",
    NOTIFIED: "已通知",
    COMPLETED: "已完成",
    EXPIRED: "已过期"
  };
  return texts[status] || "未知状态";
};

const getStatusType = (status: string) => {
  const types = {
    normal: "success",
    damaged: "primary",
    discarded: "warning",
    expired: "info"
  };
  return types[status] || "info";
};

const getStatusText = (status: string) => {
  const texts = {
    normal: "正常",
    damaged: "损坏",
    discarded: "丢弃",
    expired: "过期",
    lent: "借出"
  };
  return texts[status] || "未知状态";
};

// 导航函数
const navigateToReminders = () => {
  router.push("/reminder");
};

const navigateToEntities = () => {
  router.push("/entity");
};

const viewEntityDetail = (entity: Entity) => {
  router.push(`/entity/detail/${entity.id}`);
};
</script>

<style scoped>


.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.card-icon .el-icon {
  font-size: 30px;
  color: white;
}

.card-data {
  flex: 1;
}

.data-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.data-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.data-info {
  font-size: 12px;
  color: #999;
}

.reminder-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.reminder-item {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-info {
  margin-left: 10px;
}

.reminder-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.reminder-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.reminder-content {
  font-size: 14px;
  color: #333;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
