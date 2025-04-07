
<template>
  <div class="entity-container">
    <div class="entity-header">
      <h1 class="page-title">物品结构</h1>
      <div class="action-buttons">
        <el-button type="primary" @click="openAddEntityForm">
          <el-icon><Plus /></el-icon>添加物品
        </el-button>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧树形结构 -->
      <EntityTree 
        :loading="loading"
        :tree-data="treeData"
        @node-click="handleNodeClick"
      />
      
      <!-- 右侧详细信息 -->
      <div class="detail-container">
        <!-- 编辑或添加物品表单 -->
        <EntityForm 
          v-if="isEditing || isAdding"
          v-model="entityForm"
          :is-adding="isAdding"
          :saving="saving"
          @save="saveEntity"
          @cancel="cancelEditOrAdd"
        />
        
        <!-- 物品详情显示 -->
        <EntityDetail 
          v-else-if="currentEntity"
          :entity="currentEntity"
          @edit="openEditEntityForm"
          @delete="handleDelete"
        />
        
        <el-empty v-else description="请选择左侧物品查看详情" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useEntityCRUD } from './composables/useEntityCRUD'
import { useEntityForm } from './composables/useEntityForm'
import EntityTree from './components/EntityTree.vue'
import EntityDetail from './components/EntityDetail.vue'
import EntityForm from './components/EntityForm.vue'

defineOptions({
  // name 作为一种规范最好必须写上并且和路由的name保持一致
  name: "Entity"
});
// 使用实体CRUD相关逻辑
const {
  loading,
  saving,
  treeData,
  currentEntity,
  isEditing,
  isAdding,
  loadTreeData,
  handleNodeClick,
  handleDelete,
  openAddEntityForm,
  openEditEntityForm,
  cancelEditOrAdd,
  saveEntity
} = useEntityCRUD()

// 使用实体表单相关逻辑
const {
  entityForm,
  entityFormRef,
  resetForm,
  fillFormWithEntity
} = useEntityForm()

// 监听当前实体变化，如果是编辑状态则填充表单
watch(currentEntity, (newEntity) => {
  if (newEntity && isEditing.value) {
    fillFormWithEntity(newEntity)
  }
})

// 监听添加状态，如果添加则重置表单
watch(isAdding, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

// 在组件挂载时加载数据
onMounted(() => {
  loadTreeData()
})
</script>

<style scoped>
.entity-container {
  width: 100%;
  padding: 20px;
}

.entity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
}

.main-content {
  display: flex;
  gap: 20px;
  width: 100%;
}

.detail-container {
  flex: 1;
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }
}

@media screen and (max-width: 768px) {
  .entity-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style> 