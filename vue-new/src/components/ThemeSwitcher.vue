<template>
  <div class="theme-switcher-container">
    <el-dropdown trigger="click" @command="handleThemeChange">
      <el-button type="text" class="theme-button">
        <el-icon v-if="themeStore.mode === 'light'"><Sunny /></el-icon>
        <el-icon v-else-if="themeStore.mode === 'dark'"><Moon /></el-icon>
        <el-icon v-else><Monitor /></el-icon>
        <span class="theme-text d-md-none d-lg-inline">{{ themeText }}</span>
        <el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="light">
            <el-icon><Sunny /></el-icon>
            <span>明亮模式</span>
          </el-dropdown-item>
          <el-dropdown-item command="dark">
            <el-icon><Moon /></el-icon>
            <span>暗黑模式</span>
          </el-dropdown-item>
          <el-dropdown-item command="system">
            <el-icon><Monitor /></el-icon>
            <span>跟随系统</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import { useThemeStore } from '@/store/modules/theme'
import { Sunny, Moon, Monitor, ArrowDown } from '@element-plus/icons-vue'

// 获取主题存储
const themeStore = useThemeStore()

// 主题文本
const themeText = computed(() => {
  switch (themeStore.mode) {
    case 'light':
      return '明亮模式'
    case 'dark':
      return '暗黑模式'
    case 'system':
      return '跟随系统'
    default:
      return '主题设置'
  }
})

// 处理主题变更
const handleThemeChange = (command: string) => {
  if (command === 'light' || command === 'dark' || command === 'system') {
    themeStore.setMode(command)
  }
}

// 组件挂载时初始化主题
onMounted(() => {
  themeStore.init()
})
</script>

<style scoped>
.theme-switcher-container {
  display: flex;
  align-items: center;
}

.theme-button {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--text-color-primary);
}

.theme-text {
  margin: 0 4px;
}

/* 响应式样式 */
@media screen and (max-width: 767px) {
  .theme-button {
    padding: 8px;
  }
}
</style> 