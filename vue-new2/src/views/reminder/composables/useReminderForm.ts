import { ref, reactive } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { Reminder, ReminderFormData } from "@/types/reminder";

export function useReminderForm() {
  const reminderFormRef = ref<FormInstance>();

  const reminderForm = reactive<ReminderFormData>({
    itemId: "",
    itemName: "",
    userId: 0,
    type: "EXPIRATION",
    reminderDate: "",
    status: "PENDING",
    content: "",
    notificationMethods: ["SYSTEM"],
    daysInAdvance: 1,
    isRecurring: false,
    recurringCycle: undefined
  });

  const rules = reactive<FormRules>({
    itemId: [{ required: true, message: "请选择物品", trigger: "change" }],
    type: [{ required: true, message: "请选择提醒类型", trigger: "change" }],
    reminderDate: [
      { required: true, message: "请选择提醒日期", trigger: "change" }
    ],
    content: [{ required: true, message: "请输入提醒内容", trigger: "blur" }],
    notificationMethods: [
      { required: true, message: "请选择通知方式", trigger: "change" }
    ],
    daysInAdvance: [
      { required: true, message: "请输入提前提醒天数", trigger: "blur" },
      {
        type: "number",
        min: 1,
        message: "提前提醒天数必须大于0",
        trigger: "blur"
      }
    ]
  });

  // 重置表单
  const resetForm = () => {
    if (reminderFormRef.value) {
      reminderFormRef.value.resetFields();
    }
    Object.assign(reminderForm, {
      itemId: "",
      itemName: "",
      userId: 0,
      type: "EXPIRATION",
      reminderDate: "",
      status: "PENDING",
      content: "",
      notificationMethods: ["SYSTEM"],
      daysInAdvance: 1,
      isRecurring: false,
      recurringCycle: undefined
    });
  };

  // 填充表单数据
  const fillFormWithReminder = (reminder: Reminder) => {
    Object.assign(reminderForm, {
      id: reminder.id,
      itemId: reminder.itemId,
      itemName: reminder.itemName,
      userId: reminder.userId,
      type: reminder.type,
      reminderDate: reminder.reminderDate,
      status: reminder.status,
      content: reminder.content,
      notificationMethods: reminder.notificationMethods,
      daysInAdvance: reminder.daysInAdvance,
      isRecurring: reminder.isRecurring,
      recurringCycle: reminder.recurringCycle
    });
  };

  // 验证表单
  const validateForm = async (): Promise<boolean> => {
    if (!reminderFormRef.value) return false;
    try {
      await reminderFormRef.value.validate();
      return true;
    } catch (error) {
      return false;
    }
  };

  return {
    reminderFormRef,
    reminderForm,
    rules,
    resetForm,
    fillFormWithReminder,
    validateForm
  };
}
