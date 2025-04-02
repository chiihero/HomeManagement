<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <div class="welcome-panel">
          <h2>欢迎回来，{{ userInfo.nickname || userInfo.username }}</h2>
          <p>今天是 {{ currentDate }}，{{ welcomeMessage }}</p>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="data-overview">
      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card class="overview-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background-color: #409EFF">
              <el-icon><Box /></el-icon>
            </div>
            <div class="card-data">
              <div class="data-title">物品总数</div>
              <div class="data-value">{{ statistics.totalItems }}</div>
              <div class="data-info">其中 {{ statistics.availableItems }} 个可用</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card class="overview-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background-color: #F56C6C">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="card-data">
              <div class="data-title">即将过期</div>
              <div class="data-value">{{ statistics.expiringItems }}</div>
              <div class="data-info">{{ statistics.expiredItems }} 个已过期</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card class="overview-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background-color: #67C23A">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="card-data">
              <div class="data-title">总价值</div>
              <div class="data-value">¥{{ formatNumber(statistics.totalValue) }}</div>
              <div class="data-info">{{ statistics.categoriesCount }} 个分类</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="main-content">
      <el-col :xs="24" :sm="24" :md="12" :lg="16" :xl="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>物品分类统计</span>
              <el-radio-group v-model="chartViewType" size="small">
                <el-radio-button label="category">物品分类</el-radio-button>
                <el-radio-button label="status">状态分布</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <component :is="currentChartComponent" 
              :chart-data="currentChartData" 
              :height="300"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card class="reminder-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近提醒</span>
              <el-button type="primary" link @click="navigateToReminders">查看全部</el-button>
            </div>
          </template>
          <el-skeleton :rows="3" animated v-if="loading.reminders" />
          <div v-else>
            <el-empty description="暂无提醒" v-if="recentReminders.length === 0" />
            <ul class="reminder-list" v-else>
              <li v-for="(reminder, index) in recentReminders" :key="reminder.id" class="reminder-item">
                <el-tag size="small" :type="getReminderTypeColor(reminder.type)">
                  {{ getReminderTypeText(reminder.type) }}
                </el-tag>
                <div class="reminder-info">
                  <div class="reminder-title">{{ reminder.itemName }}</div>
                  <div class="reminder-time">
                    提醒日期: {{ formatDate(reminder.reminderDate) }}
                    <el-tag size="small" :type="getReminderStatusColor(reminder.status)" style="margin-left: 5px">
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

    <el-row :gutter="20" class="main-content">
      <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <el-card class="recent-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近添加的物品</span>
              <el-button type="primary" link @click="navigateToEntities">查看全部</el-button>
            </div>
          </template>
          <el-skeleton :rows="5" animated v-if="loading.recentItems" />
          <div v-else>
            <el-empty description="暂无物品记录" v-if="recentItems.length === 0" />
            <el-table :data="recentItems" style="width: 100%" v-else>
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
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button type="primary" link @click="viewEntityDetail(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Box, Wallet, CircleClose, Document } from '@element-plus/icons-vue'
import moment from 'moment'
import Pie from '@/components/charts/Pie.vue'
import Bar from '@/components/charts/Bar.vue'
import { 
  getDashboardStatistics 
} from '@/api/dashboard'
import { getReminders } from '@/api/reminder'
import { getRecentEntities } from '@/api/entity'
import { useAuthStore } from '@/store/modules/auth'
import type { PageResult } from '@/types/common'
import type { Entity } from '@/types/entity'
import type { Reminder } from '@/types/reminder'

const router = useRouter()
const authStore = useAuthStore()
const userInfo = computed(() => authStore.currentUser || {})

// 当前日期和欢迎语
const currentDate = computed(() => moment().format('YYYY年MM月DD日'))
const welcomeMessage = computed(() => {
  const hour = moment().hour()
  if (hour < 6) {
    return '夜深了，注意休息！'
  } else if (hour < 9) {
    return '早上好，祝你有个美好的一天！'
  } else if (hour < 12) {
    return '上午好，工作顺利！'
  } else if (hour < 14) {
    return '中午好，记得午休哦！'
  } else if (hour < 18) {
    return '下午好，工作效率高！'
  } else if (hour < 22) {
    return '晚上好，辛苦了！'
  } else {
    return '夜深了，注意休息！'
  }
})

// 统计数据
const statistics = reactive({
  totalItems: 0,
  availableItems: 0,
  expiringItems: 0,
  expiredItems: 0,
  totalValue: 0,
  categoriesCount: 0
})

// 图表相关
const chartViewType = ref('category')
const categoryChartData = ref({
  labels: [] as string[],
  datasets: [
    {
      backgroundColor: [] as string[],
      data: [] as number[]
    }
  ]
})
const statusChartData = ref({
  labels: [] as string[],
  datasets: [
    {
      backgroundColor: [] as string[],
      data: [] as number[]
    }
  ]
})

const currentChartData = computed(() => {
  return chartViewType.value === 'category' ? categoryChartData.value : statusChartData.value
})

const currentChartComponent = computed(() => {
  return chartViewType.value === 'category' ? Pie : Bar
})

// 提醒、物品和借出记录
const recentReminders = ref<Reminder[]>([])
const recentItems = ref<Entity[]>([])

// 加载状态
const loading = reactive({
  statistics: true,
  reminders: true,
  recentItems: true
})

// 初始化
onMounted(async () => {
  await fetchStatistics()
  await Promise.all([
    fetchRecentReminders(),
    fetchRecentItems()
  ])
})

// 获取统计数据
const fetchStatistics = async () => {
  loading.statistics = true
  try {
    const response = await getDashboardStatistics()
    const data = response.data
    Object.assign(statistics, data.statistics)
    
    // 设置分类图表数据
    categoryChartData.value.labels = data.categoryData.map((item: any) => item.name)
    categoryChartData.value.datasets[0].data = data.categoryData.map((item: any) => item.count)
    categoryChartData.value.datasets[0].backgroundColor = generateColors(data.categoryData.length)
    
    // 设置状态图表数据
    statusChartData.value.labels = data.statusData.map((item: any) => getStatusText(item.status))
    statusChartData.value.datasets[0].data = data.statusData.map((item: any) => item.count)
    statusChartData.value.datasets[0].backgroundColor = [
      '#67C23A', // 正常
      '#E6A23C', // 闲置
      '#F56C6C', // 损坏
      '#909399', // 丢失
      '#409EFF'  // 维修中
    ]
  } catch (error) {
    console.error('获取统计数据失败', error)
  } finally {
    loading.statistics = false
  }
}

// 获取最近提醒
const fetchRecentReminders = async () => {
  loading.reminders = true
  try {
    const authStore = useAuthStore()
    const ownerId = authStore.currentUser?.id || 1
    // 正确调用getReminders API
    const response = await getReminders({ ownerId })
    // 直接使用返回的数据数组，并限制显示5条
    recentReminders.value = (response.data || []).slice(0, 5)
  } catch (error) {
    console.error('获取最近提醒失败', error)
  } finally {
    loading.reminders = false
  }
}

// 获取最近添加的物品
const fetchRecentItems = async () => {
  loading.recentItems = true
  try {
    const response = await getRecentEntities(5)
    recentItems.value = response.data
  } catch (error) {
    console.error('获取最近添加的物品失败', error)
  } finally {
    loading.recentItems = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchStatistics()
  fetchRecentReminders()
  fetchRecentItems()
}

// 导航
const navigateToReminders = () => {
  router.push('/reminder')
}

const navigateToEntities = () => {
  router.push('/entity')
}

const viewEntityDetail = (entity: Entity) => {
  router.push(`/entity/detail/${entity.id}`)
}

// 工具方法
const formatNumber = (num: number) => {
  return num ? num.toLocaleString() : '0'
}

const formatDate = (date: string) => {
  return date ? moment(date).format('YYYY-MM-DD') : '-'
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'NORMAL': return '正常'
    case 'DAMAGED': return '已损坏'
    case 'LENT': return '已借出'
    case 'DISABLED': return '已停用'
    case 'REPAIRING': return '维修中'
    default: return '未知'
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'NORMAL': return 'success'
    case 'DAMAGED': return 'danger'
    case 'LENT': return 'warning'
    case 'DISABLED': return 'info'
    case 'REPAIRING': return 'primary'
    default: return ''
  }
}

const getReminderTypeText = (type: string) => {
  switch (type) {
    case 'EXPIRATION': return '到期提醒'
    case 'MAINTENANCE': return '维护提醒'
    case 'RETURN': return '归还提醒'
    case 'OTHER': return '其他提醒'
    default: return '未知'
  }
}

const getReminderTypeColor = (type: string) => {
  switch (type) {
    case 'EXPIRATION': return 'danger'
    case 'MAINTENANCE': return 'warning'
    case 'RETURN': return 'info'
    case 'OTHER': return ''
    default: return ''
  }
}

const getReminderStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待提醒'
    case 'NOTIFIED': return '已提醒'
    case 'COMPLETED': return '已完成'
    case 'EXPIRED': return '已过期'
    default: return '未知'
  }
}

const getReminderStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'NOTIFIED': return 'info'
    case 'COMPLETED': return 'success'
    case 'EXPIRED': return 'danger'
    default: return ''
  }
}

// 生成随机颜色
const generateColors = (count: number) => {
  const colors = [
    '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
    '#36C5C8', '#B983FF', '#EC5863', '#FEA443', '#4C5A8C',
    '#FFB6C1', '#00BFFF', '#32CD32', '#FF8C00', '#9370DB'
  ]
  
  // 如果预设颜色不够，生成随机颜色
  if (count > colors.length) {
    for (let i = colors.length; i < count; i++) {
      const color = '#' + Math.floor(Math.random() * 16777215).toString(16)
      colors.push(color)
    }
  }
  
  return colors.slice(0, count)
}
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
}

.welcome-panel {
  background: linear-gradient(to right, #24C6DC, #514A9D);
  color: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.welcome-panel h2 {
  margin: 0;
  font-size: 1.5rem;
}

.welcome-panel p {
  margin: 10px 0 0;
  font-size: 1rem;
  opacity: 0.9;
}

.data-overview {
  margin-bottom: 20px;
}

.overview-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  border-radius: 10px;
  margin-right: 16px;
  color: white;
  font-size: 24px;
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
  color: #333;
  margin-bottom: 5px;
}

.data-info {
  font-size: 12px;
  color: #999;
}

.main-content {
  margin-bottom: 20px;
}

.chart-card, .recent-card, .reminder-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.reminder-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.reminder-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-info {
  margin-left: 10px;
  flex: 1;
}

.reminder-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.reminder-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.reminder-content {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style> 