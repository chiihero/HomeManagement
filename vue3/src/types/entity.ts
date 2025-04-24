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
  deletedImageIds?: string[];
  children?: Entity[];
  createTime?: string;
  updateTime?: string;
  location?: string;
  barcode?: string;
  qrcode?: string;
  
  // 药品特有字段
  activeIngredient?: string;        // 有效成分
  dosageForm?: string;              // 剂型
  batchNumber?: string;             // 批号
  usageDosage?: string;             // 用法用量
  approvalNumber?: string;          // 批准文号
  instructionText?: string;         // 说明书内容文本
  instructionImages?: string;       // 说明书图片URL列表，以逗号分隔
  instructionImagesList?: any[];    // 说明书图片列表对象

  // 耗材特有字段
  consumptionRate?: string;         // 消耗速率
  remainingQuantity?: number;       // 剩余数量
  unit?: string;                    // 单位
  replacementCycle?: number;        // 更换周期（天）
  lastReplacementDate?: string;     // 上次更换日期
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
  fileUrl: string;
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

