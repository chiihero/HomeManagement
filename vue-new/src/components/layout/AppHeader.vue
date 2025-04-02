<template>
  <div class="app-header">
    <div class="left-area">
      <!-- 折叠按钮 -->
      <div class="hamburger-container" @click="toggleSidebar">
        <el-icon :class="{ 'is-active': !isCollapse }">
          <Fold v-if="isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
      
      <!-- 面包屑 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index" :to="{ path: item.path }">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="right-menu">
      <!-- 搜索框 -->
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="全局搜索..."
          prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>
      
      <!-- 全屏按钮 -->
      <el-tooltip content="全屏" placement="bottom">
        <div class="right-menu-item hover-effect" @click="toggleFullScreen">
          <el-icon>
            <FullScreen v-if="!isFullScreen" />
            <ScaleToOriginal v-else />
          </el-icon>
        </div>
      </el-tooltip>
      
      <!-- 通知按钮 -->
      <el-dropdown trigger="click" @command="handleNotification">
        <el-badge :value="unreadCount" :max="99" :hidden="unreadCount <= 0" class="right-menu-item hover-effect">
          <el-icon>
            <Bell />
          </el-icon>
        </el-badge>
        <template #dropdown>
          <el-dropdown-menu>
            <div class="notification-title">
              <span>通知</span>
              <el-button v-if="notifications.length > 0" type="text" @click="markAllAsRead">全部已读</el-button>
            </div>
            <el-divider></el-divider>
            <div class="notification-container">
              <el-empty description="暂无通知" v-if="notifications.length === 0" />
              <el-dropdown-item v-for="(item, index) in notifications" :key="index" :command="item">
                <div class="notification-item" :class="{ 'is-read': item.isRead }">
                  <el-icon><InfoFilled /></el-icon>
                  <div class="notification-content">
                    <div class="notification-title">{{ item.title }}</div>
                    <div class="notification-desc">{{ item.content }}</div>
                    <div class="notification-time">{{ item.time }}</div>
                  </div>
                </div>
              </el-dropdown-item>
            </div>
            <el-divider></el-divider>
            <div class="notification-footer">
              <el-button type="text" @click="viewAllNotifications">查看全部</el-button>
            </div>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/modules/auth';
import { Fold, Expand, Search, FullScreen, ScaleToOriginal, Bell, InfoFilled } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'AppHeader',
  components: {
    Fold,
    Expand,
    Search,
    FullScreen,
    ScaleToOriginal,
    Bell,
    InfoFilled
  },
  emits: ['toggle-sidebar'],
  setup(props, { emit }) {
    const route = useRoute();
    const router = useRouter();
    const authStore = useAuthStore();
    
    // 侧边栏状态
    const isCollapse = ref(false);
    
    // 全屏状态
    const isFullScreen = ref(false);
    
    // 搜索关键字
    const searchKeyword = ref('');
    
    // 生成面包屑
    const breadcrumbs = computed(() => {
      const result = [];
      if (route.path === '/') return result;
      
      const matched = route.matched.filter(item => item.meta && item.meta.title);
      
      for (const item of matched) {
        result.push({
          path: item.path,
          title: item.meta.title
        });
      }
      
      return result;
    });
    
    // 模拟通知数据
    const notifications = ref([
      {
        id: 1,
        title: '系统通知',
        content: '您有新的借出物品请求',
        time: '10分钟前',
        isRead: false
      },
      {
        id: 2,
        title: '维护通知',
        content: '您的物品「笔记本电脑」需要进行例行维护',
        time: '1小时前',
        isRead: true
      },
      {
        id: 3,
        title: '到期提醒',
        content: '您的物品「手机」保修即将到期',
        time: '2小时前',
        isRead: false
      }
    ]);
    
    // 未读通知数量
    const unreadCount = computed(() => {
      return notifications.value.filter(item => !item.isRead).length;
    });
    
    // 切换侧边栏
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value;
      emit('toggle-sidebar');
    };
    
    // 切换全屏
    const toggleFullScreen = () => {
      const element = document.documentElement;
      
      if (!isFullScreen.value) {
        if (element.requestFullscreen) {
          element.requestFullscreen();
        }
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        }
      }
    };
    
    // 处理全屏变化事件
    const handleFullScreenChange = () => {
      isFullScreen.value = !!document.fullscreenElement;
    };
    
    // 处理搜索
    const handleSearch = () => {
      if (searchKeyword.value) {
        router.push({
          path: '/search',
          query: { q: searchKeyword.value }
        });
        searchKeyword.value = '';
      }
    };
    
    // 处理通知点击
    const handleNotification = (notification: any) => {
      // 标记通知为已读
      const index = notifications.value.findIndex(item => item.id === notification.id);
      if (index !== -1) {
        notifications.value[index].isRead = true;
      }
      
      // 导航到对应页面（实际项目中根据通知类型进行处理）
      if (notification.title.includes('借出')) {
        router.push('/lending');
      } else if (notification.title.includes('维护')) {
        router.push('/entities');
      } else if (notification.title.includes('到期')) {
        router.push('/reminder');
      }
    };
    
    // 查看所有通知
    const viewAllNotifications = () => {
      router.push('/reminder');
    };
    
    // 标记所有通知为已读
    const markAllAsRead = (e: Event) => {
      e.stopPropagation();
      notifications.value.forEach(item => {
        item.isRead = true;
      });
    };
    
    onMounted(() => {
      // 监听全屏变化
      document.addEventListener('fullscreenchange', handleFullScreenChange);
    });
    
    onBeforeUnmount(() => {
      // 移除全屏变化监听
      document.removeEventListener('fullscreenchange', handleFullScreenChange);
    });
    
    return {
      isCollapse,
      isFullScreen,
      searchKeyword,
      breadcrumbs,
      notifications,
      unreadCount,
      toggleSidebar,
      toggleFullScreen,
      handleSearch,
      handleNotification,
      viewAllNotifications,
      markAllAsRead
    };
  }
});
</script>

<style scoped>
.app-header {
  height: 50px;
  line-height: 50px;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
}

.left-area {
  display: flex;
  align-items: center;
}

.hamburger-container {
  padding: 0 15px;
  cursor: pointer;
  transition: background-color 0.3s;
  line-height: 50px;
  height: 50px;
}

.hamburger-container:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.is-active {
  transform: rotate(180deg);
}

.right-menu {
  display: flex;
  align-items: center;
}

.search-container {
  margin-right: 15px;
}

.el-input {
  width: 180px;
  transition: width 0.3s;
}

.el-input:focus {
  width: 220px;
}

.right-menu-item {
  padding: 0 8px;
  cursor: pointer;
  transition: background-color 0.3s;
  line-height: 50px;
  height: 50px;
  display: flex;
  align-items: center;
}

.hover-effect:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

/* 通知样式 */
.notification-title {
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-container {
  max-height: 300px;
  overflow-y: auto;
  width: 300px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 15px;
}

.notification-item.is-read {
  opacity: 0.7;
}

.notification-content {
  margin-left: 10px;
  font-size: 13px;
}

.notification-desc {
  color: #666;
  margin: 5px 0;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-footer {
  padding: 10px 15px;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-container {
    display: none;
  }
}
</style> 