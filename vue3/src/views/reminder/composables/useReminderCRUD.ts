import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { Reminder, ReminderQueryParams } from "@/types/reminder";
import {
  fetchReminderDetail,
  createReminder,
  updateReminder,
  deleteReminder,
  processReminder,
  pageReminders
} from "@/api/reminder";

/**
 * 提醒CRUD相关逻辑
 */
export function useReminderCRUD() {
  // 加载状态
  const loading = ref(false);
  // 保存状态
  const saving = ref(false);
  // 提醒列表
  const reminderList = ref<Reminder[]>([]);
  // 当前提醒
  const currentReminder = ref<Reminder | null>(null);
  // 编辑状态
  const isEditing = ref(false);
  // 添加状态
  const isAdding = ref(false);

  // 分页参数
  const paginationConfig = ref({
    current: 1,
    size: 10,
    total: 0
  });

  // 搜索表单
  const searchForm = reactive<ReminderQueryParams>({
    entityId: undefined,
    entityName: "",
    type: undefined,
    status: undefined,
    dateRange: undefined,
    userId: undefined
  });

  /**
   * 重置搜索表单
   */
  const resetSearchForm = () => {
    Object.assign(searchForm, {
      entityId: "",
      entityName: "",
      type: undefined,
      status: undefined,
      dateRange: undefined,
      userId: undefined
    });
  };

  /**
   * 处理页码改变
   * @param current 当前页码
   */
  const handleCurrentChange = (current: number) => {
    paginationConfig.value.current = current;
  };

  /**
   * 处理每页大小改变
   * @param size 每页大小
   */
  const handleSizeChange = (size: number) => {
    paginationConfig.value.size = size;
  };

  /**
   * 处理日期范围变化
   * @param range 日期范围
   */
  const handleDateRangeChange = (range: [string, string] | null) => {
    searchForm.dateRange = range || undefined;
  };

  /**
   * 加载提醒列表
   * @param params 查询参数
   */
  const loadReminders = async (params: ReminderQueryParams) => {
    try {
      loading.value = true;
      const response = await pageReminders({
        current: paginationConfig.value.current,
        size: paginationConfig.value.size,
        ...params
      });

      // @ts-ignore - 类型断言，忽略data属性不存在的错误
      reminderList.value = response.data.records || response.data.list || [];
      // @ts-ignore - 类型断言，忽略data属性不存在的错误
      paginationConfig.value.total = response.data.total;

    } catch (error) {
      ElMessage.error("加载提醒列表失败");
      console.error("加载提醒列表失败:", error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 加载提醒详情
   * @param id 提醒ID
   */
  const loadReminderDetail = async (id: string) => {
    try {
      loading.value = true;
      const response = await fetchReminderDetail(id);
      
      // 设置当前提醒
      if (response.data) {
        currentReminder.value = response.data;
      } else {
        ElMessage.error("加载提醒详情失败");
      }
    } catch (error) {
      ElMessage.error("加载提醒详情失败");
      console.error("加载提醒详情失败:", error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 打开编辑表单
   * @param id 提醒ID
   */
  const openEditForm = async (id: string) => {
    await loadReminderDetail(id);
    isEditing.value = true;
  };

  /**
   * 打开添加表单
   */
  const openAddForm = () => {
    currentReminder.value = null;
    isAdding.value = true;
  };

  /**
   * 取消编辑/添加
   */
  const cancelEdit = () => {
    isEditing.value = false;
    isAdding.value = false;
    currentReminder.value = null;
  };

  /**
   * 保存提醒
   * @param formData 表单数据
   */
  const saveReminder = async (formData: Reminder) => {
    try {
      saving.value = true;
      formData.notificationMethods = Array.isArray(formData.notificationMethods) 
        ? formData.notificationMethods.join(',')
        : formData.notificationMethods;
      
      if (isEditing.value && currentReminder.value) {
        const response = await updateReminder(currentReminder.value.id, formData);
        if (response.code === 200) {
          ElMessage.success("更新提醒成功");
        } else {
          ElMessage.error(response.message || "更新提醒失败");
        }
      } else {
        const response = await createReminder(formData);
        if (response.code === 200) {
          ElMessage.success("创建提醒成功");
        } else {
          ElMessage.error(response.message || "创建提醒失败");
        }
      }
      cancelEdit();
      await loadReminders(null);
    } catch (error) {
      ElMessage.error(isEditing.value ? "更新提醒失败" : "创建提醒失败");
      console.error("保存提醒失败:", error);
    } finally {
      saving.value = false;
    }
  };

  /**
   * 删除提醒
   * @param id 提醒ID
   */
  const handleDelete = async (id: string) => {
    try {
      await ElMessageBox.confirm("确定要删除该提醒吗？", "提示", {
        type: "warning"
      });
      const response = await deleteReminder(id);
      if (response.code === 200) {
        ElMessage.success("删除提醒成功");
        await loadReminders(null);
      } else {
        ElMessage.error(response.message || "删除提醒失败");
      }
    } catch (error) {
      if (error !== "cancel") {
        ElMessage.error("删除提醒失败");
        console.error("删除提醒失败:", error);
      }
    }
  };

  /**
   * 完成提醒
   * @param id 提醒ID
   */
  const handleComplete = async (id: string) => {
    try {
      const response = await processReminder(id);
      if (response.code === 200) {
        ElMessage.success("标记提醒为已完成");
        await loadReminders(null);
      } else {
        ElMessage.error(response.message || "标记提醒失败");
      }
    } catch (error) {
      ElMessage.error("标记提醒失败");
      console.error("标记提醒失败:", error);
    }
  };

  return {
    loading,
    saving,
    reminderList,
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
    handleComplete,
    searchForm,
    paginationConfig,
    resetSearchForm,
    handleCurrentChange,
    handleSizeChange,
    handleDateRangeChange
  };
}
