<template>
  <el-form-item label="所属位置" prop="parentId">
    <el-tree
      :current-node-key="currentNodeKey"
      :data="locationOptions"
      :props="{ label: 'name', children: 'children' }"
      node-key="id"
      highlight-current
      @current-change="handleLocationSelect"
      default-expand-all
      :expand-on-click-node="false"
      class="location-tree"
    >
      <template #default="{ node, data }">
        <div class="custom-tree-node">
          <span>
            <el-icon v-if="data.type === 'space'"><Folder /></el-icon>
            <el-icon v-else><Goods /></el-icon>
            {{ node.label }}
          </span>
        </div>
      </template>
    </el-tree>
    <div v-if="selectedLocationName" class="selected-location">
      <span>当前位置: {{ selectedLocationName }}</span>
      <el-button type="primary" link @click="clearSelectedLocation">清除</el-button>
    </div>
  </el-form-item>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue';
import { Folder, Goods } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'LocationSelector',
  components: {
    Folder,
    Goods
  },
  props: {
    parentId: {
      type: String,
      required: true
    },
    locationOptions: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:parentId'],
  setup(props, { emit }) {
    const selectedLocationName = ref<string>('根空间');
    const currentNodeKey = ref<string>(props.parentId);

    // 监听父ID属性变化，更新当前选中节点
    watch(() => props.parentId, (newVal) => {
      currentNodeKey.value = newVal;
      updateSelectedLocationName();
    });

    // 处理位置选择
    const handleLocationSelect = (data: any) => {
      if (data && data.id) {
        emit('update:parentId', data.id);
        selectedLocationName.value = data.name;
      }
    };

    // 清除选中的位置（设置为根空间）
    const clearSelectedLocation = () => {
      emit('update:parentId', '0');
      selectedLocationName.value = '根空间';
    };

    // 更新选中位置的名称
    const updateSelectedLocationName = () => {
      // 找出当前选中的节点
      if (props.parentId === '0') {
        selectedLocationName.value = '根空间';
        return;
      }

      // 找出对应的位置名称
      const findNodeName = (nodes: any[]): string | null => {
        for (const node of nodes) {
          if (node.id === props.parentId) {
            return node.name;
          }
          if (node.children && node.children.length > 0) {
            const result = findNodeName(node.children);
            if (result) return result;
          }
        }
        return null;
      };

      const name = findNodeName(props.locationOptions);
      if (name) {
        selectedLocationName.value = name;
      }
    };

    // 初始化执行一次
    updateSelectedLocationName();

    return {
      currentNodeKey,
      selectedLocationName,
      handleLocationSelect,
      clearSelectedLocation
    };
  }
});
</script>

<style scoped>
.location-tree {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.location-tree .el-tree-node__content {
  height: 32px;
}

.selected-location {
  margin-top: 8px;
  padding: 8px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 