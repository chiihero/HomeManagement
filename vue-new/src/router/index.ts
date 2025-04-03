import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../store/modules/auth';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

// 配置NProgress
NProgress.configure({ 
  showSpinner: false,
  minimum: 0.1,
  easing: 'ease',
  speed: 500
});

// 路由懒加载辅助函数
const lazyLoad = (componentPath: string) => {
  // 必须使用静态导入路径，不能使用模板字符串
  if (componentPath === 'views/auth/Login.vue') {
    return () => import('../views/auth/Login.vue');
  } else if (componentPath === 'views/auth/Register.vue') {
    return () => import('../views/auth/Register.vue');
  } else if (componentPath === 'views/error/404.vue') {
    return () => import('../views/error/404.vue');
  } else if (componentPath === 'views/dashboard/index.vue') {
    return () => import('../views/dashboard/index.vue');
  } else if (componentPath === 'views/entity/index.vue') {
    return () => import('../views/entity/index.vue');
  } else if (componentPath === 'views/entitySearch/index.vue') {
    return () => import('../views/entitySearch/index.vue');
  } else if (componentPath === 'views/tag/index.vue') {
    return () => import('../views/tag/index.vue');
  } else if (componentPath === 'views/reminder/index.vue') {
    return () => import('../views/reminder/index.vue');
  } else if (componentPath === 'views/setting/index.vue') {
    return () => import('../views/setting/index.vue');
  } else if (componentPath === 'components/layout/Layout.vue') {
    return () => import('../components/layout/Layout.vue');
  } else {
    console.error(`无法加载组件: ${componentPath}`);
    return () => import('../views/error/404.vue');
  }
};

// 定义路由
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Layout',
    component: lazyLoad('components/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: lazyLoad('views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Dashboard', requiresAuth: true, keepAlive: true }
      },
      {
        path: 'entities',
        name: 'Entities',
        component: lazyLoad('views/entity/index.vue'),
        meta: { title: '物品结构', icon: 'Box', requiresAuth: true }
      },
      {
        path: 'entitiesSearch',
        name: 'EntitiesSearch',
        component: lazyLoad('views/entitySearch/index.vue'),
        meta: { title: '物品搜索', icon: 'Box', requiresAuth: true }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: lazyLoad('views/tag/index.vue'),
        meta: { title: '标签管理', icon: 'Collection', requiresAuth: true }
      },
      { 
        path: 'reminder',
        name: 'Reminder',
        component: lazyLoad('views/reminder/index.vue'),
        meta: { title: '提醒管理', icon: 'Bell', requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: lazyLoad('views/setting/index.vue'),
        meta: { title: '系统设置', icon: 'Setting', requiresAuth: true }
      },
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: lazyLoad('views/auth/Login.vue'),
    meta: { title: '登录', hideInMenu: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: lazyLoad('views/auth/Register.vue'),
    meta: { title: '注册', hideInMenu: true }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: lazyLoad('views/error/404.vue'),
    meta: { title: '404', hideInMenu: true }
  }
]

// 创建路由
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // 平滑滚动
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0, behavior: 'smooth' };
    }
  }
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 开始加载进度条
  NProgress.start();
  
  // 设置页面标题
  document.title = `${to.meta.title || '家庭物品管理系统'}`;
  
  const authStore = useAuthStore();
  
  // 如果有token，检查token是否过期
  if (authStore.token) {
    await authStore.checkTokenExpiration(true);
  }
  
  // 如果访问的是登录页或注册页，且已有token，则跳转到首页
  if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    next({ path: '/' });
    NProgress.done();
    return;
  }
  
  // 如果需要认证但未登录，跳转到登录页
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    NProgress.done();
    return;
  }
  
  // 如果用户已登录但没有用户信息，请求用户信息
  if (authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.fetchUserInfo();
    } catch (error) {
      console.error('Failed to fetch user info:', error);
    }
  }
  
  // 更新用户最后活动时间
  if (authStore.isAuthenticated) {
    authStore.updateLastActivity();
  }
  
  next();
})

// 全局后置钩子
router.afterEach(() => {
  // 结束加载进度条
  NProgress.done();
})

export default router 