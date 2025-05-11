<template>
  <div class="w-full h-full">
    <el-skeleton
      v-if="loading"
      :throttle="{ leading: 500, trailing: 500 }"
      :rows="1"
      animated
    />
    <el-empty v-else-if="!treeData.length" description="暂无物品数据" />
    <el-tree-v2
      v-else
      ref="treeRef"
      :data="treeData"
      :props="defaultProps"
      :filter-method="filterNode"
      node-key="id"
      :highlight-current="true"
      :current-node-key="selectedKey"
      class="h-full overflow-auto"
      :height="height"
      :default-expanded-keys="expandedKeys"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="flex justify-between items-center w-full py-1">
          <div class="flex items-center">
            <el-icon v-if="data.children && data.children.length">
              <IconifyIconOffline :icon="EpMessageBox" />
            </el-icon>
            <IconifyIconOffline v-else-if="data.type === '食品'" :icon="food" />
            <IconifyIconOffline
              v-else-if="data.type === '药品'"
              :icon="EpFirstAidKit"
            />
            <IconifyIconOffline
              v-else-if="data.type === '耗材'"
              :icon="EpToiletPaper"
            />
            <IconifyIconOffline
              v-else-if="data.type === '包装'"
              :icon="EpBox"
            />
            <IconifyIconOffline v-else :icon="Document" />

            <span class="text-sm">{{ node.label }}</span>
          </div>
          <el-tag size="small" :type="getStatusType(data.status)" class="ml-2">
            {{ getStatusText(data.status) }}
          </el-tag>
        </div>
      </template>
    </el-tree-v2>
  </div>
</template>

<script setup lang="ts">
import { ElTreeV2 } from 'element-plus'
import { ref, watch, onMounted, computed } from "vue";
import type { Entity } from "@/types/entity";
import EpMessageBox from "@iconify-icons/ep/message-box";
import EpBox from "@iconify-icons/ep/box";
import food from "@iconify-icons/ep/food";
import Document from "@iconify-icons/ep/Document";
import EpFirstAidKit from "@iconify-icons/ep/first-aid-kit";
import EpToiletPaper from "@iconify-icons/ep/toilet-paper";
interface Props {
  loading: boolean;
  treeData: Entity[];
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "node-click", node: Entity): void;
}>();
const treeRef = ref()
// const treeRef = ref<InstanceType<typeof ElTreeV2>>()
const filterText = ref("");

// 存储当前选中的节点ID
const selectedKey = ref<string>("");
// 虚拟树的高度
const height = ref<number>(window.innerWidth > 768 ? window.innerHeight : 400);
// 存储展开的节点ID
const expandedKeys = ref<string[]>([]);

// 树形配置
const defaultProps = {
  children: "children",
  label: "name"
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

// 监听树数据变化，确保在数据刷新后仍然保持选中状态和展开状态
watch(() => props.treeData, () => {
  expandedKeys.value = [];
  // 如果有选中的节点，确保在树刷新后仍然选中
  if (selectedKey.value) {
    expandedKeys.value.push(selectedKey.value);
    // 获取并展开所有父节点
    const parentIds = getParentIds(selectedKey.value, props.treeData);
    if (parentIds.length > 0) {
      expandedKeys.value.push(...parentIds);
    }
  }
});

// 递归获取所有节点ID
const getAllNodeIds = (nodes: Entity[]): string[] => {
  let ids: string[] = [];
  nodes.forEach(node => {
    ids.push(node.id);
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getAllNodeIds(node.children));
    }
  });
  return ids;
};



// 在组件挂载后设置树高度为容器高度并展开所有节点
onMounted(() => {
});

// 处理节点点击
const handleNodeClick = (data: Entity) => {
  // 保存当前选中的节点ID
  selectedKey.value = data.id;

  emit("node-click", data);
};

// 获取节点的所有父节点ID
const getParentIds = (nodeId: string, nodes: Entity[]): string[] => {
  const parentIds: string[] = [];
  
  // 递归查找父节点
  const findParent = (id: string, treeNodes: Entity[], path: string[] = []) => {
    for (const node of treeNodes) {
      // 当前路径
      const currentPath = [...path];
      
      // 如果找到了节点
      if (node.id === id) {
        return currentPath;
      }
      
      // 如果有子节点，则递归查找
      if (node.children && node.children.length > 0) {
        // 将当前节点ID添加到路径中
        currentPath.push(node.id);
        const result = findParent(id, node.children, currentPath);
        if (result.length > 0) {
          return result;
        }
      }
    }
    return [];
  };
  
  // 查找父节点路径
  const parentPath = findParent(nodeId, nodes);
  if (parentPath.length > 0) {
    parentIds.push(...parentPath);
  }
  
  return parentIds;
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
