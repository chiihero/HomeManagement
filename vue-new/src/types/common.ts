/**
 * 通用响应结果
 */
export interface ResponseResult<T> {
  /**
   * 状态码
   */
  code: number;
  /**
   * 响应消息
   */
  message: string;
  /**
   * 数据
   */
  data: T;
}

/**
 * 分页查询结果
 */
export interface PageResult<T> {
  /**
   * 总记录数
   */
  total: number;
  /**
   * 当前页数据列表
   */
  list: T[];
  /**
   * 每页大小
   */
  size: number;
  /**
   * ·页码
   */
  page: number;
}

/**
 * 分页查询参数
 */
export interface PageParams {
  /**
   * 页码
   */
  page?: number;
  /**
   * 每页大小
   */
  size?: number;
} 