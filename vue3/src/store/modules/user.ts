import { defineStore } from "pinia";
import {
  type userType,
  store,
  router,
  resetRouter,
  routerArrays
} from "../utils";
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, setToken, removeToken, userKey } from "@/utils/auth";
import { storageLocal } from "@pureadmin/utils";
import { login, logout, refreshToken, register } from "@/api/auth";
import { getUserInfo, updateUserInfo } from "@/api/user";
import { ElMessage } from "element-plus";
import type { User } from "@/types/user";
import type { ResponseResult } from "@/types/http";

// 扩展userType类型
interface UserState extends userType {
  userId: number | null;
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

export const useUserStore = defineStore("pure-user", {
  state: (): UserState => ({
    // 头像
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    // 用户ID
    userId: null,
    // 昵称
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    // 页面级别权限
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 按钮级别权限
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    // 是否勾选了登录页的免登录
    isRemembered: false,
    // 登录页的免登录存储几天，默认7天
    loginDay: 7,
    // 认证相关状态
    token: localStorage.getItem("token") || null,
    refreshTokenValue: localStorage.getItem("refresh_token") || null,
    tokenExpiry: localStorage.getItem("token_expiry")
      ? parseInt(localStorage.getItem("token_expiry") || "0")
      : null,
    user: null,
    loading: false,
    lastActivity: Date.now()
  }),
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
        // 如果无用户信息，无权限
        if (!this.user) return false;

        // 管理员拥有所有权限
        if (this.roles.includes("ADMIN")) return true;

        // 基于角色和权限字符串匹配
        return this.permissions.includes(permission);
      };
    }
  },
  actions: {
    /** 存储头像 */
    SET_AVATAR(avatar: string) {
      this.avatar = avatar;
    },
    /** 存储用户名 */
    SET_USERNAME(username: string) {
      this.username = username;
    },
    /** 存储昵称 */
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    /** 存储角色 */
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    /** 存储按钮级别权限 */
    SET_PERMS(permissions: Array<string>) {
      this.permissions = permissions;
    },
    /** 存储是否勾选了登录页的免登录 */
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    /** 设置登录页的免登录存储几天 */
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
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
      localStorage.setItem("token", token);
      localStorage.setItem("refresh_token", refreshTokenValue);
      localStorage.setItem("token_expiry", this.tokenExpiry.toString());

      // 同时设置PureAdmin的token
      setToken({
        accessToken: token,
        refreshToken: refreshTokenValue,
        expires: new Date(this.tokenExpiry)
      });

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
      localStorage.removeItem("token");
      localStorage.removeItem("refresh_token");
      localStorage.removeItem("token_expiry");

      // 清除PureAdmin token
      removeToken();
    },

    // 设置用户信息
    setUser(user: User | null) {
      this.user = user;
      if (user) {
        // 设置用户基本信息
        this.userId = user.id;
        this.username = user.username;
        this.nickname = user.nickname || user.username;
        this.avatar = user.avatar || "";

        // 设置角色和权限信息
        const roleArray = [user.role.toLowerCase()];
        this.SET_ROLES(roleArray);

        // 缓存用户信息
        sessionStorage.setItem("user_info", JSON.stringify(user));
      } else {
        this.userId = null;
        this.username = "";
        this.nickname = "";
        this.avatar = "";
        this.roles = [];
        this.permissions = [];
        sessionStorage.removeItem("user_info");
      }
    },

    // 尝试恢复用户会话
    async restoreSession() {
      // 从缓存恢复用户信息
      const cachedUserInfo = sessionStorage.getItem("user_info");
      if (cachedUserInfo) {
        try {
          const user = JSON.parse(cachedUserInfo);
          this.setUser(user);
        } catch (e) {
          console.error("Failed to parse cached user info:", e);
          sessionStorage.removeItem("user_info");
        }
      }

      // 如果有token但已过期，尝试刷新
      if (
        this.token &&
        isTokenExpired(this.tokenExpiry) &&
        this.refreshTokenValue
      ) {
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
        if (router.currentRoute.value.path !== "/auth/login") {
          router.push("/auth/login");
          ElMessage.error("会话已过期，请重新登录");
        }
        return false;
      } catch (error) {
        console.error("Token refresh error:", error);
        this.clearAuth();
        return false;
      } finally {
        this.loading = false;
      }
    },

    /** 登录 */
    async loginByUsername(data: {
      username: string;
      password: string;
      remember?: boolean;
    }) {
      this.loading = true;
      try {
        const response = await login(
          data.username,
          data.password,
          data.remember || false
        );

        if (response.code === 200 && response.data) {
          const { token, refreshToken, expiresIn, user } = response.data;

          // 设置认证信息
          this.setAuth(token, refreshToken, expiresIn);

          // 设置用户信息
          const userInfo: User = {
            id: user.id,
            username: user.username,
            email: user.email,
            phone: user.phone,
            avatar: user.avatar,
            role: user.role,
            status: user.status,
            enabled: user.enabled,
            createdTime: user.createdTime
          };

          this.setUser(userInfo);

          // 记住登录状态
          this.SET_ISREMEMBERED(!!data.remember);

          ElMessage.success("登录成功");
          return { success: true, data: response.data };
        }

        ElMessage.error(response.message || "登录失败，请检查用户名和密码");
        return { success: false, data: null };
      } catch (error) {
        console.error("Login error:", error);
        ElMessage.error("登录过程中发生错误，请稍后重试");
        return { success: false, data: null };
      } finally {
        this.loading = false;
      }
    },

    /** 获取用户信息 */
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
            status: userInfo.status,
            enabled: userInfo.enabled,
            createdTime: userInfo.createdTime
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
        console.error("Fetch user info error:", error);
        this.logout();
        return false;
      } finally {
        this.loading = false;
      }
    },

    /** 前端登出（不调用接口） */
    async logOut(silent: boolean = false) {
      try {
        // 如果不是静默登出，则调用登出接口
        if (!silent && this.token) {
          await logout();
        }
      } catch (error) {
        console.error("Logout error:", error);
      } finally {
        // 清除用户信息和认证信息
        this.clearAuth();
        this.setUser(null);

        // 重置路由和标签
        useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
        resetRouter();

        // 跳转到登录页
        router.push("/auth/login");
      }
    },

    /** 刷新token（实现与原PureAdmin兼容） */
    async handRefreshToken(data: any) {
      // 直接调用前面实现的刷新token方法
      const success = await this.refreshUserToken();
      return {
        success,
        data: success
          ? {
              accessToken: this.token,
              refreshToken: this.refreshTokenValue,
              expires: new Date(this.tokenExpiry || 0)
            }
          : null
      };
    },

    /** 检查并自动刷新token */
    async checkTokenExpiration(silent: boolean = false) {
      // 如果token已过期或即将过期，且有refreshToken，尝试刷新
      if (
        this.token &&
        isTokenExpired(this.tokenExpiry) &&
        this.refreshTokenValue
      ) {
        if (!silent) {
          ElMessage.info("正在刷新授权...");
        }
        return await this.refreshUserToken();
      }
      return true;
    },

    // 注册新用户
    async register(data: {
      username: string;
      email: string;
      password: string;
    }): Promise<{ success: boolean; message?: string }> {
      this.loading = true;
      try {
        const res = await register(data);
        if (res.code === 200) {
          return { success: true };
        } else {
          return { success: false, message: res.message || "注册失败" };
        }
      } catch (error) {
        console.error("Register error:", error);
        return { success: false, message: "注册过程发生错误" };
      } finally {
        this.loading = false;
      }
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}
