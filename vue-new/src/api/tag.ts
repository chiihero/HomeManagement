import http from './http';
import type { Tag } from '@/types/entity';
import type { PageResult, ResponseResult } from '@/types/common';

/**
 * 分页查询标签
 * @param params 查询参数
 * @returns 分页查询结果
 */
export const getTags = (params: any): Promise<ResponseResult<PageResult<Tag>>> => {
  console.log('调用getTags API，参数:', params);
  return http.get('/tags', { params }).then(response => {
    console.log('getTags响应结果:', response);
    return response;
  }).catch(error => {
    console.error('getTags请求失败:', error);
    throw error;
  });
};

/**
 * 获取所有标签
 * @returns 标签列表
 */
export const getAllTags = (userId: number): Promise<ResponseResult<Tag[]>> => {
  return http.get('/tags', { params: { userId } });
};

/**
 * 获取标签详情
 * @param id 标签ID
 * @returns 标签详情
 */
export const getTagDetail = (id: string): Promise<ResponseResult<Tag>> => {
  return http.get(`/tags/${id}`);
};

/**
 * 创建标签
 * @param tagData 标签数据
 * @returns 创建后的标签
 */
export const createTag = (tagData: Omit<Tag, 'id'>): Promise<ResponseResult<Tag>> => {
  return http.post('/tags', tagData);
};

/**
 * 更新标签
 * @param id 标签ID
 * @param tagData 标签数据
 * @returns 更新后的标签
 */
export const updateTag = (id: string, tagData: Partial<Tag>): Promise<ResponseResult<Tag>> => {
  return http.put(`/tags/${id}`, tagData);
};

/**
 * 删除标签
 * @param id 标签ID
 * @returns 删除结果
 */
export const deleteTag = (id: string): Promise<ResponseResult<null>> => {
  return http.delete(`/tags/${id}`);
};

/**
 * 批量删除标签
 * @param ids 标签ID数组
 * @returns 删除结果
 */
export const batchDeleteTags = (ids: string[]): Promise<ResponseResult<null>> => {
  return http.post('/tags/batch-delete', { ids });
};

/**
 * 获取标签使用统计
 * @returns 标签使用统计
 */
export const getTagUsageStats = (userId: number): Promise<ResponseResult<any[]>> => {
  return http.get('/tags/usage-stats', { params: { userId } });
};

/**
 * 获取物品的标签
 * @param itemId 物品ID 
 * @returns 标签列表
 */
export const getItemTags = (itemId: string): Promise<ResponseResult<Tag[]>> => {
  return http.get(`/tags/item/${itemId}`);
};

/**
 * 设置物品的标签
 * @param itemId 物品ID
 * @param tagIds 标签ID数组
 * @returns 设置结果
 */
export const setItemTags = (itemId: string, tagIds: string[]): Promise<ResponseResult<boolean>> => {
  return http.post(`/tags/item/${itemId}`, tagIds);
}; 