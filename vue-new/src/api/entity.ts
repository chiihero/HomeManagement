import http from './http';
import type { Entity, PageResult, ResponseResult, Tag } from '@/types/entity';

// 实体查询参数接口
export interface EntityQueryParams {
  current?: number;
  size?: number;
  name?: string;
  type?: string;
  specification?: string;
  status?: string;
  usageFrequency?: string;
  userId?: number;
  parentId?: number;
  userId?: number;
}

/**
 * 分页查询实体
 * @param params 查询参数
 */
export function pageEntities(params: any) {
  return http.get('/entities/page', { params });
}

/**
 * 获取实体详情
 * @param id 实体ID
 */
export const getEntityDetail = (id: string): Promise<ResponseResult<Entity>> => {
  return http.get(`/entities/${id}`);
};

/**
 * 添加实体
 * @param entity 实体信息
 */
export function addEntity(entity: Omit<Entity, 'id' | 'createTime' | 'updateTime'>) {
  return http.post('/entities', entity);
}

/**
 * 更新实体
 * @param id 实体ID
 * @param entity 实体信息
 */
export const updateEntity = (id: string, entity: Partial<Entity>): Promise<ResponseResult<Entity>> => {
  return http.put(`/entities/${id}`, entity);
};

/**
 * 删除实体
 * @param id 实体ID
 */
export const deleteEntity = (id: string): Promise<ResponseResult<null>> => {
  return http.delete(`/entities/${id}`);
};

/**
 * 上传实体图片
 * @param entityId 实体ID
 * @param file 文件对象
 * @param isMainImage 是否为主图
 * @returns 上传结果
 */
export function uploadEntityImage(entityId: number, file: File, isMainImage: boolean = false) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('entityId', entityId.toString());
  formData.append('isMainImage', isMainImage.toString());
  
  return http.post('/entity-images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 删除实体图片
 * @param imageId 图片ID
 * @returns 删除结果
 */
export const deleteEntityImage = (imageId: string): Promise<ResponseResult<boolean>> => {
  return http.delete(`/entity-images/${imageId}`);
};

/**
 * 获取父级为空的所有实体（顶层实体/位置）
 * @returns 实体列表
 */
export function getTopLevelEntities() {
  return http.get('/entities/top-level');
}

/**
 * 获取物品树形结构
 * @param userId 用户ID
 * @returns Promise<ResponseResult<any>>
 */
export function getEntityTree(userId: number) {
  console.log('请求物品树形结构，userId:', userId);
  return http.get('/entities/tree', { params: { userId } }).then(response => {
    console.log('物品树形结构响应:', response);
    return response;
  }).catch(error => {
    console.error('物品树形结构请求失败:', error);
    throw error;
  });
}

/**
 * 获取实体标签
 * @param entityId 实体ID
 * @returns 标签列表
 */
export const getEntityTags = (entityId: string): Promise<ResponseResult<Tag[]>> => {
  return http.get(`/entities/${entityId}/tags`);
};

/**
 * 设置实体标签
 * @param entityId 实体ID
 * @param tagIds 标签ID数组
 * @returns 设置结果
 */
export const setEntityTags = (entityId: string, tagIds: string[]): Promise<ResponseResult<null>> => {
  return http.post(`/entities/${entityId}/tags`, { tagIds });
};

/**
 * 获取实体位置选项（用于级联选择器）
 * @returns 位置选项列表
 */
export function getAvailableLocations() {
  return http.get('/entities/locations');
}

/**
 * 根据标签获取实体列表
 * @param tagId 标签ID
 * @param params 查询参数
 * @returns 分页结果
 */
export const getEntitiesByTag = (tagId: string, params: any): Promise<ResponseResult<PageResult<Entity>>> => {
  return http.get(`/entities/by-tag/${tagId}`, { params });
};

/**
 * 根据父级实体获取子实体列表
 * @param parentId 父级实体ID
 * @param params 查询参数
 * @returns 分页结果
 */
export const getEntitiesByParent = (parentId: string, params: any): Promise<ResponseResult<PageResult<Entity>>> => {
  return http.get(`/entities/list/by-parent`, { params: {...params, parentId} });
};


/**
 * 根据状态获取实体列表
 * @param status 实体状态
 * @param params 查询参数
 * @returns 分页结果
 */
export const getEntitiesByStatus = (status: string, params: any): Promise<ResponseResult<PageResult<Entity>>> => {
  return http.get(`/entities/list/by-status`, { params: {...params, status} });
};

/**
 * 获取即将到期的实体列表
 * @param params 查询参数
 * @returns 分页结果
 */
export function getExpiringEntities(params: { days: number, userId: number }) {
  return http.get('/entities/list/expiring', { params });
}

/**
 * 获取最近添加的实体列表
 * @param params 查询参数
 * @returns 分页结果
 */
export function getRecentEntities(params: { days: number, userId: number }) {
  return http.get('/entities/recent', { params });
}

/**
 * 获取实体统计数据
 * @returns 统计数据
 */
export function getEntitiesStats(userId: number) {
  return http.get('/entities/stats', { params: { userId } });
}

/**
 * 获取实体类型分布统计
 * @returns 类型分布数据
 */
export function getEntityTypeDistribution(userId: number) {
  return http.get('/entities/stat/by-type', { params: { userId } });
}

/**
 * 获取实体状态分布统计
 * @returns 状态分布数据
 */
export function getEntityStatusDistribution(userId: number) {
  return http.get('/entities/stat/by-status', { params: { userId } });
}

/**
 * 根据标签统计物品
 * @returns 标签统计数据
 */
export function getEntityTagDistribution(userId: number) {
  return http.get('/entities/stat/by-tag', { params: { userId } });
}

/**
 * 根据父实体统计子实体
 * @returns 父实体统计数据
 */
export function getEntityParentDistribution(userId: number) {
  return http.get('/entities/stat/by-parent', { params: { userId } });
}

/**
 * 根据使用频率统计物品
 * @returns 使用频率统计数据
 */
export function getEntityUsageDistribution(userId: number) {
  return http.get('/entities/stat/by-usage-frequency', { params: { userId } });
}

/**
 * 获取实体趋势数据（最近6个月）
 * @returns 趋势数据
 */
export function getEntityTrends(userId: number) {
  return http.get('/entities/trends', { params: { userId } });
}

/**
 * 统计物品总价值
 * @returns 总价值
 */
export function getEntitiesSumValue(userId: number) {
  return http.get('/entities/sum-value', { params: { userId } });
}
/**
 * 获取用户使用的物品列表
 * @returns 实体列表
 */
export const getEntitiesByUser = (userId: number): Promise<ResponseResult<Entity[]>> => {
  return http.get(`/entities/list/by-user`, { params: { userId} });
};
/**
 * 根据类型获取实体列表
 * @returns 实体列表
 */
export const listEntitiesByType = (type: string, userId: number): Promise<ResponseResult<Entity[]>> => {
  return http.get(`/entities/list/by-type`, { params: { type, userId } });
};

/**
 * 搜索实体
 * @returns 实体列表
 */
export function searchEntities(params: { keyword: string, userId: number }) {
  return http.get('/entities/search', { params });
}

/**
 * 高级搜索实体
 * @returns 实体列表
 */
export function advancedSearchEntities(params: { keyword: string, onlyAvailable: boolean, userId: number }) {
  return http.get('/entities/advanced-search', { params });
}