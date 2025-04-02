import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

// 定义路由
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Dashboard' }
      },
      {
        path: 'entities',
        name: 'Entities',
        component: () => import('@/views/entity/index.vue'),
        meta: { title: '物品管理', icon: 'Box' }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/tag/index.vue'),
        meta: { title: '标签管理', icon: 'Collection' }
      },
      {
        path: 'reminder',
        name: 'Reminder',
        component: () => import('@/views/reminder/index.vue'),
        meta: { title: '提醒管理', icon: 'Bell' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/setting/index.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', hideInMenu: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', hideInMenu: true }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hideInMenu: true }
  }
]

// 创建路由
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '家庭物品管理系统'}`;
  
  // 获取token
  const token = localStorage.getItem('token');
  
  // 如果访问的是登录页或注册页，且已有token，则跳转到首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next({ path: '/' });
    return;
  }
  
  // 如果访问的不是登录页或注册页，且没有token，则跳转到登录页
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next({ path: '/login' });
    return;
  }
  
  next();
})

export default router 