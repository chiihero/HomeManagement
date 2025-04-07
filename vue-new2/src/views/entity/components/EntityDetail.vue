<template>
  <div class="entity-detail">
    <el-card shadow="hover" class="h-full">
      <template #header>
        <div class="flex justify-between items-center">
          <span>物品详情</span>
          <div class="actions">
            <el-button type="primary" @click="handleEdit">编辑</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </template>

      <el-skeleton :rows="10" animated v-if="loading" />
      <el-empty v-else-if="!entity" description="请选择物品" />
      <div v-else class="detail-content">
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="名称">{{ entity.name }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ entity.type }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(entity.status)">
                {{ getStatusText(entity.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="位置">{{ entity.location }}</el-descriptions-item>
            <el-descriptions-item label="价格">{{ formatPrice(entity.price) }}</el-descriptions-item>
            <el-descriptions-item label="购买日期">{{ formatDate(entity.purchaseDate) }}</el-descriptions-item>
            <el-descriptions-item label="保修期">{{ entity.warrantyPeriod }}个月</el-descriptions-item>
            <el-descriptions-item label="父级物品">{{ entity.parentId ? getParentName(entity.parentId) : '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h3 class="section-title">描述</h3>
          <div class="description">{{ entity.description || '暂无描述' }}</div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">标签</h3>
          <div class="tags">
            <el-tag
              v-for="tag in entity.tags"
              :key="tag"
              class="mr-1"
            >
              {{ tag }}
            </el-tag>
            <el-empty v-if="!entity.tags?.length" description="暂无标签" />
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">图片</h3>
          <div class="images">
            <el-image
              v-for="image in entity.images"
              :key="image"
              :src="image"
              :preview-src-list="entity.images"
              fit="cover"
              class="image-item"
            />
            <el-empty v-if="!entity.images?.length" description="暂无图片" />
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">附件</h3>
          <div class="attachments">
            <el-link
              v-for="attachment in entity.attachments"
              :key="attachment.name"
              type="primary"
              :href="attachment.url"
              target="_blank"
              class="attachment-item"
            >
              <el-icon class="mr-1"><Document /></el-icon>
              {{ attachment.name }}
            </el-link>
            <el-empty v-if="!entity.attachments?.length" description="暂无附件" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import type { Entity } from '@/types/entity'

interface Props {
  loading: boolean
  entity: Entity | null
  treeData: Entity[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'edit'): void
  (e: 'delete'): void
}>()

// 获取父级物品名称
const getParentName = (parentId: string) => {
  const findParent = (items: Entity[]): string | null => {
    for (const item of items) {
      if (item.id === parentId) return item.name
      if (item.children?.length) {
        const found = findParent(item.children)
        if (found) return found
      }
    }
    return null
  }
  return findParent(props.treeData) || '未知'
}

// 获取状态类型
const getStatusType = (status: string) => {
  const types = {
    AVAILABLE: 'success',
    IN_USE: 'primary',
    MAINTENANCE: 'warning',
    DISPOSED: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts = {
    AVAILABLE: '可用',
    IN_USE: '使用中',
    MAINTENANCE: '维护中',
    DISPOSED: '已处置'
  }
  return texts[status] || '未知状态'
}

// 格式化价格
const formatPrice = (price: number) => {
  return `¥${price.toFixed(2)}`
}

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString()
}

// 处理编辑
const handleEdit = () => {
  emit('edit')
}

// 处理删除
const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.entity-detail {
  flex: 1;
  height: 100%;
  margin-left: 16px;
}

.h-full {
  height: 100%;
}

.detail-content {
  padding: 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #303133;
}

.description {
  line-height: 1.6;
  color: #606266;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
}

.attachments {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
}

.mr-1 {
  margin-right: 4px;
}
</style> 