<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
      <span v-if="index === breadcrumbs.length - 1" class="no-redirect">{{ item.title }}</span>
      <a v-else @click.prevent="handleLink(item)">{{ item.title }}</a>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue';
import { useRoute, useRouter, RouteLocationMatched } from 'vue-router';

interface BreadcrumbItem {
  path: string;
  title: string;
}

export default defineComponent({
  name: 'Breadcrumb',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const breadcrumbs = ref<BreadcrumbItem[]>([]);

    // 获取面包屑数据
    const getBreadcrumbs = () => {
      const matched = route.matched.filter(item => item.meta && item.meta.title);
      
      // 根路径
      const home: BreadcrumbItem = {
        path: '/dashboard',
        title: '首页'
      };
      
      // 不在首页时，添加首页为第一个面包屑
      if (route.path !== '/dashboard') {
        breadcrumbs.value = [home];
      } else {
        breadcrumbs.value = [];
      }
      
      // 添加其他路由
      matched.forEach(item => {
        if (item.meta && item.meta.title) {
          breadcrumbs.value.push({
            path: item.path,
            title: item.meta.title as string
          });
        }
      });
    };
    
    // 处理点击链接
    const handleLink = (item: BreadcrumbItem) => {
      router.push(item.path);
    };
    
    // 监听路由变化
    watch(
      () => route.path,
      () => {
        getBreadcrumbs();
      },
      { immediate: true }
    );
    
    return {
      breadcrumbs,
      handleLink
    };
  }
});
</script>

<style scoped>
.el-breadcrumb {
  font-size: 14px;
  line-height: 1;
}

.no-redirect {
  color: #97a8be;
  cursor: text;
}
</style> 