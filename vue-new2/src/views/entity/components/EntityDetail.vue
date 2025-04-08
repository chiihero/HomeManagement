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
            :key="image"
            :src="image"
            :preview-src-list="entity.images"
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

      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3
            class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full"
          >
            附件
          </h3>
        </div>
        <div class="flex flex-col gap-2">
          <el-link
            v-for="attachment in entity.attachments"
            :key="attachment.name"
            type="primary"
            :href="attachment.url"
            target="_blank"
            class="flex items-center"
          >
            <el-icon class="mr-2"><Document /></el-icon>
            {{ attachment.name }}
          </el-link>
          <el-empty
            v-if="!entity.attachments?.length"
            description="暂无附件"
            :image-size="60"
          />
        </div>
      </div>

      <div class="flex justify-end gap-2 mt-8">
        <el-button type="primary" @click="handleEdit">
          <el-icon class="mr-1"><Edit /></el-icon>编辑
        </el-button>
        <el-button type="danger" @click="handleDelete">
          <el-icon class="mr-1"><Delete /></el-icon>删除
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { Document, Edit, Delete } from "@element-plus/icons-vue";
import type { Entity } from "@/types/entity";

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

// 获取父级物品名称
const getParentName = (parentId: string) => {
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
  return findParent(props.treeData) || "未知";
};

// 获取状态类型
const getStatusType = (status: string) => {
  const types = {
    AVAILABLE: "success",
    IN_USE: "primary",
    MAINTENANCE: "warning",
    DISPOSED: "info"
  };
  return types[status] || "info";
};

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    AVAILABLE: "可用",
    IN_USE: "使用中",
    MAINTENANCE: "维护中",
    DISPOSED: "已处置"
  };
  return texts[status] || "未知状态";
};

// 格式化价格
const formatPrice = (price: number) => {
  return `¥${price.toFixed(2)}`;
};

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString();
};

// 处理编辑
const handleEdit = () => {
  emit("edit");
};

// 处理删除
const handleDelete = () => {
  emit("delete");
};
</script>
