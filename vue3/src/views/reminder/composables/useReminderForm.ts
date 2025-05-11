import { ref, reactive } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { Reminder } from "@/types/reminder";

/**
 * 提醒表单相关逻辑
 */
export function useReminderForm() {
  // 表单引用
  const reminderFormRef = ref<FormInstance>();

  // 表单数据
  const reminderForm = reactive<Reminder>({
    entityId: "",
    userId: "0",
    type: "warranty",
    remindDate: "",
    status: "pending",
    content: "",
    notificationMethods: "system",
    daysInAdvance: 1,
    isRecurring: false,
    recurringCycle: undefined,
    id: undefined,
    createUserId: undefined,
    createTime: undefined,
    updateTime: undefined
  });

  // 表单验证规则
  const rules = reactive<FormRules>({
    entityId: [{ required: true, message: "请选择物品", trigger: "change" }],
    type: [{ required: true, message: "请选择提醒类型", trigger: "change" }],
    remindDate: [
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

  /**
   * 填充表单数据
   * @param reminder 提醒数据
   */
  const fillFormWithReminder = (reminder: Reminder) => {
    Object.assign(reminderForm, {
      id: reminder.id,
      entityId: reminder.entityId,
      userId: reminder.userId,
      type: reminder.type,
      remindDate: reminder.remindDate,
      status: reminder.status,
      content: reminder.content,
      notificationMethods: reminder.notificationMethods
        ? reminder.notificationMethods.split(",")
        : ["system"],
      daysInAdvance: reminder.daysInAdvance,
      isRecurring: reminder.isRecurring,
      recurringCycle: reminder.recurringCycle
    });
  };

  /**
   * 重置表单
   */
  const resetForm = () => {
    if (reminderFormRef.value) {
      reminderFormRef.value.resetFields();
    }
    Object.assign(reminderForm, {
      entityId: "",
      userId: 0,
      type: "warranty",
      remindDate: "",
      status: "pending",
      content: "",
      notificationMethods: ["system"],
      daysInAdvance: 1,
      isRecurring: false,
      recurringCycle: undefined
    });
  };

  /**
   * 验证表单
   * @returns 验证结果
   */
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
