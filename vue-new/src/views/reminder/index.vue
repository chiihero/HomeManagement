<template>
  <div class="reminder-manage">
    <div class="header">
      <h2>物品提醒管理</h2>
      <el-button type="primary" @click="handleAdd">新增提醒</el-button>
    </div>

    <el-card class="search-card">
      <el-form :model="searchForm" ref="searchFormRef" label-width="80px" inline>
        <el-form-item label="物品名称">
          <el-input v-model="searchForm.itemName" placeholder="请输入物品名称" clearable />
        </el-form-item>
        <el-form-item label="提醒类型">
          <el-select v-model="searchForm.type" placeholder="请选择提醒类型" clearable>
            <el-option label="到期提醒" value="EXPIRATION" />
            <el-option label="维护提醒" value="MAINTENANCE" />
            <el-option label="归还提醒" value="RETURN" />
            <el-option label="其他提醒" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待提醒" value="PENDING" />
            <el-option label="已提醒" value="NOTIFIED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已过期" value="EXPIRED" />
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
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearchForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="reminderList"
        style="width: 100%"
        border
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="itemName" label="物品名称" min-width="120" />
        <el-table-column label="提醒类型" width="100">
          <template #default="scope">
            <el-tag :type="getReminderTypeColor(scope.row.type)">
              {{ getReminderTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提醒日期" min-width="120">
          <template #default="scope">
            {{ formatDate(scope.row.reminderDate) }}
          </template>
        </el-table-column>
        <el-table-column label="提前提醒天数" width="120" align="center">
          <template #default="scope">
            {{ scope.row.daysInAdvance }} 天
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getReminderStatusColor(scope.row.status)">
              {{ getReminderStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="提醒内容" min-width="150" show-overflow-tooltip />
        <el-table-column label="创建时间" min-width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              size="small" 
              type="success" 
              @click="handleComplete(scope.row)"
            >
              标记完成
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
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
    </el-card>

    <!-- 添加/编辑提醒对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="550px"
    >
      <el-form :model="reminderForm" ref="reminderFormRef" :rules="rules" label-width="100px">
        <el-form-item label="物品" prop="itemId">
          <el-select 
            v-model="reminderForm.itemId" 
            placeholder="请选择物品" 
            filterable 
            remote 
            :remote-method="searchItems"
            :loading="itemsLoading"
          >
            <el-option
              v-for="item in entityOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒类型" prop="type">
          <el-select v-model="reminderForm.type" placeholder="请选择提醒类型">
            <el-option label="到期提醒" value="EXPIRATION" />
            <el-option label="维护提醒" value="MAINTENANCE" />
            <el-option label="归还提醒" value="RETURN" />
            <el-option label="其他提醒" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒日期" prop="reminderDate">
          <el-date-picker
            v-model="reminderForm.reminderDate"
            type="date"
            placeholder="请选择提醒日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="提前提醒天数" prop="daysInAdvance">
          <el-input-number v-model="reminderForm.daysInAdvance" :min="0" :max="90" />
        </el-form-item>
        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="reminderForm.content"
            type="textarea"
            placeholder="请输入提醒内容"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="通知方式" prop="notificationMethods">
          <el-checkbox-group v-model="reminderForm.notificationMethods">
            <el-checkbox label="EMAIL">邮件</el-checkbox>
            <el-checkbox label="SYSTEM">系统通知</el-checkbox>
            <el-checkbox label="SMS">短信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="重复提醒">
          <el-switch v-model="reminderForm.isRecurring" />
        </el-form-item>
        <el-form-item v-if="reminderForm.isRecurring" label="重复周期" prop="recurringCycle">
          <el-select v-model="reminderForm.recurringCycle" placeholder="请选择重复周期">
            <el-option label="每天" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="每年" value="YEARLY" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import moment from 'moment'
import type { FormInstance, FormRules } from 'element-plus'
import { getReminderDetail, addReminder, updateReminder, deleteReminder, completeReminder, getRemindersByDateRange, getReminders } from '@/api/reminder'
import { searchEntities } from '@/api/entity'
import type { Entity } from '@/types/entity'
import type { Reminder, ReminderQueryParams } from '@/types/reminder'
import { useAuthStore } from '@/store/modules/auth'

// 状态和数据
const loading = ref(false)
const reminderList = ref<Reminder[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const authStore = useAuthStore()

// 搜索表单
const searchFormRef = ref<FormInstance>()
const searchForm = reactive<ReminderQueryParams>({
  itemName: '',
  type: '',
  status: '',
  dateRange: []
})

// 添加/编辑表单
const reminderFormRef = ref<FormInstance>()
const dialogVisible = ref(false)
const dialogTitle = ref('新增提醒')
const itemsLoading = ref(false)
const entityOptions = ref<Entity[]>([])
const reminderForm = reactive({
  id: '',
  itemId: '',
  type: 'EXPIRATION',
  reminderDate: '',
  daysInAdvance: 3,
  content: '',
  notificationMethods: ['SYSTEM'],
  isRecurring: false,
  recurringCycle: 'MONTHLY'
})

// 表单验证规则
const rules = reactive<FormRules>({
  itemId: [{ required: true, message: '请选择物品', trigger: 'change' }],
  type: [{ required: true, message: '请选择提醒类型', trigger: 'change' }],
  reminderDate: [{ required: true, message: '请选择提醒日期', trigger: 'change' }],
  daysInAdvance: [{ required: true, message: '请输入提前提醒天数', trigger: 'blur' }],
  content: [{ required: true, message: '请输入提醒内容', trigger: 'blur' }],
  notificationMethods: [{ required: true, message: '请选择至少一种通知方式', trigger: 'change' }],
  recurringCycle: [{ required: true, message: '请选择重复周期', trigger: 'change' }]
})

// 默认日期范围为最近30天
onMounted(() => {
  // 默认设置日期范围为当前日期和未来30天
  const today = moment().format('YYYY-MM-DD')
  const future30Days = moment().add(30, 'days').format('YYYY-MM-DD')
  searchForm.dateRange = [today, future30Days]
  fetchReminderList()
})

// 获取提醒列表
const fetchReminderList = async () => {
  loading.value = true
  try {
    const ownerId = authStore.currentUser?.id || 1 // 从store中获取当前用户ID
    
    let reminders: Reminder[] = []
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      // 使用日期范围查询
      const startDate = searchForm.dateRange[0]
      const endDate = searchForm.dateRange[1]
      
      const { data } = await getRemindersByDateRange(ownerId, startDate, endDate)
      reminders = data || []
    } else {
      // 无日期范围时，默认查询状态
      const params = {
        ownerId,
        status: searchForm.status || undefined,
        type: searchForm.type || undefined
      }
      const { data } = await getReminders(params)
      reminders = data || []
    }
    
    // 过滤处理其他查询条件
    if (searchForm.itemName) {
      reminders = reminders.filter(item => 
        item.itemName?.toLowerCase().includes(searchForm.itemName.toLowerCase())
      )
    }
    
    if (searchForm.type && !searchForm.dateRange) {
      reminders = reminders.filter(item => item.type === searchForm.type)
    }
    
    if (searchForm.status && !searchForm.dateRange) {
      reminders = reminders.filter(item => item.status === searchForm.status)
    }
    
    // 手动分页处理
    total.value = Array.isArray(reminders) ? reminders.length : 0
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    reminderList.value = Array.isArray(reminders) ? reminders.slice(startIndex, endIndex) : []
  } catch (error) {
    console.error('获取提醒列表失败', error)
    ElMessage.error('获取提醒列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索物品
const searchItems = async (query: string) => {
  if (query) {
    itemsLoading.value = true
    try {
      const { data } = await searchEntities({ 
        keyword: query, 
        ownerId: authStore.currentUser?.id || 1 // 添加ownerId参数
      })
      entityOptions.value = data
    } catch (error) {
      console.error('搜索物品失败', error)
    } finally {
      itemsLoading.value = false
    }
  } else {
    entityOptions.value = []
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReminderList()
}

// 重置搜索表单
const resetSearchForm = () => {
  // 重置为默认日期范围
  const today = moment().format('YYYY-MM-DD')
  const future30Days = moment().add(30, 'days').format('YYYY-MM-DD')
  searchForm.itemName = ''
  searchForm.type = ''
  searchForm.status = ''
  searchForm.dateRange = [today, future30Days]
  
  if (searchFormRef.value) {
    searchFormRef.value.resetFields()
  }
  
  currentPage.value = 1
  fetchReminderList()
}

// 添加提醒
const handleAdd = () => {
  dialogTitle.value = '新增提醒'
  Object.assign(reminderForm, {
    id: '',
    itemId: '',
    type: 'EXPIRATION',
    reminderDate: moment().add(30, 'days').format('YYYY-MM-DD'),
    daysInAdvance: 3,
    content: '',
    notificationMethods: ['SYSTEM'],
    isRecurring: false,
    recurringCycle: 'MONTHLY'
  })
  dialogVisible.value = true
}

// 编辑提醒
const handleEdit = async (row: Reminder) => {
  dialogTitle.value = '编辑提醒'
  try {
    const { data } = await getReminderDetail(row.id)
    Object.assign(reminderForm, {
      id: data.id,
      itemId: data.itemId,
      type: data.type,
      reminderDate: data.reminderDate,
      daysInAdvance: data.daysInAdvance,
      content: data.content,
      notificationMethods: data.notificationMethods,
      isRecurring: data.isRecurring,
      recurringCycle: data.recurringCycle || 'MONTHLY'
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取提醒详情失败', error)
    ElMessage.error('获取提醒详情失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!reminderFormRef.value) return
  
  await reminderFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = {
          id: reminderForm.id,
          itemId: reminderForm.itemId,
          type: reminderForm.type,
          reminderDate: reminderForm.reminderDate,
          daysInAdvance: reminderForm.daysInAdvance,
          content: reminderForm.content,
          notificationMethods: reminderForm.notificationMethods,
          isRecurring: reminderForm.isRecurring,
          recurringCycle: reminderForm.isRecurring ? reminderForm.recurringCycle : null
        }
        
        if (reminderForm.id) {
          await updateReminder(formData)
          ElMessage.success('更新提醒成功')
        } else {
          await addReminder(formData)
          ElMessage.success('添加提醒成功')
        }
        dialogVisible.value = false
        fetchReminderList()
      } catch (error) {
        console.error('保存提醒失败', error)
        ElMessage.error('保存提醒失败')
      }
    }
  })
}

// 标记完成
const handleComplete = (row: Reminder) => {
  ElMessageBox.confirm(`确定将"${row.itemName}"的提醒标记为已完成吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await completeReminder(row.id)
      ElMessage.success('操作成功')
      fetchReminderList()
    } catch (error) {
      console.error('操作失败', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 删除提醒
const handleDelete = (row: Reminder) => {
  ElMessageBox.confirm(`确定删除"${row.itemName}"的提醒吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReminder(row.id)
      ElMessage.success('删除成功')
      fetchReminderList()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 分页相关方法
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchReminderList()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchReminderList()
}

// 工具方法
const formatDate = (date: string) => {
  return date ? moment(date).format('YYYY-MM-DD') : '-'
}

const formatDateTime = (datetime: string) => {
  return datetime ? moment(datetime).format('YYYY-MM-DD HH:mm') : '-'
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
</script>

<style scoped>
.reminder-manage {
  padding: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style> 