import { ref } from "vue";
import type { Entity } from "@/types/entity";
import { getImageData } from "@/api/image";

export function useEntityDetail() {
  // 图片URL缓存
  const imageUrlCache = ref<Record<string, string>>({});

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

  // 计算对比色，确保文字在背景色上可见
  const getContrastColor = (hexColor: string) => {
    // 如果没有颜色或颜色格式不正确，默认返回黑色
    if (!hexColor || !hexColor.startsWith("#")) {
      return "#000000";
    }

    // 移除#前缀并处理不同格式的颜色（#RGB和#RRGGBB）
    let hex = hexColor.slice(1);
    if (hex.length === 3) {
      hex = hex
        .split("")
        .map(x => x + x)
        .join("");
    }

    // 转换为RGB
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);

    // 计算亮度 (YIQ方程式)
    const yiq = (r * 299 + g * 587 + b * 114) / 1000;

    // 根据亮度返回黑色或白色
    return yiq >= 150 ? "#000000" : "#ffffff";
  };

  return {
    imageUrlCache,
    loadAllImages,
    getParentName,
    getStatusType,
    getStatusText,
    formatPrice,
    formatDate,
    getPreviewImageUrls,
    getContrastColor
  };
}
