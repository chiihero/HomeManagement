import { ref } from "vue";
import type { Entity } from "@/types/entity";
import { EntityStatus } from "@/types/entity";
import { getImageData } from "@/api/image";

export function useEntityDetail() {
  // 图片URL缓存
  const imageUrlCache = ref<Record<string, string>>({});
  const currentEntity = ref<Entity>(null);

  // 获取图片URL
  // const getImageUrl = async (imageId: string) => {
  //   if (imageUrlCache.value[imageId]) {
  //     return imageUrlCache.value[imageId];
  //   }

  //   try {
  //     const blob = await getImageData(imageId);
  //     const url = URL.createObjectURL(blob);
  //     imageUrlCache.value[imageId] = url;
  //     return url;
  //   } catch (error) {
  //     console.error("获取图片数据失败:", error);
  //     return "";
  //   }
  // };

  // 加载所有图片
  const loadAllImages = async (entity: Entity | null) => {
    if (!entity?.images) return;

    for (const image of entity.images) {
      if (image.id) {
        try {
          const blob = await getImageData(image.id.toString());
          const url = URL.createObjectURL(blob);
          imageUrlCache.value[image.id] = url;
          image.imageUrl = url;
          return url;
        } catch (error) {
          console.error("获取图片数据失败:", error);
          return "";
        }
      }
    }
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
    return findParent(treeData) || "未知";
  };

  // 获取状态类型
  const getStatusType = (status: string) => {
    const types = {
      normal: "success",
      damaged: "primary",
      discarded: "warning",
      expired: "info"
    };
    return types[status] || "info";
  };

  // 获取状态文本
  const getStatusText = (status: string) => {
    const texts = {
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
    return `¥${price}`;
  };

  // 格式化日期
  const formatDate = (date: string) => {
    if (!date) {
      return "无";
    }
    return new Date(date).toLocaleDateString();
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

  return {
    imageUrlCache,
    // getImageUrl,
    loadAllImages,
    getParentName,
    getStatusType,
    getStatusText,
    formatPrice,
    formatDate,
    getPreviewImageUrls
  };
}
