<template>
  <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
    <div class="logo-container">
      <router-link to="/">
        <img src="@/assets/images/logo.png" alt="Logo" class="logo-image" v-if="!isCollapse" />
        <img src="@/assets/images/logo-mini.png" alt="Logo" class="logo-image-mini" v-else />
      </router-link>
    </div>
    
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :collapse="isCollapse"
        :default-active="activeMenu"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :unique-opened="false"
        :collapse-transition="false"
        router
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
          :is-collapse="isCollapse"
        />
      </el-menu>
    </el-scrollbar>
    
    <div class="sidebar-footer">
      <el-dropdown @command="handleCommand" trigger="click">
        <div class="user-info">
          <el-avatar :size="isCollapse ? 32 : 36" :src="userAvatar" />
          <div class="user-detail" v-if="!isCollapse">
            <span class="user-name">{{ userName }}</span>
            <span class="user-role">{{ userRole }}</span>
          </div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><UserFilled /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/modules/auth';
import SidebarItem from './SidebarItem.vue';
import { Setting, UserFilled, SwitchButton } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'AppSidebar',
  components: {
    SidebarItem,
    Setting,
    UserFilled,
    SwitchButton
  },
  props: {
    isCollapse: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle-sidebar'],
  setup(props, { emit }) {
    const router = useRouter();
    const route = useRoute();
    const authStore = useAuthStore();
    
    // 主题变量
    const variables = computed(() => ({
      menuBg: '#304156',
      menuText: '#bfcbd9',
      menuActiveText: '#409EFF'
    }));
    
    // 当前激活的菜单
    const activeMenu = computed(() => {
      const { meta, path } = route;
      if (meta.activeMenu) {
        return meta.activeMenu;
      }
      return path;
    });
    
    // 获取路由表中的菜单项
    const routes = computed(() => {
      return router.options.routes.find(route => route.path === '/')?.children?.filter(route => {
        return !(route.meta?.hideInMenu);
      }) || [];
    });
    
    // 用户信息
    const userName = computed(() => {
      return authStore.currentUser?.nickname || authStore.currentUser?.username || '用户';
    });
    
    const userRole = computed(() => {
      return authStore.currentUser?.role === 'ADMIN' ? '管理员' : '普通用户';
    });
    
    const userAvatar = computed(() => {
      return authStore.currentUser?.avatar || '/placeholder-avatar.png';
    });
    
    // 处理下拉菜单命令
    const handleCommand = (command: string) => {
      switch (command) {
        case 'profile':
          router.push('/settings/profile');
          break;
        case 'settings':
          router.push('/settings');
          break;
        case 'logout':
          authStore.logout();
          break;
      }
    };
    
    return {
      variables,
      activeMenu,
      routes,
      userName,
      userRole,
      userAvatar,
      handleCommand
    };
  }
});
</script>

<style scoped>
.sidebar-container {
  width: 210px;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow: hidden;
  background-color: #304156;
  transition: width 0.28s;
  z-index: 1001;
  display: flex;
  flex-direction: column;
}

.is-collapse {
  width: 54px;
}

.logo-container {
  height: 50px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
}

.logo-image {
  height: 32px;
}

.logo-image-mini {
  height: 24px;
}

.scrollbar-wrapper {
  flex: 1;
  overflow-x: hidden !important;
}

.el-scrollbar__wrap {
  overflow-x: hidden !important;
}

.el-menu {
  border-right: none;
}

.sidebar-footer {
  padding: 10px;
  background-color: #263445;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-detail {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-name {
  font-size: 14px;
  color: #ffffff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #bfcbd9;
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar-container {
    transform: translateX(-210px);
    z-index: 1001;
  }
  
  .is-collapse {
    transform: translateX(-54px);
  }
}
</style> 