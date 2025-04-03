<script lang="ts" setup>
import { useThemeStore } from './store/modules/theme';
import { onMounted } from 'vue';

// 获取主题存储
const themeStore = useThemeStore();

// 组件挂载时初始化主题
onMounted(() => {
  themeStore.init();
});
</script>

<template>
  <router-view />
</template>
