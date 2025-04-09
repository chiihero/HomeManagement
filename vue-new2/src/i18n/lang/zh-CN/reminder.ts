export default {
  reminder: {
    title: "提醒事项管理",
    add: "添加提醒",
    edit: "编辑提醒",
    delete: "删除提醒",
    complete: "完成提醒",
    search: {
      itemName: "物品名称",
      type: "提醒类型",
      status: "状态",
      dateRange: "提醒日期",
      placeholder: {
        itemName: "请输入物品名称",
        type: "请选择提醒类型",
        status: "请选择状态",
        dateRange: "请选择日期范围"
      }
    },
    table: {
      itemName: "物品名称",
      type: "提醒类型",
      reminderDate: "提醒日期",
      status: "状态",
      content: "提醒内容",
      notificationMethods: "通知方式",
      actions: "操作"
    },
    form: {
      item: "物品",
      type: "提醒类型",
      reminderDate: "提醒日期",
      content: "提醒内容",
      notificationMethods: "通知方式",
      daysInAdvance: "提前提醒",
      isRecurring: "重复提醒",
      recurringCycle: "重复周期",
      placeholder: {
        item: "请选择物品",
        type: "请选择提醒类型",
        reminderDate: "请选择提醒日期",
        content: "请输入提醒内容",
        recurringCycle: "请选择重复周期"
      }
    },
    types: {
      EXPIRATION: "到期提醒",
      MAINTENANCE: "维护提醒",
      RETURN: "归还提醒",
      OTHER: "其他提醒"
    },
    status: {
      PENDING: "待提醒",
      NOTIFIED: "已提醒",
      COMPLETED: "已完成",
      EXPIRED: "已过期"
    },
    notificationMethods: {
      EMAIL: "邮件",
      SYSTEM: "系统通知",
      SMS: "短信"
    },
    recurringCycles: {
      DAILY: "每天",
      WEEKLY: "每周",
      MONTHLY: "每月",
      YEARLY: "每年"
    },
    messages: {
      addSuccess: "添加提醒成功",
      addFailed: "添加提醒失败",
      editSuccess: "更新提醒成功",
      editFailed: "更新提醒失败",
      deleteSuccess: "删除提醒成功",
      deleteFailed: "删除提醒失败",
      completeSuccess: "标记提醒为已完成",
      completeFailed: "标记提醒失败",
      confirmDelete: "确定要删除该提醒吗？"
    }
  }
};
