<template>
  <div class="layout-container">
    <el-container class="main-container">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
        <div class="logo-container">
          <!-- <img src="@/assets/images/logo.png" class="logo-image" alt="Logo" /> -->
          <h1 class="logo-text" v-show="!isCollapse">家庭物品管理</h1>
        </div>
        
        <el-scrollbar>
          <el-menu
            :default-active="activeMenuItem"
            class="el-menu-vertical"
            :collapse="isCollapse"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            :collapse-transition="false"
            :router="true"
          >
            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <template #title>首页</template>
            </el-menu-item>
            
            <el-menu-item index="/entities">
              <el-icon><Box /></el-icon>
              <template #title>物品结构</template>
            </el-menu-item>

            <el-menu-item index="/entitiesSearch">
              <el-icon><Box /></el-icon>
              <template #title>物品搜索</template>
            </el-menu-item>

            <el-menu-item index="/tags">
              <el-icon><Collection /></el-icon>
              <template #title>标签管理</template>
            </el-menu-item>
            
            <el-menu-item index="/reminder">
              <el-icon><Bell /></el-icon>
              <template #title>到期提醒</template>
            </el-menu-item>
            
            <el-sub-menu index="settings">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </template>
              <el-menu-item index="/settings/profile">个人资料</el-menu-item>
              <el-menu-item index="/settings/notification">通知设置</el-menu-item>
              <el-menu-item index="/settings/system">系统设置</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </el-aside>
      
      <el-container class="content-container">
        <el-header class="header-container">
          <div class="header-left">
            <el-icon class="collapse-icon" @click="toggleSidebar">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <Breadcrumb />
          </div>
          
          <div class="header-right">
            <el-tooltip content="全屏显示" placement="bottom">
              <el-icon class="header-icon" @click="toggleFullScreen">
                <FullScreen />
              </el-icon>
            </el-tooltip>
            
            <el-tooltip content="消息通知" placement="bottom">
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
                <el-icon class="header-icon" @click="showNotifications">
                  <Bell />
                </el-icon>
              </el-badge>
            </el-tooltip>
            
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userInfo?.avatar" v-if="userInfo?.avatar">
                  {{ userInfo?.nickname ? userInfo.nickname.substring(0, 1) : (userInfo?.username ? userInfo.username.substring(0, 1) : 'U') }}
                </el-avatar>
                <el-avatar :size="32" icon="el-icon-user" v-else></el-avatar>
                <span class="username" v-if="!isSmallScreen">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><UserIcon /></el-icon>个人资料
                  </el-dropdown-item>
                  <el-dropdown-item command="password">
                    <el-icon><Lock /></el-icon>修改密码
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
        
        <el-footer class="footer-container">
          Copyright © {{ currentYear }} 家庭物品管理系统 版权所有
        </el-footer>
      </el-container>
    </el-container>
    
    <!-- 通知抽屉 -->
    <el-drawer
      v-model="notificationDrawer"
      title="消息通知"
      direction="rtl"
      size="350px"
    >
      <div class="notification-header">
        <span>消息通知 ({{ notifications.length }})</span>
        <el-button v-if="notifications.length > 0" type="primary" link @click="markAllRead">全部已读</el-button>
      </div>
      
      <el-tabs v-model="notificationActiveTab">
        <el-tab-pane label="全部消息" name="all">
          <div v-if="notifications.length > 0" class="notification-list">
            <div 
              v-for="(item, index) in notifications" 
              :key="index" 
              class="notification-item"
              :class="{ 'notification-unread': !item.read }"
              @click="viewNotification(item)"
            >
              <div class="notification-icon" :class="`notification-type-${item.type}`">
                <el-icon>
                  <Bell v-if="item.type === 'reminder'" />
                  <InfoFilled v-else-if="item.type === 'system'" />
                  <WarningFilled v-else-if="item.type === 'warning'" />
                  <CircleCheckFilled v-else />
                </el-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title">{{ item.title }}</div>
                <div class="notification-desc">{{ item.content }}</div>
                <div class="notification-time">{{ formatTime(item.time) }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无消息通知" />
        </el-tab-pane>
        <el-tab-pane label="未读消息" name="unread">
          <div v-if="unreadNotifications.length > 0" class="notification-list">
            <div 
              v-for="(item, index) in unreadNotifications" 
              :key="index" 
              class="notification-item notification-unread"
              @click="viewNotification(item)"
            >
              <div class="notification-icon" :class="`notification-type-${item.type}`">
                <el-icon>
                  <Bell v-if="item.type === 'reminder'" />
                  <InfoFilled v-else-if="item.type === 'system'" />
                  <WarningFilled v-else-if="item.type === 'warning'" />
                  <CircleCheckFilled v-else />
                </el-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title">{{ item.title }}</div>
                <div class="notification-desc">{{ item.content }}</div>
                <div class="notification-time">{{ formatTime(item.time) }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无未读消息" />
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/store/modules/auth';
import { User } from '@/types/user';
import { 
  HomeFilled, Box, Collection, Promotion, Bell, Setting, User as UserIcon, Lock, 
  SwitchButton, Fold, Expand, FullScreen, ArrowDown, InfoFilled,
  WarningFilled, CircleCheckFilled, Share
} from '@element-plus/icons-vue';
import Breadcrumb from '@/components/layout/Breadcrumb.vue';
import moment from 'moment';
import { ElMessageBox } from 'element-plus';

interface Notification {
  id: string;
  title: string;
  content: string;
  type: 'reminder' | 'system' | 'warning' | 'success';
  time: string;
  read: boolean;
  entityId?: string;
}

export default defineComponent({
  name: 'Layout',
  components: {
    HomeFilled, Box, Collection, Promotion, Bell, Setting, UserIcon, Lock,
    SwitchButton, Fold, Expand, FullScreen, ArrowDown, InfoFilled,
    WarningFilled, CircleCheckFilled, Breadcrumb, Share
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const authStore = useAuthStore();
    const userInfo = computed(() => authStore.currentUser || {} as User);
    
    // 侧边栏折叠状态
    const isCollapse = ref(false);
    const notificationDrawer = ref(false);
    const notificationActiveTab = ref('all');
    
    // 消息通知
    const notifications = ref<Notification[]>([
      {
        id: '1',
        title: '到期提醒',
        content: '您有3个物品即将到期，请及时处理',
        type: 'reminder',
        time: new Date().toISOString(),
        read: false
      },
      {
        id: '2',
        title: '系统通知',
        content: '系统将于今晚23:00-24:00进行维护更新',
        type: 'system',
        time: new Date(Date.now() - 3600000).toISOString(),
        read: true
      }
    ]);
    
    // 未读消息
    const unreadNotifications = computed(() => {
      return notifications.value.filter(item => !item.read);
    });
    
    // 未读消息数量
    const unreadCount = computed(() => {
      return unreadNotifications.value.length;
    });
    
    // 当前激活的菜单项
    const activeMenuItem = computed(() => {
      return route.path;
    });
    
    // 是否是小屏幕
    const isSmallScreen = ref(window.innerWidth < 768);
    
    // 当前年份
    const currentYear = new Date().getFullYear();
    
    // 切换侧边栏折叠状态
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value;
    };
    
    // 显示通知抽屉
    const showNotifications = () => {
      notificationDrawer.value = true;
    };
    
    // 查看通知详情
    const viewNotification = (notification: Notification) => {
      // 标记为已读
      notification.read = true;
      
      // 如果是到期提醒，跳转到相应的实体详情页
      if (notification.type === 'reminder' && notification.entityId) {
        router.push(`/entity/detail/${notification.entityId}`);
        notificationDrawer.value = false;
      }
    };
    
    // 标记所有消息为已读
    const markAllRead = () => {
      notifications.value.forEach(item => {
        item.read = true;
      });
    };
    
    // 处理用户下拉菜单命令
    const handleCommand = (command: string) => {
      switch (command) {
        case 'profile':
          router.push('/settings/profile');
          break;
        case 'password':
          router.push('/settings/password');
          break;
        case 'logout':
          handleLogout();
          break;
      }
    };
    
    // 处理退出登录
    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await authStore.logout();
        router.push('/login');
      }).catch(() => {
        // 取消退出
      });
    };
    
    // 切换全屏
    const toggleFullScreen = () => {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        document.documentElement.requestFullscreen();
      }
    };
    
    // 格式化时间
    const formatTime = (time: string) => {
      return moment(time).fromNow();
    };
    
    // 监听窗口大小变化
    const handleResize = () => {
      isSmallScreen.value = window.innerWidth < 768;
      if (window.innerWidth < 992 && !isCollapse.value) {
        isCollapse.value = true;
      }
    };
    
    // 监听路由变化，在小屏幕上自动折叠侧边栏
    watch(() => route.path, () => {
      if (isSmallScreen.value && !isCollapse.value) {
        isCollapse.value = true;
      }
    });
    
    onMounted(() => {
      window.addEventListener('resize', handleResize);
      handleResize();
    });
    
    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize);
    });
    
    return {
      isCollapse,
      activeMenuItem,
      userInfo,
      currentYear,
      isSmallScreen,
      notificationDrawer,
      notificationActiveTab,
      notifications,
      unreadNotifications,
      unreadCount,
      toggleSidebar,
      showNotifications,
      viewNotification,
      markAllRead,
      handleCommand,
      toggleFullScreen,
      formatTime
    };
  }
});
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.main-container {
  height: 100%;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.3s;
  position: relative;
  z-index: 999;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  background-color: #263445;
}

.logo-image {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  margin: 0;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 220px;
}

.el-menu {
  border-right: none;
}

.content-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header-container {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-icon {
  font-size: 20px;
  color: #606266;
  margin: 0 10px;
  cursor: pointer;
}

.notification-badge {
  margin: 0 10px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 8px;
  color: #606266;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  height: calc(100% - 60px - 50px); /* header - footer */
}

.footer-container {
  height: 50px;
  background-color: #fff;
  color: #606266;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
}

/* 通知样式 */
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 15px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.notification-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-bottom: 1px solid #f0f0f0;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-unread {
  background-color: #e6f7ff;
}

.notification-icon {
  margin-right: 16px;
  padding: 8px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.notification-type-reminder {
  background-color: #e6f7ff;
  color: #1890ff;
}

.notification-type-system {
  background-color: #f6ffed;
  color: #52c41a;
}

.notification-type-warning {
  background-color: #fff7e6;
  color: #fa8c16;
}

.notification-type-success {
  background-color: #e6fffb;
  color: #13c2c2;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.notification-desc {
  color: #606266;
  font-size: 13px;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 