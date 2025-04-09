<template>
  <div class="w-full">
    <el-skeleton v-if="loading" :rows="10" animated />
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
          <el-descriptions-item label="位置">{{
            entity.location
          }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{
            formatPrice(entity.price)
          }}</el-descriptions-item>
          <el-descriptions-item label="购买日期">{{
            formatDate(entity.purchaseDate)
          }}</el-descriptions-item>
          <el-descriptions-item label="保修期"
            >{{ entity.warrantyPeriod }}个月</el-descriptions-item
          >
          <el-descriptions-item label="父级物品">{{
            entity.parentId ? getParentName(entity.parentId) : "无"
          }}</el-descriptions-item>
        </el-descriptions>
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
          <el-tag v-for="tag in entity.tags" :key="tag">
            {{ tag }}
          </el-tag>
          <el-empty
            v-if="!entity.tags?.length"
            description="暂无标签"
            :image-size="60"
          />
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
            v-for="image in entity.images"
            :key="image.id"
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
import { ref, computed, onMounted, watch } from "vue";
import { Document, Edit, Delete } from "@element-plus/icons-vue";
import type { Entity } from "@/types/entity";
import { useEntityDetail } from "../composables/useEntityDetail";

interface Props {
  loading: boolean;
  entity: Entity | null;
  treeData: Entity[];
}

const props = defineProps<Props>();

// 使用实体详情composable
const {
  imageUrlCache,
  loadAllImages,
  getParentName,
  getStatusType,
  getStatusText,
  formatPrice,
  formatDate,
  getPreviewImageUrls
} = useEntityDetail();

// 预览图片URL列表
const previewImageUrls = computed(() => {
  return getPreviewImageUrls(props.entity);
});

// 监听实体变化，加载图片
watch(() => props.entity, (newEntity) => {
  if (newEntity) {
    loadAllImages(newEntity);
  }
}, { immediate: true });

// 组件挂载时加载图片
onMounted(() => {
  if (props.entity) {
    loadAllImages(props.entity);
  }
});
</script>
