import { http } from "@/utils/http";
import type { ResponseResult } from "@/types/http";
import type { Entity } from "@/types/entity";
import type { Reminder } from "@/types/reminder";
import { useUserStoreHook } from "@/store/modules/user";
const userId = useUserStoreHook().userId;

/**
 * 获取仪表盘统计数据
 * @returns 统计数据
 */
export const getDashboardStatistics = (): Promise<ResponseResult<any>> => {
  return http.get("/dashboard/statistics", { params: { userId } });
};

/**
 * 获取仪表盘卡片数据
 */
export interface DashboardStats {
  totalEntities: number;
  totalSpaces: number;
  totalItems: number;
  totalTags: number;
  expiringItems: number;
  recentAddedItems: number;
  totalValue: number;
  averageValue: number;
}

/**
 * 获取仪表盘统计卡片数据
 * @returns 统计卡片数据
 */
export const getDashboardStats = (): Promise<
  ResponseResult<DashboardStats>
> => {
  return http.get("/dashboard/stats", { params: { userId } });
};

/**
 * 获取仪表盘图表数据类型
 */
export interface DashboardChartData {
  // 物品状态分布
  statusDistribution: Array<{
    name: string;
    value: number;
  }>;
  // 物品标签分布（前10个）
  tagDistribution: Array<{
    name: string;
    value: number;
  }>;
  // 物品存放位置分布（前10个）
  locationDistribution: Array<{
    name: string;
    value: number;
  }>;
  // 最近6个月物品添加趋势
  additionTrend: Array<{
    month: string;
    count: number;
  }>;
}

/**
 * 获取仪表盘图表数据
 * @returns 图表数据
 */
export const getDashboardChartData = (): Promise<
  ResponseResult<DashboardChartData>
> => {
  return http.get("/dashboard/chart-data", { params: { userId } });
};

/**
 * 获取最近添加的实体
 * @param limit 限制数量
 */
export const getRecentEntities = (
  limit: number = 5
): Promise<ResponseResult<Entity[]>> => {
  return http.get(`/dashboard/recent-entities`, { params: { limit, userId } });
};

/**
 * 获取最近的待办提醒
 * @param limit 限制数量
 */
export const getRecentReminders = (
  limit: number = 5
): Promise<ResponseResult<Reminder[]>> => {
  return http.get(`/dashboard/recent-reminders`, { params: { limit, userId } });
};

/**
 * 获取时间趋势数据
 * @param period 时间周期：week, month, quarter, year
 */
export const getTimeTrendData = (
  period: "week" | "month" | "quarter" | "year"
): Promise<ResponseResult<any>> => {
  return http.get(`/dashboard/trends`, { params: { period, userId } });
};
