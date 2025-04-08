// 定义实体类型
export interface Entity {
  id?: string;
  name: string;
  type: "item" | "space"; // item-物品, space-空间
  code?: string;
  specification?: string;
  quantity?: number;
  price?: number;
  purchaseDate?: string;
  warrantyPeriod?: number;
  warrantyEndDate?: string;
  usageFrequency?: "daily" | "weekly" | "monthly" | "rarely";
  usageYears?: number;
  status?: "normal" | "damaged" | "discarded"; // normal-正常, damaged-损坏, discarded-丢弃
  description?: string;
  userId?: string;
  userName?: string;
  parentId?: string;
  parentName?: string;
  level?: number;
  path?: string;
  sort?: number;
  createUserId?: string;
  tags?: Tag[];
  tagIds?: string[];
  images?: EntityImage[];
  children?: Entity[];
  createTime?: string;
  updateTime?: string;
}

// 定义标签类型
export interface Tag {
  id: number;
  name: string;
  color: string;
  userId: number;
}

// 定义实体图片类型
export interface EntityImage {
  id: number;
  entityId: number;
  imagePath: string;
  mainImage: boolean;
  createdTime: string;
}

// 分页数据类型
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 响应类型
export interface ResponseResult<T> {
  code: number;
  msg: string;
  data: T;
}

// 物品状态枚举
export enum EntityStatus {
  AVAILABLE = "AVAILABLE", // 可用
  IN_USE = "IN_USE", // 使用中
  MAINTENANCE = "MAINTENANCE", // 维护中
  DISPOSED = "DISPOSED" // 已处置
}

// 附件类型
export type Attachment = {
  name: string;
  url: string;
};

// 物品表单数据类型
export type EntityFormData = {
  name: string;
  type: string;
  parentId: string | null;
  status: EntityStatus;
  location: string;
  price: number;
  purchaseDate: string;
  warrantyPeriod: number;
  description: string;
  tags: string[];
  images: string[];
  attachments: Attachment[];
};

// 物品查询参数类型
export type EntityQueryParams = {
  name?: string;
  type?: string;
  status?: EntityStatus;
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  startDate?: string;
  endDate?: string;
  tags?: string[];
  page: number;
  pageSize: number;
};

// 物品统计数据类型
export type EntityStats = {
  total: number;
  available: number;
  inUse: number;
  maintenance: number;
  disposed: number;
  totalValue: number;
  byType: {
    type: string;
    count: number;
  }[];
  byStatus: {
    status: EntityStatus;
    count: number;
  }[];
};

// 物品导入/导出类型
export type EntityImportExport = {
  name: string;
  type: string;
  parentName?: string;
  status: EntityStatus;
  location: string;
  price: number;
  purchaseDate: string;
  warrantyPeriod: number;
  description: string;
  tags: string;
  images: string;
  attachments: string;
};
