// 定义实体类型
export interface Entity {
  id?: string;
  name: string;
  type: 'item' | 'space'; // item-物品, space-空间
  code?: string;
  specification?: string;
  quantity?: number;
  price?: number;
  purchaseDate?: string;
  warrantyPeriod?: number;
  warrantyEndDate?: string;
  usageFrequency?: 'daily' | 'weekly' | 'monthly' | 'rarely';
  usageYears?: number;
  status?: 'normal' | 'damaged' | 'discarded'; // normal-正常, damaged-损坏, discarded-丢弃
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