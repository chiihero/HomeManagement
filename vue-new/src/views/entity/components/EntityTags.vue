<template>
  <el-form-item label="标签" prop="tags">
    <el-select
      v-model="selectedTags"
      multiple
      filterable
      allow-create
      default-first-option
      placeholder="请选择或创建标签"
      style="width: 100%"
      @change="handleTagsChange"
    >
      <el-option
        v-for="tag in tagOptions"
        :key="tag.id"
        :label="tag.name"
        :value="tag.id"
        :style="{ backgroundColor: tag.color, color: getContrastColor(tag.color) }"
      />
    </el-select>
  </el-form-item>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue';
import { useEntityForm } from '../composables/useEntityForm';

export default defineComponent({
  name: 'EntityTags',
  props: {
    tags: {
      type: Array,
      required: true
    },
    tagOptions: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:tags'],
  setup(props, { emit }) {
    const { getContrastColor } = useEntityForm();
    const selectedTags = ref<number[]>([]);
    
    // 监听tags属性变化，更新选中标签
    watch(() => props.tags, (newVal) => {
      selectedTags.value = [...newVal];
    }, { immediate: true });
    
    // 处理标签变更
    const handleTagsChange = (value: number[]) => {
      emit('update:tags', value);
    };

    return {
      selectedTags,
      getContrastColor,
      handleTagsChange
    };
  }
});
</script>

<style scoped>
/* 无需额外样式 */
</style> 