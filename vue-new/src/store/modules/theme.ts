import { defineStore } from 'pinia'

// 主题模式类型
export type ThemeMode = 'light' | 'dark' | 'system'

// CSS变量主题配置
interface ThemeVariables {
  [key: string]: { light: string; dark: string }
}

// 主题状态接口
interface ThemeState {
  mode: ThemeMode
  isDark: boolean
  variables: ThemeVariables
  systemMediaQuery: MediaQueryList | null
  mediaQueryHandler: ((e: MediaQueryListEvent) => void) | null
}

// 默认主题变量
const defaultThemeVariables: ThemeVariables = {
  '--primary-color': { light: '#1677ff', dark: '#177ddc' },
  '--secondary-color': { light: '#5a5a5a', dark: '#a3a3a3' },
  '--background-color': { light: '#ffffff', dark: '#141414' },
  '--text-color': { light: '#1f1f1f', dark: '#e0e0e0' },
  '--border-color': { light: '#f0f0f0', dark: '#303030' },
}

export const useThemeStore = defineStore('theme', {
  state: (): ThemeState => {
    // 从本地存储读取主题模式，默认为system
    const savedMode = localStorage.getItem('theme-mode') as ThemeMode
    const validModes: ThemeMode[] = ['light', 'dark', 'system']
    const mode = validModes.includes(savedMode) ? savedMode : 'system'
    
    // 获取当前是否为深色模式
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)')
    const isDark = mode === 'dark' || 
      (mode === 'system' && prefersDark.matches)

    return {
      mode,
      isDark,
      variables: defaultThemeVariables,
      systemMediaQuery: null,
      mediaQueryHandler: null
    }
  },
  
  actions: {
    // 设置主题模式
    setMode(newMode: ThemeMode) {
      // 如果模式相同，则不做任何处理
      if (this.mode === newMode) return
      
      this.mode = newMode
      
      // 根据模式设置isDark状态
      if (newMode === 'system') {
        if (this.systemMediaQuery) {
          this.isDark = this.systemMediaQuery.matches
        }
      } else {
        this.isDark = newMode === 'dark'
      }
      
      // 保存到本地存储
      localStorage.setItem('theme-mode', newMode)
      
      // 应用主题
      this.applyTheme()
    },
    
    // 手动切换亮色/暗色模式
    toggleDarkMode() {
      // 如果当前是system模式，切换到具体的light/dark模式
      if (this.mode === 'system') {
        this.setMode(this.isDark ? 'light' : 'dark')
      } else {
        // 否则直接在light和dark之间切换
        this.setMode(this.mode === 'light' ? 'dark' : 'light')
      }
    },
    
    // 自定义主题变量
    setThemeVariable(name: string, lightValue: string, darkValue: string) {
      this.variables[name] = { light: lightValue, dark: darkValue }
      this.applyTheme()
    },
    
    // 重置主题变量为默认值
    resetThemeVariables() {
      this.variables = { ...defaultThemeVariables }
      this.applyTheme()
    },
    
    // 应用所有主题变量到DOM
    applyTheme() {
      // 设置根数据属性
      document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : 'light')
      
      // 应用CSS变量
      const cssMode = this.isDark ? 'dark' : 'light'
      Object.entries(this.variables).forEach(([varName, values]) => {
        document.documentElement.style.setProperty(varName, values[cssMode])
      })
      
      // 发布主题变更事件，让其他组件可以响应
      window.dispatchEvent(new CustomEvent('theme-change', { 
        detail: { isDark: this.isDark, mode: this.mode }
      }))
    },
    
    // 初始化主题，监听系统主题变化
    init() {
      // 清理之前的监听器（如果有）
      this.cleanupListeners()
      
      // 创建系统主题媒体查询
      this.systemMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      
      // 定义媒体查询处理函数
      this.mediaQueryHandler = (e: MediaQueryListEvent) => {
        if (this.mode === 'system') {
          this.isDark = e.matches
          this.applyTheme()
        }
      }
      
      // 添加监听器
      this.systemMediaQuery.addEventListener('change', this.mediaQueryHandler)
      
      // 应用初始主题
      this.applyTheme()
    },
    
    // 清理监听器
    cleanupListeners() {
      if (this.systemMediaQuery && this.mediaQueryHandler) {
        this.systemMediaQuery.removeEventListener('change', this.mediaQueryHandler)
        this.systemMediaQuery = null
        this.mediaQueryHandler = null
      }
    }
  },
  
  // 在store销毁时清理监听器
  onDestroy() {
    this.cleanupListeners()
  }
}) 