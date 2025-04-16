<template>
  <div class="bg-gray-50 min-h-screen p-4 md:p-6">
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
          <el-button type="primary" :loading="treeLoading" @click="loadTreeData"
            ><el-icon><Refresh /></el-icon>刷新树结构</el-button
          >
        </div>
      </div>
    </el-card>

    <!-- 主要内容区域 -->
    <div class="flex flex-col lg:flex-row gap-6">
      <!-- 左侧树形结构 -->
      <el-card class="tree-wrapper border-0 shadow-sm w-full lg:w-72 shrink-0">
        <template #header>
          <div class="flex  flex-col items-center justify-between">
            <span class="text-gray-700  font-bold">分类物品树</span>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索物品"
              class="w-32 md:w-40"
              clearable
              :prefix-icon="Search"
            />
          </div>
        </template>
        <EntityTree
          :loading="treeLoading"
          :tree-data="filteredTreeData"
          class="entity-tree"
          @node-click="handleNodeClick"
        />
      </el-card>

      <!-- 右侧详细信息 -->
      <el-card class="detail-container grow border-0 shadow-sm">
        <template #header>
          <div class="flex flex-col items-center justify-between">
            <span class="text-gray-700 font-bold ">
              {{ getDetailTitle }}
            </span>
            <div v-if="!isEditing && currentEntity" class="flex gap-2 mt-1">
              <el-button type="primary" @click="openEditEntityForm">
                <el-icon class="mr-1"><Edit /></el-icon>编辑
              </el-button>
              <el-button type="danger" @click="handleDelete(currentEntity)">
                <el-icon class="mr-1"><Delete /></el-icon>删除
              </el-button>
              <el-tooltip effect="dark" content="刷新" placement="top">
                <el-button
                  type="primary"
                  :loading="loading"
                  @click="refreshCurrentEntity"
                  ><el-icon><Refresh /></el-icon>刷新</el-button
                >
              </el-tooltip>
            </div>
          </div>
        </template>

        <!-- 编辑或添加物品表单 -->
        <EntityForm
          v-if="isEditing || isAdding"
          :entity="currentEntity || null"
          :tree-data="treeData"
          :existing-tags="entityTags"
          :is-editing="isEditing"
          :saving="saving"
          @cancel="cancelEditOrAdd"
          @submit="saveEntity"
        />

        <!-- 物品详情显示 -->
        <EntityDetail
          v-else-if="currentEntity"
          :loading="detailLoading"
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
import { ref, onMounted, computed } from "vue";
import {
  Plus,
  Folder,
  Refresh,
  Search,
  Select,
  Edit,
  Delete
} from "@element-plus/icons-vue";
import { useEntityCRUD } from "./composables/useEntityCRUD";
import EntityTree from "./components/EntityTree.vue";
import EntityDetail from "./components/EntityDetail.vue";
import EntityForm from "./components/EntityForm.vue";
import type { Entity } from "@/types/entity";

defineOptions({
  name: "Entity"
});

// 使用实体CRUD相关逻辑，指定为非搜索模式
const {
  treeLoading,
  loading,
  saving,
  treeData,
  currentEntity,
  isEditing,
  isAdding,
  entityTags,
  detailLoading,
  loadTreeData,
  handleNodeClick,
  handleDelete,
  openAddEntityForm,
  openEditEntityForm,
  cancelEditOrAdd,
  saveEntity,
  loadEntityDetail,
  loadAllTags
} = useEntityCRUD({
  isSearchMode: false
});

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
        // 创建子节点的副本并进行过滤
        const filteredChildren = filterTree([...node.children]);
        // 创建节点的副本，避免修改原始节点
        const nodeCopy = { ...node };
        nodeCopy.children = filteredChildren;
        // 如果子节点匹配，父节点也要显示
        const shouldKeep = isMatch || filteredChildren.length > 0;
        return shouldKeep ? nodeCopy : shouldKeep;
      }

      return isMatch;
    });
  };

  // 使用扩展运算符创建浅拷贝
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
    detailLoading.value = true;
    loadEntityDetail(currentEntity.value.id).finally(() => {
      detailLoading.value = false;
    });
  }
};

// 在组件挂载时加载数据
onMounted(() => {
  loadTreeData();
  loadAllTags(); // 加载所有标签
});
</script>

<style scoped>
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

:deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
}
</style>
