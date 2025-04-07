<template>
  <div class="entity-tree">
    <el-card shadow="hover" class="h-full">
      <template #header>
        <div class="flex justify-between items-center">
          <span>物品结构</span>
          <el-input
            v-model="filterText"
            placeholder="搜索物品"
            clearable
            class="w-200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
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
      >
        <template #default="{ node, data }">
          <div class="custom-tree-node">
            <div class="node-content">
              <el-icon v-if="data.children && data.children.length" class="mr-1">
                <Folder />
              </el-icon>
              <el-icon v-else class="mr-1">
                <Document />
              </el-icon>
              <span>{{ node.label }}</span>
            </div>
            <div class="node-actions">
              <el-tag size="small" :type="getStatusType(data.status)">
                {{ getStatusText(data.status) }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>
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
.entity-tree {
  width: 300px;
  height: 100%;
}

.h-full {
  height: 100%;
}

.w-200px {
  width: 200px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-content {
  display: flex;
  align-items: center;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mr-1 {
  margin-right: 4px;
}
</style> 