<template>
  <div class="tree-container">
    <el-card class="tree-card">
      <template #header>
        <div class="card-header">
          <span>物品结构</span>
        </div>
      </template>
      <div v-if="loading" class="tree-loading">
        <el-skeleton :rows="10" animated />
      </div>
      <el-empty v-else-if="treeData.length === 0" description="暂无物品结构数据" />
      <el-tree
        v-else
        ref="entityTreeRef"
        :data="treeData"
        :props="defaultProps"
        node-key="id"
        highlight-current
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
        default-expand-all
      >
        <template #default="{ node }">
          <div class="custom-tree-node">
            <span>
              <el-icon><component :is="node.data.type === 'space' ? Folder : Goods" /></el-icon>
              {{ node.label }}
            </span>
          </div>
        </template>
      </el-tree>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { Folder, Goods } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'EntityTree',
  components: {
    Folder,
    Goods
  },
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    treeData: {
      type: Array,
      default: () => []
    }
  },
  emits: ['node-click'],
  setup(props, { emit }) {
    const entityTreeRef = ref<any>(null);

    // 树节点配置
    const defaultProps = {
      children: 'children',
      label: 'name'
    };

    // 处理树节点点击
    const handleNodeClick = (data: any) => {
      emit('node-click', data);
    };

    return {
      entityTreeRef,
      defaultProps,
      handleNodeClick,
      Folder, 
      Goods
    };
  }
});
</script>

<style scoped>
.tree-container {
  width: 25%;
  min-width: 200px;
  transition: all 0.3s ease;
}

.tree-card {
  height: 100%;
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.tree-loading {
  padding: 20px;
}

@media screen and (max-width: 992px) {
  .tree-container {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style> 