import { ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type {
  Reminder,
  ReminderQueryParams,
  NotificationMethod
} from "@/types/reminder";
import type { ResponseResult } from "@/types/http";
import {
  fetchReminders,
  fetchReminderDetail,
  createReminder,
  updateReminder,
  deleteReminder,
  processReminder
} from "@/api/reminder";

export function useReminderCRUD() {
  const loading = ref(false);
  const saving = ref(false);
  const reminderList = ref<Reminder[]>([]);
  const total = ref(0);
  const currentReminder = ref<Reminder | null>(null);
  const isEditing = ref(false);
  const isAdding = ref(false);

  // 加载提醒列表
  const loadReminders = async (params: ReminderQueryParams) => {
    try {
      loading.value = true;
      const response = await fetchReminders(params);
      
      // 正确处理响应
      reminderList.value = response.data || [];
      total.value = (response.data || []).length; // 由于后端没有返回总数，暂时使用数组长度
    } catch (error) {
      ElMessage.error("加载提醒列表失败");
      console.error("加载提醒列表失败:", error);
    } finally {
      loading.value = false;
    }
  };

  // 加载提醒详情
  const loadReminderDetail = async (id: number) => {
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

  // 打开编辑表单
  const openEditForm = async (id: number) => {
    await loadReminderDetail(id);
    isEditing.value = true;
  };

  // 打开添加表单
  const openAddForm = () => {
    currentReminder.value = null;
    isAdding.value = true;
  };

  // 取消编辑/添加
  const cancelEdit = () => {
    isEditing.value = false;
    isAdding.value = false;
    currentReminder.value = null;
  };

  // 保存提醒
  const saveReminder = async (formData: Reminder) => {
    try {
      saving.value = true;
      formData.notificationMethods= formData.notificationMethods.join(',');
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
      await loadReminders({ page: 1, size: 10 });
    } catch (error) {
      ElMessage.error(isEditing.value ? "更新提醒失败" : "创建提醒失败");
      console.error("保存提醒失败:", error);
    } finally {
      saving.value = false;
    }
  };

  // 删除提醒
  const handleDelete = async (id: number) => {
    try {
      await ElMessageBox.confirm("确定要删除该提醒吗？", "提示", {
        type: "warning"
      });
      const response = await deleteReminder(id);
      if (response.code === 200) {
        ElMessage.success("删除提醒成功");
        await loadReminders({ page: 1, size: 10 });
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

  // 完成提醒
  const handleComplete = async (id: number) => {
    try {
      const response = await processReminder(id);
      if (response.code === 200) {
        ElMessage.success("标记提醒为已完成");
        await loadReminders({ page: 1, size: 10 });
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
  };
}
