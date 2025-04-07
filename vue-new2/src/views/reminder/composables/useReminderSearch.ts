import { ref, reactive } from 'vue'
import type { ReminderQueryParams } from '@/types/reminder'

export function useReminderSearch() {
  const searchForm = reactive<ReminderQueryParams>({
    itemName: '',
    type: undefined,
    status: undefined,
    dateRange: undefined,
    userId: undefined,
    page: 1,
    size: 10
  })

  const loading = ref(false)

  // 重置搜索表单
  const resetSearchForm = () => {
    Object.assign(searchForm, {
      itemName: '',
      type: undefined,
      status: undefined,
      dateRange: undefined,
      userId: undefined,
      page: 1,
      size: 10
    })
  }

  // 处理页码变化
  const handlePageChange = (page: number) => {
    searchForm.page = page
  }

  // 处理每页条数变化
  const handleSizeChange = (size: number) => {
    searchForm.size = size
    searchForm.page = 1
  }

  // 处理日期范围变化
  const handleDateRangeChange = (range: [string, string] | null) => {
    searchForm.dateRange = range || undefined
  }

  return {
    searchForm,
    loading,
    resetSearchForm,
    handlePageChange,
    handleSizeChange,
    handleDateRangeChange
  }
} 