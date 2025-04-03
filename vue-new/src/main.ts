import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/styles/main.css'
import { useAuthStore } from './store/modules/auth'
import { useThemeStore } from './store/modules/theme'

// 创建Pinia实例并使用持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 创建应用实例
const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(router)
app.use(pinia)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err)
  console.error('错误组件:', instance)
  console.error('错误信息:', info)
}

// 挂载应用
app.mount('#app')

// 初始化主题
const themeStore = useThemeStore()
themeStore.init()

// 恢复用户会话
const authStore = useAuthStore()
authStore.restoreSession()

// 监听网络状态变化
window.addEventListener('online', () => {
  console.log('网络已连接')
  // 可以在这里添加网络恢复后的操作，例如重新加载数据
})

window.addEventListener('offline', () => {
  console.log('网络已断开')
  // 可以在这里添加网络断开后的提示
})

// 开发环境日志控制
if (import.meta.env.DEV) {
  console.log('当前运行在开发环境')
} else {
  console.log = () => {}
  console.warn = () => {}
  console.error = (message: any) => {
    // 只保留错误日志，可以对接错误监控系统
  }
}
