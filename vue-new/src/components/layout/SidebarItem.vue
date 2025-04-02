<template>
  <div class="sidebar-item-wrapper">
    <!-- 有子菜单的情况 -->
    <template v-if="hasChildren(item)">
      <el-sub-menu :index="resolvePath(item.path)">
        <template #title>
          <el-icon v-if="item.meta && item.meta.icon">
            <component :is="item.meta.icon" />
          </el-icon>
          <span>{{ item.meta ? item.meta.title : item.name }}</span>
        </template>
        
        <sidebar-item
          v-for="child in item.children"
          :key="child.path"
          :item="child"
          :base-path="resolvePath(child.path)"
          :is-collapse="isCollapse"
        />
      </el-sub-menu>
    </template>
    
    <!-- 无子菜单的情况 -->
    <template v-else>
      <el-menu-item :index="resolvePath(item.path)" @click="pushRouter(item)">
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <template #title>{{ item.meta ? item.meta.title : item.name }}</template>
      </el-menu-item>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useRouter } from 'vue-router';
import path from 'path-browserify';

export default defineComponent({
  name: 'SidebarItem',
  // 组件递归调用自身
  components: {
    SidebarItem: () => import('./SidebarItem.vue')
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    basePath: {
      type: String,
      default: ''
    },
    isCollapse: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const router = useRouter();
    
    // 判断是否有子菜单
    const hasChildren = (item: any) => {
      return item.children && item.children.length > 0 && !item.meta?.hideChildrenInMenu;
    };
    
    // 解析路径
    const resolvePath = (routePath: string) => {
      if (routePath.startsWith('/')) {
        return routePath;
      }
      if (props.basePath) {
        return path.resolve(props.basePath, routePath);
      }
      return routePath;
    };
    
    // 跳转路由
    const pushRouter = (item: any) => {
      router.push({
        path: resolvePath(item.path)
      });
    };
    
    return {
      hasChildren,
      resolvePath,
      pushRouter
    };
  }
});
</script>

<style scoped>
.sidebar-item-wrapper {
  width: 100%;
}
</style> 