import type { MockMethod } from "vite-plugin-mock";
import {
  ReminderType,
  ReminderStatus,
  NotificationMethod,
  RecurringCycle
} from "@/types/reminder";

// 生成随机ID
const generateId = () => Math.floor(Math.random() * 1000000);

// 生成随机日期
const generateDate = (days: number) => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().split("T")[0];
};

// 生成随机提醒数据
const generateReminders = (count: number) => {
  const reminders = [];
  const types = Object.values(ReminderType);
  const statuses = Object.values(ReminderStatus);
  const methods = Object.values(NotificationMethod);
  const cycles = Object.values(RecurringCycle);

  for (let i = 0; i < count; i++) {
    const type = types[Math.floor(Math.random() * types.length)];
    const status = statuses[Math.floor(Math.random() * statuses.length)];
    const notificationMethods = methods.slice(
      0,
      Math.floor(Math.random() * methods.length) + 1
    );
    const isRecurring = Math.random() > 0.5;
    const recurringCycle = isRecurring
      ? cycles[Math.floor(Math.random() * cycles.length)]
      : undefined;

    reminders.push({
      id: generateId(),
      itemId: `item-${generateId()}`,
      itemName: `Item ${i + 1}`,
      userId: 1,
      type,
      reminderDate: generateDate(Math.floor(Math.random() * 30)),
      status,
      content: `This is a reminder for item ${i + 1}`,
      notificationMethods,
      daysInAdvance: Math.floor(Math.random() * 7) + 1,
      isRecurring,
      recurringCycle,
      createdAt: generateDate(-Math.floor(Math.random() * 30)),
      updatedAt: generateDate(-Math.floor(Math.random() * 30))
    });
  }

  return reminders;
};

// 模拟数据
const reminders = generateReminders(100);

export default [
  {
    url: "/api/reminders",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10, itemName, type, status, dateRange } = query;
      let filteredReminders = [...reminders];

      // 过滤条件
      if (itemName) {
        filteredReminders = filteredReminders.filter(item =>
          item.itemName.toLowerCase().includes(itemName.toLowerCase())
        );
      }
      if (type) {
        filteredReminders = filteredReminders.filter(
          item => item.type === type
        );
      }
      if (status) {
        filteredReminders = filteredReminders.filter(
          item => item.status === status
        );
      }
      if (dateRange) {
        const [startDate, endDate] = dateRange.split(",");
        filteredReminders = filteredReminders.filter(item => {
          const date = new Date(item.reminderDate);
          return date >= new Date(startDate) && date <= new Date(endDate);
        });
      }

      // 分页
      const start = (page - 1) * size;
      const end = start + size;
      const paginatedReminders = filteredReminders.slice(start, end);

      return {
        code: 200,
        data: {
          list: paginatedReminders,
          total: filteredReminders.length
        }
      };
    }
  },
  {
    url: "/api/reminders/:id",
    method: "get",
    response: ({ params }) => {
      const reminder = reminders.find(item => item.id === Number(params.id));
      return {
        code: 200,
        data: reminder
      };
    }
  },
  {
    url: "/api/reminders",
    method: "post",
    response: ({ body }) => {
      const newReminder = {
        id: generateId(),
        ...body,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      reminders.push(newReminder);
      return {
        code: 200,
        data: newReminder
      };
    }
  },
  {
    url: "/api/reminders/:id",
    method: "put",
    response: ({ params, body }) => {
      const index = reminders.findIndex(item => item.id === Number(params.id));
      if (index !== -1) {
        reminders[index] = {
          ...reminders[index],
          ...body,
          updatedAt: new Date().toISOString()
        };
        return {
          code: 200,
          data: reminders[index]
        };
      }
      return {
        code: 404,
        message: "Reminder not found"
      };
    }
  },
  {
    url: "/api/reminders/:id",
    method: "delete",
    response: ({ params }) => {
      const index = reminders.findIndex(item => item.id === Number(params.id));
      if (index !== -1) {
        reminders.splice(index, 1);
        return {
          code: 200
        };
      }
      return {
        code: 404,
        message: "Reminder not found"
      };
    }
  },
  {
    url: "/api/reminders/:id/complete",
    method: "patch",
    response: ({ params }) => {
      const index = reminders.findIndex(item => item.id === Number(params.id));
      if (index !== -1) {
        reminders[index] = {
          ...reminders[index],
          status: ReminderStatus.COMPLETED,
          updatedAt: new Date().toISOString()
        };
        return {
          code: 200,
          data: reminders[index]
        };
      }
      return {
        code: 404,
        message: "Reminder not found"
      };
    }
  },
  {
    url: "/api/reminders/search",
    method: "get",
    response: ({ query }) => {
      const { keyword } = query;
      const filteredReminders = reminders.filter(
        item =>
          item.itemName.toLowerCase().includes(keyword.toLowerCase()) ||
          item.content.toLowerCase().includes(keyword.toLowerCase())
      );
      return {
        code: 200,
        data: filteredReminders
      };
    }
  },
  {
    url: "/api/reminders/upcoming",
    method: "get",
    response: ({ query }) => {
      const { days = 7 } = query;
      const today = new Date();
      const upcomingDate = new Date();
      upcomingDate.setDate(today.getDate() + Number(days));

      const filteredReminders = reminders.filter(item => {
        const reminderDate = new Date(item.reminderDate);
        return reminderDate >= today && reminderDate <= upcomingDate;
      });

      return {
        code: 200,
        data: filteredReminders
      };
    }
  },
  {
    url: "/api/reminders/expired",
    method: "get",
    response: () => {
      const today = new Date();
      const filteredReminders = reminders.filter(item => {
        const reminderDate = new Date(item.reminderDate);
        return reminderDate < today && item.status !== ReminderStatus.COMPLETED;
      });

      return {
        code: 200,
        data: filteredReminders
      };
    }
  }
] as MockMethod[];
