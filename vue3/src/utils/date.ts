import moment from "moment";

 // 格式化日期
export const formatDate = (date: string) => {
    if (!date) {
      return "无";
    }
    return moment(date).format("YYYY-MM-DD");
    // return new Date(date).toLocaleDateString();
  };

// 格式化日期时间
export const formatDateTime = (dateTime: string) => {
    return moment(dateTime).format('YYYY-MM-DD HH:mm');
  };
  