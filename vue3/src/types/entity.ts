// 定义实体类型
export interface Entity {
  id?: string;
  name: string;
  type: string;
  code?: string;
  specification?: string;
  quantity?: number;
  price?: number;
  purchaseDate?: string;
  productionDate?: string;
  warrantyPeriod?: number;
  warrantyEndDate?: string;
  usageFrequency?: "daily" | "weekly" | "monthly" | "rarely";
  usageYears?: number;
  status?: EntityStatus; // normal-正常, damaged-损坏, discarded-丢弃，expired-过期，lent-借出
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
  images?: EntityImage[] | { file: File; url: string }[] | any[];
  children?: Entity[];
  createTime?: string;
  updateTime?: string;
  location?: string;
}

// 定义标签类型
export interface Tag {
  id: number;
  name: string;
  color: string;
  userId: string;
}

// 定义实体图片类型
export interface EntityImage {
  id: number;
  entityId: number;
  imageUrl: string;
  imageType: string;
  contentType: string;
  fileName: string;
  fileSize: string;
  sortOrder: number;
  createdTime: string;
}

// 物品状态枚举
export enum EntityStatus {
  normal = "normal",     // 正常
  damaged = "damaged",   // 损坏
  discarded = "discarded", // 丢弃
  expired = "expired",   // 过期
  lent = "lent"          // 借出
}

// 物品查询参数类型
export type EntityQueryParams = {
  name?: string;
  type?: string;
  status?: EntityStatus;
  minPrice?: number;
  maxPrice?: number;
  startDate?: string;
  endDate?: string;
  tags?: Tag[];
  page: number;
  pageSize: number;
};

