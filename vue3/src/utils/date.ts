import dayjs from "dayjs";

 // 格式化日期
export const formatDate = (date: string) => {
    if (!date) {
      return "无";
    }
    return dayjs(date).format("YYYY-MM-DD");
    // return new Date(date).toLocaleDateString();
  };

// 格式化日期时间
export const formatDateTime = (dateTime: string) => {
    return dayjs(dateTime).format('YYYY-MM-DD HH:mm');
  };
  