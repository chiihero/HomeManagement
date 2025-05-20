<template>
  <div class="w-full">
    <el-skeleton v-if="loading" :rows="1" 
    :throttle="{ leading: 500, trailing: 500 }"
    animated />
    <el-empty v-else-if="!entity" description="请选择物品" />
    <div v-else class="space-y-6">
      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3
            class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full"
          >
            基本信息
          </h3>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{
            entity.name
          }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{
            entity.type
          }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(entity.status)">
              {{ getStatusText(entity.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数量">{{
            entity.quantity
          }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{
            formatPrice(entity.price)
          }}</el-descriptions-item>
          <el-descriptions-item label="购买日期">{{
            formatDate(entity.purchaseDate)
          }}</el-descriptions-item>
          <el-descriptions-item label="生产日期">{{
            formatDate(entity.productionDate)
          }}</el-descriptions-item>
          <el-descriptions-item label="保修期"
            >{{ entity.warrantyPeriod }}个月
          </el-descriptions-item>
          <el-descriptions-item label="保修截止日期">{{
            formatDate(entity.warrantyEndDate)
          }}</el-descriptions-item>
          <el-descriptions-item label="父级物品">{{
            entity.parentId ? getParentName(entity.parentId, treeData) : "无"
          }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 二维码和条形码 -->
      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3
            class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full"
          >
            标识码
          </h3>
        </div>
        <div class="flex flex-wrap gap-6">
          <div class="flex flex-col items-center">
            <span class="mb-2 text-sm text-gray-600">二维码</span>
            <ReQrcode v-if="entity" :text="getQrcodeValue()" :size="160" class="qrcode" />
          </div>
        </div>
        <div class="mt-3">
          <el-button type="success" size="small" @click="downloadQrCode">下载二维码</el-button>
        </div>
      </div>

      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3
            class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full"
          >
            描述
          </h3>
        </div>
        <p class="text-gray-600 whitespace-pre-line">
          {{ entity.description || "暂无描述" }}
        </p>
      </div>

      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3
            class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full"
          >
            标签
          </h3>
        </div>
        <div class="flex flex-wrap gap-2">
          <el-tag
            v-for="(tag, index) in entity.tags"
            :key="index"
            :color="tag.color"
            :style="{ color: getContrastColor(tag.color) }"
          >
            {{ tag.name }}
          </el-tag>
          <p v-if="!entity.tags?.length" class="text-gray-600 whitespace-pre-line">暂无标签</p>
        </div>
      </div>

      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3
            class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full"
          >
            图片
          </h3>
        </div>
        <div class="flex flex-wrap gap-4">
          <el-image
            v-for="(image, index) in entity.images"
            :key="index"
            :src="imageUrlCache[image.id]"
            :preview-src-list="previewImageUrls"
            fit="cover"
            class="w-28 h-28 rounded-md object-cover"
          />
          <el-empty
            v-if="!entity.images?.length"
            description="暂无图片"
            :image-size="60"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, onMounted, ref } from "vue";
import type { Entity } from "@/types/entity";
import { useEntityDetail } from "../composables/useEntityDetail";
import { getContrastColor  } from "@/utils/color";
import { formatDate } from "@/utils/date";
import ReQrcode from "@/components/ReQrcode";
import { ElMessage } from "element-plus";

interface Props {
  loading: boolean;
  entity: Entity | null;
  treeData: Entity[];
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "edit"): void;
  (e: "delete"): void;
}>();

// 使用实体详情composable
const {
  imageUrlCache,
  loadAllImages,
  getParentName,
  getStatusType,
  getStatusText,
  formatPrice,
  getPreviewImageUrls
} = useEntityDetail();



// 预览图片URL列表
const previewImageUrls = computed(() => {
  return getPreviewImageUrls(props.entity);
});

// 监听实体变化，加载图片
watch(
  () => props.entity,
  newEntity => {
    if (newEntity) {
      loadAllImages(newEntity);
    }
  },
  { immediate: true }
);

// 组件挂载时加载图片
onMounted(() => {
  if (props.entity) {
    loadAllImages(props.entity);
  }
});

// 生成二维码内容
const getQrcodeValue = () => {
  if (!props.entity) return "";
  return JSON.stringify({
    id: props.entity.id,
    name: props.entity.name,
    type: props.entity.type,
  });
};



// 下载二维码
const downloadQrCode = () => {
  if (!props.entity) return;
  
  const qrcodeComponent = document.querySelector('.qrcode');
  if (!qrcodeComponent) {
    ElMessage.error('找不到二维码元素');
    return;
  }
  
  // 获取二维码内的canvas或svg元素
  // const qrcodeElement = qrcodeComponent.querySelector('.qrcode, canvas, svg') as HTMLElement;
  const qrcodeElement = qrcodeComponent as HTMLElement;
  if (!qrcodeElement) {
    ElMessage.error('找不到二维码图像元素');
    return;
  }
  
  try {
    if (qrcodeElement instanceof HTMLCanvasElement) {
      // 如果是Canvas元素直接导出
      const link = document.createElement('a');
      link.download = `${props.entity.name}_QR码.png`;
      link.href = qrcodeElement.toDataURL('image/png');
      link.click();
    } else {
      // 如果是其他元素，可能需要其他处理方式
      ElMessage.info('不支持的二维码元素类型');
    }
  } catch (error) {
    console.error('下载二维码失败:', error);
    ElMessage.error('下载二维码失败');
  }
};

</script>
