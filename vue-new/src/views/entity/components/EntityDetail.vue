<template>
  <el-card class="detail-card">
    <template #header>
      <div class="card-header">
        <span>物品详情</span>
        <div class="header-actions">
          <el-button type="primary" link @click="$emit('edit', entity)">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-popconfirm 
            title="确定删除此物品吗？" 
            @confirm="$emit('delete', entity)"
            confirm-button-text="确定"
            cancel-button-text="取消"
          >
            <template #reference>
              <el-button type="danger" link>
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </template>
    
    <div class="entity-detail">
      <div class="detail-section">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ entity.name }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="entity.type === 'item' ? 'success' : 'primary'">
              {{ entity.type === 'item' ? '物品' : '空间' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="规格">{{ entity.specification || '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{ entity.price ? '¥' + entity.price.toFixed(2) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(entity.status)">
              {{ getStatusText(entity.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="使用频率">{{ getUsageFrequencyText(entity.usageFrequency) }}</el-descriptions-item>
          <el-descriptions-item label="购买日期">{{ entity.purchaseDate ? formatDate(entity.purchaseDate) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ entity.expirationDate ? formatDate(entity.expirationDate) : '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="detail-section">
        <h3>标签</h3>
        <div class="tags-container">
          <el-tag 
            v-for="tag in entity.tags" 
            :key="tag.id" 
            :style="{ backgroundColor: tag.color, color: getContrastColor(tag.color) }"
            class="tag-item"
          >
            {{ tag.name }}
          </el-tag>
          <span v-if="!entity.tags || entity.tags.length === 0">暂无标签</span>
        </div>
      </div>
      
      <div class="detail-section">
        <h3>描述</h3>
        <p class="description">{{ entity.description || '暂无描述' }}</p>
      </div>
      
      <div class="detail-section" v-if="entity.images && entity.images.length > 0">
        <h3>图片</h3>
        <el-carousel height="200px" indicator-position="outside">
          <el-carousel-item v-for="(image, index) in entity.images" :key="index">
            <el-image 
              :src="image.imagePath" 
              fit="contain"
              class="detail-image"
            />
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>
  </el-card>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Edit, Delete } from '@element-plus/icons-vue';
import { useEntityForm } from '../composables/useEntityForm';

export default defineComponent({
  name: 'EntityDetail',
  components: {
    Edit,
    Delete
  },
  props: {
    entity: {
      type: Object,
      required: true
    }
  },
  emits: ['edit', 'delete'],
  setup() {
    const { 
      formatDate, 
      getStatusType, 
      getStatusText, 
      getUsageFrequencyText, 
      getContrastColor 
    } = useEntityForm();

    return {
      formatDate,
      getStatusType,
      getStatusText,
      getUsageFrequencyText,
      getContrastColor
    };
  }
});
</script>

<style scoped>
.detail-card {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.entity-detail {
  padding: 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin-right: 4px;
  margin-bottom: 4px;
}

.description {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.detail-image {
  width: 100%;
  height: 100%;
}

@media screen and (max-width: 768px) {
  .detail-card .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-card .header-actions {
    margin-top: 10px;
  }
}
</style> 