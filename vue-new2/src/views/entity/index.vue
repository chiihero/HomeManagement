<template>
  <div class="entity-container bg-gray-50 min-h-screen p-4 md:p-6">
    <el-card class="header-card mb-6 border-0 shadow-sm">
      <div
        class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
      >
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0">
          <el-icon class="mr-2 text-primary"><Folder /></el-icon>物品结构管理
        </h1>
        <div class="flex items-center gap-2">
          <el-button
            type="primary"
            class="flex items-center gap-2"
            @click="openAddEntityForm"
          >
            <el-icon><Plus /></el-icon>添加物品
          </el-button>
          <el-button
            :icon="Refresh"
            circle
            :loading="loading"
            @click="loadTreeData"
          />
        </div>
      </div>
    </el-card>

    <!-- 主要内容区域 -->
    <div class="flex flex-col lg:flex-row gap-6">
      <!-- 左侧树形结构 -->
      <el-card class="tree-wrapper border-0 shadow-sm w-full lg:w-72 shrink-0">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-gray-700 font-medium">分类结构</span>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索物品"
              class="w-32 md:w-40"
              size="small"
              clearable
              :prefix-icon="Search"
            />
          </div>
        </template>
        <EntityTree
          :loading="loading"
          :tree-data="filteredTreeData"
          class="entity-tree"
          @node-click="handleNodeClick"
        />
      </el-card>

      <!-- 右侧详细信息 -->
      <el-card class="detail-container grow border-0 shadow-sm">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-gray-700 font-medium">
              {{ getDetailTitle }}
            </span>
            <el-tooltip
              v-if="currentEntity && !isEditing && !isAdding"
              effect="dark"
              content="刷新"
              placement="top"
            >
              <el-button
                :icon="Refresh"
                circle
                plain
                size="small"
                @click="refreshCurrentEntity"
              />
            </el-tooltip>
          </div>
        </template>

        <!-- 编辑或添加物品表单 -->
        <EntityForm
          v-if="isEditing || isAdding"
          :entity="currentEntity || null"
          :tree-data="treeData"
          :existing-tags="[]"
          :is-editing="isEditing"
          :saving="saving"
          @cancel="cancelEditOrAdd"
          @submit="saveEntity"
        />

        <!-- 物品详情显示 -->
        <EntityDetail
          v-else-if="currentEntity"
          :loading="false"
          :entity="currentEntity"
          :tree-data="treeData"
          @edit="openEditEntityForm"
          @delete="() => handleDelete(currentEntity)"
        />

        <el-empty v-else description="请选择左侧物品查看详情" :image-size="120">
          <template #image>
            <el-icon size="60" class="text-gray-300"><Select /></el-icon>
          </template>
        </el-empty>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue";
import { Plus, Folder, Refresh, Search, Select } from "@element-plus/icons-vue";
import { useEntityCRUD } from "./composables/useEntityCRUD";
import { useEntityForm } from "./composables/useEntityForm";
import EntityTree from "./components/EntityTree.vue";
import EntityDetail from "./components/EntityDetail.vue";
import EntityForm from "./components/EntityForm.vue";
import type { Entity } from "@/types/entity";

defineOptions({
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
} = useEntityCRUD();

// 使用实体表单相关逻辑
const { entityForm, entityFormRef, resetForm, fillFormWithEntity } =
  useEntityForm();

// 搜索关键词
const searchKeyword = ref("");

// 过滤后的树形数据
const filteredTreeData = computed(() => {
  if (!searchKeyword.value.trim()) return treeData.value;

  // 递归搜索树形数据
  const filterTree = (nodes: Entity[]) => {
    return nodes.filter(node => {
      // 检查当前节点是否匹配
      const isMatch = node.name
        .toLowerCase()
        .includes(searchKeyword.value.toLowerCase());

      // 如果有子节点，递归过滤
      if (node.children && node.children.length > 0) {
        const filteredChildren = filterTree(node.children);
        node.children = filteredChildren;
        // 如果子节点匹配，父节点也要显示
        return isMatch || filteredChildren.length > 0;
      }

      return isMatch;
    });
  };

  return filterTree([...treeData.value]);
});

// 动态获取详情标题
const getDetailTitle = computed(() => {
  if (isAdding.value) return "添加新物品";
  if (isEditing.value) return `编辑物品: ${currentEntity.value?.name || ""}`;
  return currentEntity.value
    ? `物品详情: ${currentEntity.value.name}`
    : "物品详情";
});

// 刷新当前实体
const refreshCurrentEntity = () => {
  if (currentEntity.value && currentEntity.value.id) {
    // 这里可以添加刷新单个实体的逻辑
    loadTreeData();
  }
};

// 监听当前实体变化，如果是编辑状态则填充表单
watch(currentEntity, newEntity => {
  if (newEntity && isEditing.value) {
    fillFormWithEntity(newEntity);
  }
});

// 监听添加状态，如果添加则重置表单
watch(isAdding, newVal => {
  if (newVal) {
    resetForm();
  }
});

// 在组件挂载时加载数据
onMounted(() => {
  loadTreeData();
});
</script>

<style scoped>
.entity-container {
  width: 100%;
}

.header-card {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
}

.entity-tree {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding-right: 4px;
}

.entity-tree::-webkit-scrollbar {
  width: 4px;
}

.entity-tree::-webkit-scrollbar-thumb {
  background-color: #e4e4e7;
  border-radius: 4px;
}

.entity-tree::-webkit-scrollbar-track {
  background-color: #f8fafc;
}

.tree-wrapper,
.detail-container {
  background-color: white;
  transition: all 0.3s ease;
}

.tree-wrapper:hover,
.detail-container:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 为Element Plus卡片标题添加样式 */
:deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

/* 允许内容区域滚动 */
.detail-container :deep(.el-card__body) {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}
</style>
