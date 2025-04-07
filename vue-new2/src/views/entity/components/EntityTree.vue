<template>
  <div class="w-full h-full">
    <el-skeleton :rows="5" animated v-if="loading" />
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
      @node-click="handleNodeClick"
      class="h-full overflow-auto"
    >
      <template #default="{ node, data }">
        <div class="flex justify-between items-center w-full py-1">
          <div class="flex items-center">
            <el-icon v-if="data.children && data.children.length" class="mr-2 text-blue-500">
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
import { ref, watch } from 'vue'
import { Search, Folder, Document } from '@element-plus/icons-vue'
import type { Entity } from '@/types/entity'

interface Props {
  loading: boolean
  treeData: Entity[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'node-click', node: Entity): void
}>()

const treeRef = ref()
const filterText = ref('')

// 树形配置
const defaultProps = {
  children: 'children',
  label: 'name'
}

// 过滤节点
const filterNode = (value: string, data: Entity) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 监听过滤文本
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 处理节点点击
const handleNodeClick = (data: Entity) => {
  emit('node-click', data)
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