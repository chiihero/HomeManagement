import { defineStore } from 'pinia';
import { login, register, logout, getUserInfo } from '@/api/auth';
import { User } from '@/types/user';
import router from '@/router';

// 定义认证状态接口
export interface AuthState {
  token: string | null;
  user: User | null;
  loading: boolean;
}

// 定义认证store
export const useAuthStore = defineStore('auth', {
  // 状态
  state: (): AuthState => ({
    token: localStorage.getItem('token') || null,
    user: null,
    loading: false
  }),
  
  // getters
  getters: {
    isAuthenticated(): boolean {
      return !!this.token;
    },
    currentUser(): User | null {
      return this.user;
    }
  },
  
  // actions
  actions: {
    // 设置token
    setToken(token: string | null) {
      this.token = token;
      if (token) {
        localStorage.setItem('token', token);
      } else {
        localStorage.removeItem('token');
      }
    },
    
    // 设置用户信息
    setUser(user: User | null) {
      this.user = user;
    },
    
    // 登录
    async login(username: string, password: string) {
      this.loading = true;
      try {
        const response = await login(username, password);
        console.log('Login response:', response);
        if (response && response.code === 200 && response.data) {
          const { token, user } = response.data;
          this.setToken(token);
          this.setUser(user);
          return true;
        }
        console.error('Login failed:', response);
        return false;
      } catch (error) {
        console.error('Login error:', error);
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
        if (response.data.code === 200) {
          // 注册成功后直接登录
          return await this.login(userData.username, userData.password);
        }
        return false;
      } catch (error) {
        console.error('Register error:', error);
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 获取用户信息
    async fetchUserInfo() {
      if (!this.token) return;
      
      this.loading = true;
      try {
        const response = await getUserInfo();
        if (response.data.code === 200) {
          this.setUser(response.data.data);
        } else {
          // 如果获取用户信息失败，可能是token过期
          this.logout();
        }
      } catch (error) {
        console.error('Fetch user info error:', error);
        this.logout();
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
        this.setToken(null);
        this.setUser(null);
        this.loading = false;
        router.push('/login');
      }
    }
  }
}); 