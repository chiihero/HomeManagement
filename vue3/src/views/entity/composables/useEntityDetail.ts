import { ref } from "vue";
import type { Entity } from "@/types/entity";

export function useEntityDetail() {
  // 图片URL缓存
  const imageUrlCache = ref<Record<string, string>>({});

  // 加载所有图片
  const loadAllImages = async (entity: Entity | null) => {
    if (!entity?.images) return;

    for (const image of entity.images) {
      if (image.id && image.imageUrl) {
        try {
          // 直接使用后端返回的图片URL
            // 如果是相对路径，添加基础URL
            const baseUrl = import.meta.env.VITE_API_URL || "";
            const url = image.imageUrl.startsWith("http") 
              ? image.imageUrl 
              : `${baseUrl}${image.imageUrl}`;
            
            imageUrlCache.value[image.id] = url;
        } catch (error) {
          console.error("获取图片数据失败:", error);
        }
      }
    }
  };
  // 预览图片URL列表
  const getPreviewImageUrls = (entity: Entity | null) => {
    if (!entity?.images) return [];
    return entity.images
      .map(img => {
        if (img.id && imageUrlCache.value[img.id]) {
          return imageUrlCache.value[img.id];
        }
        return "";
      })
      .filter(url => url !== "");
  };

  // 获取父级物品名称
  const getParentName = (parentId: string, treeData: Entity[]) => {
    const findParent = (items: Entity[]): string | null => {
      for (const item of items) {
        if (item.id === parentId) return item.name;
        if (item.children?.length) {
          const found = findParent(item.children);
          if (found) return found;
        }
      }
      return null;
    };
    if (parentId=="0") return "根空间";
    return findParent(treeData) || "未知";
  };

  // 获取状态类型
  const getStatusType = (status: string) => {
    const types: Record<string, string> = {
      normal: "success",
      damaged: "primary",
      discarded: "warning",
      expired: "info",
      lent: "info"
    };
    return types[status] || "info";
  };

  // 获取状态文本
  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
      normal: "正常",
      damaged: "损坏",
      discarded: "丢弃",
      expired: "过期",
      lent: "借出"
    };
    return texts[status] || "未知状态";
  };

  // 格式化价格
  const formatPrice = (price: number) => {
    if (!price && price !== 0) return "¥0.00";
    return `¥${price.toFixed(2)}`;
  };


  
  return {
    imageUrlCache,
    loadAllImages,
    getParentName,
    getStatusType,
    getStatusText,
    formatPrice,
    getPreviewImageUrls,
  };
}
