<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h2 class="page-title m-0">物品提醒管理</h2>
      <el-button type="primary" @click="handleAdd">新增提醒</el-button>
    </div>

    <el-card shadow="hover" class="mb-medium">
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

    <el-card shadow="hover">
      <el-table
        v-loading="loading"
        :data="reminderList"
        class="w-100"
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

      <div class="mt-medium flex justify-end">
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
import { getReminderDetail, addReminder, updateReminder, deleteReminder, completeReminder, getRemindersByDateRange } from '@/api/reminder'
import { searchEntities } from '@/api/entity'
import type { Entity } from '@/types/entity'
import type { Reminder, ReminderQueryParams } from '@/types/reminder'
import { useAuthStore } from '@/store/modules/auth'

// 状态和数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增提醒')
const itemsLoading = ref(false)
const currentId = ref<number | null>(null)
const reminderList = ref<Reminder[]>([])
const entityOptions = ref<Entity[]>([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单引用
const searchFormRef = ref<FormInstance>()
const reminderFormRef = ref<FormInstance>()

// 用户信息
const authStore = useAuthStore()
const userId = ref(authStore.currentUser?.id || 0)

// 搜索表单
const searchForm = reactive<ReminderQueryParams>({
  itemName: '',
  type: '',
  status: '',
  dateRange: ['', ''],
  userId: authStore.currentUser?.id || 0
})

// 提醒表单
const reminderForm = reactive({
  id: undefined as number | undefined,
  itemId: undefined as string | undefined,
  itemName: '',
  userId: userId.value,
  type: 'EXPIRATION',
  reminderDate: '',
  status: 'PENDING',
  content: '',
  notificationMethods: ['SYSTEM'],
  daysInAdvance: 7,
  isRecurring: false,
  recurringCycle: 'MONTHLY'
})

// 表单校验规则
const rules = reactive<FormRules>({
  itemId: [
    { required: true, message: '请选择物品', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择提醒类型', trigger: 'change' }
  ],
  reminderDate: [
    { required: true, message: '请选择提醒日期', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入提醒内容', trigger: 'blur' }
  ],
  notificationMethods: [
    { required: true, message: '请选择至少一种通知方式', trigger: 'change' }
  ],
  recurringCycle: [
    { required: true, message: '请选择重复周期', trigger: 'change' }
  ]
})

// 初始化
onMounted(() => {
  // loadReminderList()
})

// 加载提醒列表
const loadReminderList = async () => {
  loading.value = true
  try {
    const params = {
      itemName: searchForm.itemName,
      type: searchForm.type,
      status: searchForm.status,
      startDate: searchForm.dateRange && searchForm.dateRange[0] ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange && searchForm.dateRange[1] ? searchForm.dateRange[1] : null,
      userId: searchForm.userId,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await getRemindersByDateRange(params.userId || 0, params.startDate || '', params.endDate || '')
    if (response.code === 200) {
      if (Array.isArray(response.data)) {
        reminderList.value = response.data
        total.value = response.data.length
      } else {
        console.error('返回数据格式错误')
        reminderList.value = []
        total.value = 0
      }
    } else {
      ElMessage.error(response.message || '加载提醒列表失败')
    }
  } catch (error) {
    console.error('加载提醒列表失败', error)
    ElMessage.error('加载提醒列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadReminderList()
}

// 重置搜索表单
const resetSearchForm = () => {
  if (searchFormRef.value) {
    searchFormRef.value.resetFields()
    handleSearch()
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadReminderList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadReminderList()
}

// 搜索物品
const searchItems = async (query: string) => {
  if (query.length < 1) return
  
  itemsLoading.value = true
  try {
    const response = await searchEntities({ 
      keyword: query, 
      userId: userId.value
    })
    if (response.code === 200) {
      entityOptions.value = response.data || []
    } else {
      entityOptions.value = []
    }
  } catch (error) {
    console.error('搜索物品失败', error)
    entityOptions.value = []
  } finally {
    itemsLoading.value = false
  }
}

// 新增提醒
const handleAdd = () => {
  dialogTitle.value = '新增提醒'
  currentId.value = null
  resetReminderForm()
  dialogVisible.value = true
}

// 编辑提醒
const handleEdit = async (row: Reminder) => {
  dialogTitle.value = '编辑提醒'
  currentId.value = row.id ? Number(row.id) : null
  
  try {
    const response = await getReminderDetail(String(row.id))
    if (response.code === 200) {
      const reminderData = response.data
      
      // 重置表单后再赋值
      resetReminderForm()
      
      // 填充表单数据
      reminderForm.itemId = reminderData.itemId
      reminderForm.itemName = reminderData.itemName
      reminderForm.type = reminderData.type || 'EXPIRATION'
      reminderForm.reminderDate = reminderData.reminderDate || ''
      reminderForm.status = reminderData.status || 'PENDING'
      reminderForm.content = reminderData.content || ''
      reminderForm.notificationMethods = reminderData.notificationMethods || ['SYSTEM']
      reminderForm.daysInAdvance = reminderData.daysInAdvance || 7
      reminderForm.isRecurring = reminderData.isRecurring || false
      reminderForm.recurringCycle = reminderData.recurringCycle || 'MONTHLY'
      
      // 如果有物品信息，添加到选项中
      if (reminderData.itemId && reminderData.itemName) {
        entityOptions.value = [{
          id: reminderData.itemId,
          name: reminderData.itemName
        } as Entity]
      }
      
      dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取提醒详情失败')
    }
  } catch (error) {
    console.error('获取提醒详情失败', error)
    ElMessage.error('获取提醒详情失败')
  }
}

// 重置提醒表单
const resetReminderForm = () => {
  reminderForm.id = undefined
  reminderForm.itemId = undefined
  reminderForm.itemName = ''
  reminderForm.userId = userId.value
  reminderForm.type = 'EXPIRATION'
  reminderForm.reminderDate = ''
  reminderForm.status = 'PENDING'
  reminderForm.content = ''
  reminderForm.notificationMethods = ['SYSTEM']
  reminderForm.daysInAdvance = 7
  reminderForm.isRecurring = false
  reminderForm.recurringCycle = 'MONTHLY'
  
  // 重置表单验证
  if (reminderFormRef.value) {
    reminderFormRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!reminderFormRef.value) return
  
  await reminderFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        let response
        const submitData = {
          ...reminderForm,
          id: currentId.value
        }
        
        if (currentId.value) {
          // 编辑提醒
          response = await updateReminder({
            id: String(currentId.value),
            ...submitData
          })
        } else {
          // 新增提醒
          response = await addReminder(submitData)
        }
        
        if (response.code === 200) {
          ElMessage.success(currentId.value ? '编辑成功' : '添加成功')
          dialogVisible.value = false
          loadReminderList()
        } else {
          ElMessage.error(response.message || (currentId.value ? '编辑失败' : '添加失败'))
        }
      } catch (error) {
        console.error('提交表单失败', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除提醒
const handleDelete = (row: Reminder) => {
  ElMessageBox.confirm('确定要删除这条提醒吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteReminder(String(row.id))
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadReminderList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除提醒失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除，不做任何处理
  })
}

// 标记完成
const handleComplete = async (row: Reminder) => {
  try {
    const response = await completeReminder(String(row.id))
    if (response.code === 200) {
      ElMessage.success('操作成功')
      loadReminderList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('标记完成失败', error)
    ElMessage.error('操作失败')
  }
}

// 获取提醒类型文本
const getReminderTypeText = (type: string) => {
  switch (type) {
    case 'EXPIRATION': return '到期提醒'
    case 'MAINTENANCE': return '维护提醒'
    case 'RETURN': return '归还提醒'
    case 'OTHER': return '其他提醒'
    default: return '未知'
  }
}

// 获取提醒类型颜色
const getReminderTypeColor = (type: string) => {
  switch (type) {
    case 'EXPIRATION': return 'danger'
    case 'MAINTENANCE': return 'warning'
    case 'RETURN': return 'info'
    case 'OTHER': return ''
    default: return ''
  }
}

// 获取提醒状态文本
const getReminderStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待提醒'
    case 'NOTIFIED': return '已提醒'
    case 'COMPLETED': return '已完成'
    case 'EXPIRED': return '已过期'
    default: return '未知'
  }
}

// 获取提醒状态颜色
const getReminderStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'NOTIFIED': return 'info'
    case 'COMPLETED': return 'success'
    case 'EXPIRED': return 'danger'
    default: return ''
  }
}

// 格式化日期
const formatDate = (date: string) => {
  return date ? moment(date).format('YYYY-MM-DD') : '-'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dateTime ? moment(dateTime).format('YYYY-MM-DD HH:mm') : '-'
}
</script> 