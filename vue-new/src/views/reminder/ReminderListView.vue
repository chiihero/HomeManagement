<template>
  <div class="container-fluid">
    <div class="page-header">
      <h2>提醒管理</h2>
      <div class="page-header-actions">
        <el-button type="primary" size="small" @click="openReminderForm(null)">
          <el-icon><Plus /></el-icon> 添加提醒
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <div class="filter-container">
        <el-select v-model="filterStatus" placeholder="提醒状态" clearable @change="handleFilter">
          <el-option label="待处理" value="pending" />
          <el-option label="已处理" value="processed" />
          <el-option label="已过期" value="expired" />
        </el-select>
        <el-select v-model="filterType" placeholder="提醒类型" clearable @change="handleFilter">
          <el-option label="保修到期" value="warranty" />
          <el-option label="定期维护" value="maintenance" />
          <el-option label="借用归还" value="return" />
          <el-option label="实体过期" value="expiry" />
          <el-option label="自定义提醒" value="custom" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="resetFilter">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
      </div>
    </el-card>

    <el-card class="reminder-list-card">
      <template #header>
        <div class="card-header-wrapper">
          <span>提醒列表</span>
          <div class="header-actions">
            <el-button type="success" size="small" @click="processSelected" :disabled="selectedReminders.length === 0">
              <el-icon><Check /></el-icon> 批量标记已处理
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="reminder-list-wrapper">
        <el-empty v-if="reminders.length === 0" description="暂无提醒数据" />
        
        <el-table
          v-else
          ref="reminderTable"
          :data="reminders"
          style="width: 100%"
          border
          v-loading="loading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="getStatusType(scope.row.status)"
                effect="light"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="scope">
              <el-tag
                :type="getTypeTagType(scope.row.type)"
                effect="plain"
              >
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="entityName" label="关联实体" min-width="150" show-overflow-tooltip />
          <el-table-column prop="remindDate" label="提醒日期" width="120" sortable />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="openReminderForm(scope.row)"
                text
              >
                编辑
              </el-button>
              <el-button
                v-if="scope.row.status === 'pending'"
                size="small"
                type="success"
                @click="processReminder(scope.row.id)"
                text
              >
                标记处理
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="confirmDelete(scope.row)"
                text
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 提醒表单弹窗 -->
    <el-dialog
      v-model="reminderFormVisible"
      :title="currentReminder.id ? '编辑提醒' : '添加提醒'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="reminderFormRef"
        :model="currentReminder"
        :rules="reminderFormRules"
        label-width="100px"
      >
        <el-form-item label="提醒标题" prop="title">
          <el-input v-model="currentReminder.title" placeholder="请输入提醒标题" />
        </el-form-item>
        <el-form-item label="提醒类型" prop="type">
          <el-select v-model="currentReminder.type" placeholder="请选择提醒类型" style="width: 100%">
            <el-option label="保修到期" value="warranty" />
            <el-option label="定期维护" value="maintenance" />
            <el-option label="借用归还" value="return" />
            <el-option label="实体过期" value="expiry" />
            <el-option label="自定义提醒" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联实体" prop="entityId">
          <el-select 
            v-model="currentReminder.entityId" 
            placeholder="请选择关联实体" 
            style="width: 100%"
            filterable
            remote
            :remote-method="searchEntities"
            :loading="entitiesLoading"
            clearable
          >
            <el-option 
              v-for="item in entityOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒日期" prop="remindDate">
          <el-date-picker
            v-model="currentReminder.remindDate"
            type="date"
            placeholder="选择提醒日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="currentReminder.content"
            type="textarea"
            placeholder="请输入提醒内容"
            rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reminderFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReminderForm" :loading="submitting">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import moment from 'moment'
import { 
  getReminders,
  updateReminder,
  deleteReminder,
  completeReminder as processReminderApi,
  getRemindersByDateRange
} from '@/api/reminder'
import { useAuthStore } from '@/store/modules/auth'
import type { Reminder } from '@/types/reminder'
import { searchEntities } from '@/api/entity'

// 分页和过滤参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')
const filterType = ref('')
const dateRange = ref<[string, string] | null>(null)

// 表格数据
const reminders = ref<Reminder[]>([])
const loading = ref(false)
const selectedReminders = ref<Reminder[]>([])
const reminderTable = ref()

// 表单数据
const reminderFormVisible = ref(false)
const reminderFormRef = ref<FormInstance>()
const submitting = ref(false)
const currentReminder = reactive<Reminder>({
  title: '',
  type: 'custom',
  remindDate: '',
  status: 'pending'
})

// 实体选择器数据
const entityOptions = ref<{ id: number, name: string }[]>([])
const entitiesLoading = ref(false)

// 表单验证规则
const reminderFormRules = {
  title: [
    { required: true, message: '请输入提醒标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择提醒类型', trigger: 'change' }
  ],
  remindDate: [
    { required: true, message: '请选择提醒日期', trigger: 'change' }
  ]
}

// 状态和类型显示转换
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'warning',
    'processed': 'success',
    'expired': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待处理',
    'processed': '已处理',
    'expired': '已过期'
  }
  return map[status] || '未知'
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    'warranty': 'info',
    'maintenance': 'success',
    'return': 'warning',
    'expiry': 'danger',
    'custom': 'primary'
  }
  return map[type] || 'info'
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    'warranty': '保修到期',
    'maintenance': '定期维护',
    'return': '借用归还',
    'expiry': '实体过期',
    'custom': '自定义'
  }
  return map[type] || '未知'
}

// 默认设置日期范围为当前日期和未来30天
onMounted(() => {
  const today = moment().format('YYYY-MM-DD')
  const future30Days = moment().add(30, 'days').format('YYYY-MM-DD')
  dateRange.value = [today, future30Days]
  fetchReminders()
})

// 获取提醒列表
const fetchReminders = async () => {
  loading.value = true
  
  const authStore = useAuthStore()
  const ownerId = authStore.currentUser?.id || 1
  
  try {
    let remindersList: Reminder[] = []
    
    // 使用日期范围查询接口
    if (dateRange.value && dateRange.value.length === 2) {
      const startDate = dateRange.value[0]
      const endDate = dateRange.value[1]
      
      const response = await getRemindersByDateRange(ownerId, startDate, endDate)
      remindersList = response.data || []
    } else {
      // 如果没有日期范围，按状态查询
      const params = {
        ownerId,
        status: filterStatus.value || undefined,
        type: filterType.value || undefined
      }
      const response = await getReminders(params)
      remindersList = response.data || []
    }
    
    // 过滤器处理
    if (filterStatus.value && dateRange.value) {
      remindersList = remindersList.filter(item => item.status === filterStatus.value)
    }
    
    if (filterType.value && dateRange.value) {
      remindersList = remindersList.filter(item => item.type === filterType.value)
    }
    
    // 手动分页处理
    total.value = remindersList.length
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    reminders.value = remindersList.slice(startIndex, endIndex)
  } catch (error) {
    console.error('获取提醒列表失败', error)
    ElMessage.error('获取提醒列表失败')
  } finally {
    loading.value = false
  }
}

// 打开提醒表单
const openReminderForm = (reminder: Reminder | null) => {
  if (reminder) {
    Object.assign(currentReminder, reminder)
  } else {
    // 重置表单
    Object.assign(currentReminder, {
      id: undefined,
      title: '',
      content: '',
      type: 'custom',
      entityId: undefined,
      entityName: undefined,
      remindDate: '',
      status: 'pending'
    })
  }
  reminderFormVisible.value = true
}

// 提交提醒表单
const submitReminderForm = async () => {
  if (!reminderFormRef.value) return
  
  await reminderFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (currentReminder.id) {
        // 更新提醒
        await updateReminder(currentReminder.id, currentReminder)
        ElMessage.success('提醒更新成功')
      } else {
        // 创建提醒
        await getReminders({
          ownerId: authStore.currentUser?.id || 1,
          title: currentReminder.title,
          type: currentReminder.type,
          remindDate: currentReminder.remindDate,
          content: currentReminder.content,
          entityId: currentReminder.entityId
        })
        ElMessage.success('提醒创建成功')
      }
      reminderFormVisible.value = false
      fetchReminders()
    } catch (error) {
      console.error('保存提醒失败', error)
      ElMessage.error('保存提醒失败')
    } finally {
      submitting.value = false
    }
  })
}

// 确认删除
const confirmDelete = (reminder: Reminder) => {
  ElMessageBox.confirm(
    `确定要删除提醒"${reminder.title}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      if (!reminder.id) return
      await deleteReminder(reminder.id)
      ElMessage.success('提醒删除成功')
      fetchReminders()
    } catch (error) {
      console.error('删除提醒失败', error)
      ElMessage.error('删除提醒失败')
    }
  }).catch(() => {
    // 取消删除，不做处理
  })
}

// 处理单个提醒
const processReminder = async (id: number) => {
  if (!id) return
  
  try {
    await processReminderApi(id.toString())
    ElMessage.success('提醒已标记为处理')
    fetchReminders()
  } catch (error) {
    console.error('处理提醒失败', error)
    ElMessage.error('处理提醒失败')
  }
}

// 处理选中的提醒
const processSelected = async () => {
  if (selectedReminders.value.length === 0) {
    ElMessage.warning('请选择要处理的提醒')
    return
  }
  
  ElMessageBox.confirm(
    `确定要将选中的 ${selectedReminders.value.length} 条提醒标记为已处理吗？`,
    '批量处理确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 创建一个包含所有请求的数组
      const promises = selectedReminders.value
        .filter(item => item.id && item.status === 'pending')
        .map(item => processReminderApi(item.id!.toString()))
      
      await Promise.all(promises)
      ElMessage.success('批量处理成功')
      fetchReminders()
    } catch (error) {
      console.error('批量处理失败', error)
      ElMessage.error('批量处理失败')
    }
  }).catch(() => {
    // 取消处理，不做任何操作
  })
}

// 表格选择变更事件
const handleSelectionChange = (selection: Reminder[]) => {
  selectedReminders.value = selection
}

// 搜索实体
const searchEntities = async (query: string) => {
  if (!query) return
  
  entitiesLoading.value = true
  try {
    const ownerId = authStore.currentUser?.id || 1
    const response = await searchEntities({ keyword: query, ownerId }) 
    entityOptions.value = response.data || []
  } catch (error) {
    console.error('搜索实体失败', error)
    // 添加备用方案，防止API调用失败
    entityOptions.value = [
      { id: 1, name: '示例实体1' },
      { id: 2, name: '示例实体2' },
      { id: 3, name: '示例实体3' }
    ].filter(item => item.name.includes(query))
  } finally {
    entitiesLoading.value = false
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchReminders()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchReminders()
}

const handleFilter = () => {
  currentPage.value = 1
  fetchReminders()
}

const resetFilter = () => {
  filterStatus.value = ''
  filterType.value = ''
  
  // 重置为默认日期范围
  const today = moment().format('YYYY-MM-DD')
  const future30Days = moment().add(30, 'days').format('YYYY-MM-DD')
  dateRange.value = [today, future30Days]
  
  currentPage.value = 1
  fetchReminders()
}
</script>

<style scoped>
.container-fluid {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reminder-list-wrapper {
  min-height: 300px;
}

.reminder-list-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 