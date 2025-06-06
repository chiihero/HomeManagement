import { http } from "@/utils/http";
import type {
  Entity,
  EntityQueryParams,
} from "@/types/entity";
import type { ResponseResult } from "@/types/http";

// 获取物品列表
export const getEntities = (params: EntityQueryParams) => {
  return http.get<
    ResponseResult<{
      total: number;
      list: Entity[];
    }>,EntityQueryParams
  >("/entities", { params });
};
/**
 * 分页查询实体
 * @param params 查询参数
 */
export function pageEntities(params: any) {
  return http.get("/entities/page", { params });
}

// 获取物品树
export const getRecentEntities = (days: number ,userId: string) => {
  return http.get<ResponseResult<Entity[]>,object>("/entities/recent", {
    params: { days,userId }
  });
};
// 获取物品树
export const getEntityTree = (userId: string) => {
  return http.get<ResponseResult<Entity[]>, number>("/entities/tree", {
    params: { userId }
  });
};
/**
 * 获取用户使用的物品列表
 * @returns 实体列表
 */
export const getEntitiesByUser = (userId: string) => {
  return http.get<ResponseResult<Entity[]>,string>(`/entities/list/by-user`, { params: { userId } });
};
// 获取物品详情
export const getEntity = (id: string) => {
  return http.get<ResponseResult<Entity>,string>(`/entities/${id}`);
};

// 创建物品
export const createEntity = (data: Entity) => {
  return http.post<ResponseResult<Entity>, Entity>("/entities", {data:data});
};

// 更新物品
export const updateEntity = (id: string, data: Entity) => {
  return http.put<ResponseResult<Entity>,Entity >(`/entities/${id}`,{data: data});
};

// 删除物品
export const deleteEntity = (id: string) => {
  return http.delete<ResponseResult<void>,string>(`/entities/${id}`);
};


// 获取所有类型
export const getAllTypes = () => {
  return http.get<ResponseResult<string[]>,void>("/entities/types");
};

// 获取所有位置
export const getAllLocations = () => {
  return http.get<ResponseResult<string[]>,void>("/entities/locations");
};



// 搜索物品
export const searchEntities = (userId: string, keyword: string) => {
  return http.get<ResponseResult<Entity[]>,object>("/entities/search", {
    params: { userId, keyword }
  });
};

// 获取物品历史记录
export const getEntityHistory = (id: string) => {
  return http.get<
    ResponseResult<
      {
        id: string;
        entityId: string;
        action: string;
        changes: Record<string, any>;
        operator: string;
        createdAt: string;
      }[]
    >,void
  >(`/entities/${id}/history`);
};

// 恢复物品历史版本
export const restoreEntityVersion = (id: string, historyId: string) => {
  return http.post<ResponseResult<Entity>,void>(
    `/entities/${id}/restore/${historyId}`
  );
};

// 根据条形码查询物品
export const getEntityByBarcode = (barcode: string, userId: string) => {
  return http.get<ResponseResult<Entity>,{ barcode: string; userId: string }>(`/entities/by-barcode`, { params: { barcode, userId } });
};

// 根据二维码查询物品
export const getEntityByQRCode = (qrcode: string, userId: string) => {
  return http.get<ResponseResult<Entity>,{ qrcode: string; userId: string }>(`/entities/by-qrcode`, { params: { qrcode, userId } });
};
