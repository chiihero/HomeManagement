<template>
  <el-config-provider :locale="currentLocale">
    <!-- 路由视图 -->
    <router-view />
    <!-- 全局对话框组件 -->
    <ReDialog />
  </el-config-provider>
</template>

<script lang="ts">
/**
 * 应用程序根组件
 * 提供全局配置和布局
 */
import { defineComponent } from "vue";
import { ElConfigProvider } from "element-plus";
import { ReDialog } from "@/components/ReDialog";
import zhCn from "element-plus/es/locale/lang/zh-cn";

export default defineComponent({
  name: "app",
  components: {
    [ElConfigProvider.name]: ElConfigProvider,
    ReDialog
  },
  computed: {
    /**
     * 获取当前Element Plus的语言配置
     * @returns 当前语言配置对象
     */
    currentLocale() {
      return zhCn;
    }
  }
});
</script>
