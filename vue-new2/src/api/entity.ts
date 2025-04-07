import http from '@/utils/http'
import type { EntityType, EntityFormData, EntityQueryParams, EntityStats, EntityImportExport } from '@/types/entity'
import type { ResponseResult } from '@/types/http'

// 获取物品列表
export const getEntities = (params: EntityQueryParams) => {
  return http.get<ResponseResult<{
    total: number
    list: EntityType[]
  }>>('/entities', { params })
}

// 获取物品树
export const getEntityTree = (userId: number) => {
  return http.get<ResponseResult<EntityType[]>>('/entities/tree', { params: { userId } })
}

// 获取物品详情
export const getEntity = (id: string) => {
  return http.get<ResponseResult<EntityType>>(`/entities/${id}`)
}

// 创建物品
export const createEntity = (data: EntityFormData) => {
  return http.post<ResponseResult<EntityType>>('/entities', data)
}

// 更新物品
export const updateEntity = (id: string, data: EntityFormData) => {
  return http.put<ResponseResult<EntityType>>(`/entities/${id}`, data)
}

// 删除物品
export const deleteEntity = (id: string) => {
  return http.delete<ResponseResult<void>>(`/entities/${id}`)
}

// 获取物品统计
export const getEntityStats = () => {
  return http.get<ResponseResult<EntityStats>>('/entities/stats')
}

// 导入物品
export const importEntities = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ResponseResult<{
    success: number
    failed: number
    errors: string[]
  }>>('/entities/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出物品
export const exportEntities = (params: EntityQueryParams) => {
  return http.get<Blob>('/entities/export', {
    params,
    responseType: 'blob'
  })
}

// 获取所有标签
export const getAllTags = () => {
  return http.get<ResponseResult<string[]>>('/entities/tags')
}

// 获取所有类型
export const getAllTypes = () => {
  return http.get<ResponseResult<string[]>>('/entities/types')
}

// 获取所有位置
export const getAllLocations = () => {
  return http.get<ResponseResult<string[]>>('/entities/locations')
}

// 批量更新物品状态
export const batchUpdateStatus = (ids: string[], status: string) => {
  return http.put<ResponseResult<void>>('/entities/batch/status', {
    ids,
    status
  })
}

// 批量删除物品
export const batchDeleteEntities = (ids: string[]) => {
  return http.delete<ResponseResult<void>>('/entities/batch', {
    data: { ids }
  })
}

// 搜索物品
export const searchEntities = (keyword: string) => {
  return http.get<ResponseResult<EntityType[]>>('/entities/search', {
    params: { keyword }
  })
}

// 获取物品历史记录
export const getEntityHistory = (id: string) => {
  return http.get<ResponseResult<{
    id: string
    entityId: string
    action: string
    changes: Record<string, any>
    operator: string
    createdAt: string
  }[]>>(`/entities/${id}/history`)
}

// 恢复物品历史版本
export const restoreEntityVersion = (id: string, historyId: string) => {
  return http.post<ResponseResult<EntityType>>(`/entities/${id}/restore/${historyId}`)
} 