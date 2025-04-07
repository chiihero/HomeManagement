// 响应结果
export interface ResponseResult<T = any> {
  code: number
  message: string
  data: T
} 