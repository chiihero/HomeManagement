<template>
  <div class="w-full h-full">
    <el-skeleton v-if="loading"
    :throttle="{ leading: 500, trailing: 500 }" :rows="1" animated />
    <el-empty v-else-if="!treeData.length" description="暂无物品数据" />
    <el-tree
      v-else
      ref="treeRef"
      :data="treeData"
      :props="defaultProps"
      :filter-node-method="filterNode"
      node-key="id"
      highlight-current
      :expand-on-click-node="false"
      :key="'entity-tree'"
      :default-expanded-keys="expandedKeys"
      @node-expand="handleNodeExpand"
      @node-collapse="handleNodeCollapse"
      class="h-full overflow-auto"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="flex justify-between items-center w-full py-1">
          <div class="flex items-center">
            <el-icon
              v-if="data.children && data.children.length"
              class="mr-2 text-blue-500"
            >
              <Folder />
            </el-icon>
            <el-icon v-else class="mr-2 text-gray-500">
              <Document />
            </el-icon>
            <span class="text-sm">{{ node.label }}</span>
          </div>
          <el-tag size="small" :type="getStatusType(data.status)" class="ml-2">
            {{ getStatusText(data.status) }}
          </el-tag>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import { Search, Folder, Document } from "@element-plus/icons-vue";
import type { Entity } from "@/types/entity";

interface Props {
  loading: boolean;
  treeData: Entity[];
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "node-click", node: Entity): void;
}>();

const treeRef = ref();
const filterText = ref("");
// 存储展开的节点ID
const expandedKeys = ref<string[]>([]);

// 树形配置
const defaultProps = {
  children: "children",
  label: "name"
};

// 处理节点展开
const handleNodeExpand = (data: Entity) => {
  if (data.id && !expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id);
  }
};

// 处理节点折叠
const handleNodeCollapse = (data: Entity) => {
  if (data.id) {
    const index = expandedKeys.value.indexOf(data.id);
    if (index !== -1) {
      expandedKeys.value.splice(index, 1);
    }
  }
};

// 过滤节点
const filterNode = (value: string, data: Entity) => {
  if (!value) return true;
  return data.name.toLowerCase().includes(value.toLowerCase());
};

// 监听过滤文本
watch(filterText, val => {
  treeRef.value?.filter(val);
});

// 在组件挂载后自动展开根节点
onMounted(() => {
  // 延迟执行，确保树已经渲染完成
  setTimeout(() => {
    // 默认展开第一级节点
    if (props.treeData && props.treeData.length > 0) {
      props.treeData.forEach(node => {
        if (node.id) {
          expandedKeys.value.push(node.id);
        }
      });
    }
  }, 100);
});

// 处理节点点击
const handleNodeClick = (data: Entity) => {
  emit("node-click", data);
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
</script>

<style scoped>
:deep(.el-tree-node__content) {
  height: auto;
  padding: 4px 0;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--el-fill-color-light);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--el-color-primary-light-9);
}
</style>
