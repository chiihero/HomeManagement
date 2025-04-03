import { defineStore } from 'pinia';
import { login, register, logout, getUserInfo, refreshToken } from '@/api/auth';
import { User } from '@/types/user';
import router from '@/router';
import { ElMessage } from 'element-plus';

// 定义认证状态接口
export interface AuthState {
  token: string | null;
  refreshTokenValue: string | null;
  tokenExpiry: number | null;
  user: User | null;
  loading: boolean;
  lastActivity: number;
}

// 检查Token是否过期
function isTokenExpired(expiry: number | null): boolean {
  if (!expiry) return true;
  // 提前5分钟视为过期，以便有时间刷新
  return Date.now() > expiry - 5 * 60 * 1000;
}

// 定义认证store
export const useAuthStore = defineStore('auth', {
  // 状态
  state: (): AuthState => ({
    token: localStorage.getItem('token') || null,
    refreshTokenValue: localStorage.getItem('refresh_token') || null,
    tokenExpiry: localStorage.getItem('token_expiry') ? parseInt(localStorage.getItem('token_expiry') || '0') : null,
    user: null,
    loading: false,
    lastActivity: Date.now()
  }),
  
  // getters
  getters: {
    isAuthenticated(): boolean {
      return !!this.token && !isTokenExpired(this.tokenExpiry);
    },
    currentUser(): User | null {
      return this.user;
    },
    /**
     * 检查用户是否有指定权限
     */
    hasPermission(): (permission: string) => boolean {
      return (permission: string) => {
        // 基于角色的简单权限检查
        if (!this.user) return false;
        
        // 如果是管理员，拥有所有权限
        if (this.user.role === 'ADMIN') return true;
        
        // 根据角色和权限字符串进行匹配
        // 实际项目中可能需要更复杂的权限管理逻辑
        return permission.startsWith(this.user.role);
      };
    }
  },
  
  // actions
  actions: {
    // 更新最后活动时间
    updateLastActivity() {
      this.lastActivity = Date.now();
    },
    
    // 设置认证信息
    setAuth(token: string, refreshTokenValue: string, expiresIn: number) {
      this.token = token;
      this.refreshTokenValue = refreshTokenValue;
      // 转换过期时间为时间戳
      this.tokenExpiry = Date.now() + expiresIn * 1000;
      
      // 保存到本地存储
      localStorage.setItem('token', token);
      localStorage.setItem('refresh_token', refreshTokenValue);
      localStorage.setItem('token_expiry', this.tokenExpiry.toString());
      
      // 更新最后活动时间
      this.updateLastActivity();
    },
    
    // 清除认证信息
    clearAuth() {
      this.token = null;
      this.refreshTokenValue = null;
      this.tokenExpiry = null;
      this.user = null;
      
      // 清除本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('token_expiry');
    },
    
    // 设置用户信息
    setUser(user: User | null) {
      this.user = user;
      // 可以选择将用户信息缓存到sessionStorage
      if (user) {
        sessionStorage.setItem('user_info', JSON.stringify(user));
      } else {
        sessionStorage.removeItem('user_info');
      }
    },
    
    // 尝试恢复用户会话
    async restoreSession() {
      // 先从缓存恢复用户信息
      const cachedUserInfo = sessionStorage.getItem('user_info');
      if (cachedUserInfo) {
        try {
          this.user = JSON.parse(cachedUserInfo);
        } catch (e) {
          console.error('Failed to parse cached user info:', e);
          sessionStorage.removeItem('user_info');
        }
      }
      
      // 如果有token，但token已过期或即将过期，尝试刷新
      if (this.token && isTokenExpired(this.tokenExpiry) && this.refreshTokenValue) {
        await this.refreshUserToken();
      }
      
      // 如果有token但没有用户信息，获取用户信息
      if (this.token && !this.user) {
        await this.fetchUserInfo();
      }
    },
    
    // 刷新用户Token
    async refreshUserToken() {
      if (!this.refreshTokenValue) return false;
      
      this.loading = true;
      try {
        const response = await refreshToken(this.refreshTokenValue);
        
        // 检查响应格式和状态码
        if (response.code === 200 && response.data) {
          const { token, refreshToken, expiresIn } = response.data;
          this.setAuth(token, refreshToken, expiresIn);
          return true;
        }
        
        // 如果刷新失败，清除认证信息并重定向到登录页
        this.clearAuth();
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login');
          ElMessage.error('会话已过期，请重新登录');
        }
        return false;
      } catch (error) {
        console.error('Token refresh error:', error);
        this.clearAuth();
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 登录
    async login(username: string, password: string, remember: boolean = false) {
      this.loading = true;
      try {
        const response = await login(username, password, remember);
        
        if (response.code === 200 && response.data) {
          const { token, refreshToken, expiresIn, user } = response.data;
          
          // 设置认证信息
          this.setAuth(token, refreshToken, expiresIn);
          
          // 设置用户信息
          // 将后端返回的用户信息转换为前端User类型
          const userInfo: User = {
            id: user.id,
            username: user.username,
            email: user.email,
            phone: user.phone,
            avatar: user.avatar,
            role: user.role,
            status: user.status
          };
          
          this.setUser(userInfo);
          ElMessage.success('登录成功');
          return true;
        }
        
        ElMessage.error(response.msg || '登录失败，请检查用户名和密码');
        return false;
      } catch (error) {
        console.error('Login error:', error);
        ElMessage.error('登录过程中发生错误，请稍后重试');
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 注册
    async register(userData: any) {
      this.loading = true;
      try {
        const response = await register(userData);
        if (response.code === 200) {
          ElMessage.success('注册成功，请登录');
          router.push('/login');
          return true;
        }
        ElMessage.error(response.msg || '注册失败，请稍后重试');
        return false;
      } catch (error) {
        console.error('Register error:', error);
        ElMessage.error('注册过程中发生错误，请稍后重试');
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 获取用户信息
    async fetchUserInfo(): Promise<boolean> {
      if (!this.token) return false;
      
      this.loading = true;
      try {
        const response = await getUserInfo();
        
        if (response.code === 200 && response.data) {
          // 将后端返回的用户信息转换为前端User类型
          const userInfo = response.data;
          
          const userData: User = {
            id: userInfo.id,
            username: userInfo.username,
            email: userInfo.email,
            phone: userInfo.phone,
            avatar: userInfo.avatar,
            role: userInfo.role,
            status: userInfo.status
          };
          
          this.setUser(userData);
          return true;
        } else {
          // 如果获取用户信息失败，尝试刷新Token
          if (this.refreshTokenValue) {
            const refreshed = await this.refreshUserToken();
            if (refreshed) {
              // 再次尝试获取用户信息
              return await this.fetchUserInfo();
            }
          }
          
          // 如果刷新Token失败或没有刷新Token，则登出
          this.logout();
          return false;
        }
      } catch (error) {
        console.error('Fetch user info error:', error);
        this.logout();
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 退出登录
    async logout() {
      this.loading = true;
      try {
        if (this.token) {
          await logout();
        }
      } catch (error) {
        console.error('Logout error:', error);
      } finally {
        this.clearAuth();
        this.loading = false;
        router.push('/login');
      }
    },
    
    // 检查并处理Token过期
    async checkTokenExpiration(silent: boolean = false) {
      // 如果没有token或已登出，不处理
      if (!this.token) return;
      
      // 检查token是否过期
      if (isTokenExpired(this.tokenExpiry)) {
        // 有刷新token则尝试刷新
        if (this.refreshTokenValue) {
          const refreshed = await this.refreshUserToken();
          if (!refreshed && !silent) {
            ElMessage.error('会话已过期，请重新登录');
            router.push('/login');
          }
        } else {
          // 没有刷新token，直接登出
          this.clearAuth();
          if (!silent) {
            ElMessage.error('会话已过期，请重新登录');
            router.push('/login');
          }
        }
      }
    }
  }
}); 