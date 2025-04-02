import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import router from '../router'
import { ElMessage } from 'element-plus'

// 创建axios实例
const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    // 如果有token，则添加到请求头
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 确保非FormData类型的请求体被正确序列化为JSON
    if (config.data && 
        !(config.data instanceof FormData)) {
      if (config.method?.toLowerCase() === 'post' || config.method?.toLowerCase() === 'put') {
        config.headers['Content-Type'] = 'application/json';
        config.data = JSON.stringify(config.data);
      }
    }
    
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse) => {
    // 如果响应中包含成功消息，显示给用户
    if (response.data && response.data.msg && response.status === 200) {
      ElMessage.success(response.data.msg)
    }
    return response.data
  },
  (error: AxiosError) => {
    if (error.response) {
      const status = error.response.status
      
      // 显示错误消息
      if (error.response.data && typeof error.response.data === 'object' && 'msg' in error.response.data) {
        ElMessage.error(error.response.data.msg as string)
      }
      
      // 处理401错误（未授权）
      if (status === 401) {
        // 清除token
        localStorage.removeItem('token')
        // 如果当前不在登录页面，则重定向到登录页
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
      }
      
      // 处理403错误（禁止访问）
      if (status === 403) {
        console.error('没有权限访问此资源')
        ElMessage.error('没有权限访问此资源')
      }
      
      // 处理404错误（资源不存在）
      if (status === 404) {
        console.error('请求的资源不存在')
        ElMessage.error('请求的资源不存在')
      }
      
      // 处理500错误（服务器错误）
      if (status === 500) {
        console.error('服务器错误，请稍后重试')
        ElMessage.error('系统错误，请稍后重试')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('网络错误，无法连接到服务器')
      ElMessage.error('网络连接错误，请检查您的网络后重试')
    } else {
      // 请求配置有误
      console.error('请求错误:', error.message)
      ElMessage.error('系统错误，请稍后重试')
    }
    
    return Promise.reject(error)
  }
)

export default http 