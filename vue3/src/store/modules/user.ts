import { defineStore } from "pinia";
import {
  type userType,
  store,
  router,
  resetRouter,
  routerArrays
} from "../utils";
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, getToken, setToken, removeToken, userKey } from "@/utils/auth";
import { storageLocal } from "@pureadmin/utils";
import { login, logout, refreshToken, register } from "@/api/auth";
import { getUserInfo, updateUserInfo } from "@/api/user";
import { ElMessage } from "element-plus";
import type { User } from "@/types/user";
import type { ResponseResult } from "@/types/http";

/**
 * 用户状态接口，继承自基础userType并添加额外的属性
 */
interface UserState extends userType {
  /** 加载状态标识 */
  loading: boolean;
  /** 用户最后活动时间戳 */
  lastActivity: number;
}

/**
 * 检查Token是否过期
 * @param expiry - Token过期时间戳
 * @returns 如果Token已过期或即将过期返回true，否则返回false
 */
function isTokenExpired(expiry: number | null): boolean {
  if (!expiry) return true;
  // 提前5分钟视为过期，以便有时间刷新
  return Date.now() > expiry - 5 * 60 * 1000;
}

export const useUserStore = defineStore("pure-user", {
  state: (): UserState => ({
    // 用户头像
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    // 用户ID
    userId: storageLocal().getItem<DataInfo<number>>(userKey)?.userId ?? "",
    // 用户昵称
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    // 用户角色列表，用于页面级权限控制
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 用户权限列表，用于按钮级权限控制
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    // 是否勾选了登录页的免登录选项
    isRemembered: false,
    // 免登录状态保存天数，默认7天
    loginDay: 7,

    // 加载状态标识
    loading: false,
    // 用户最后活动时间戳
    lastActivity: Date.now()
  }),
  getters: {
    /**
     * 判断用户是否已认证
     * @returns 用户是否已认证且Token未过期
     */
    isAuthenticated(): boolean {
      return !!this.token && !isTokenExpired(this.tokenExpiry);
    },
    /**
     * 获取当前用户信息
     * @returns 当前用户对象或null
     */
    currentUser(): User | null {
      return this.userId;
    },
    /**
     * 检查用户是否有指定权限
     * @returns 用于检查特定权限的函数
     */
    hasPermission(): (permission: string) => boolean {
      return (permission: string) => {
        // 如果无用户信息，无权限
        if (!this.userId) return false;

        // 管理员拥有所有权限
        if (this.roles.includes("ADMIN")) return true;

        // 基于权限字符串匹配
        return this.permissions.includes(permission);
      };
    }
  },
  actions: {
    /** 
     * 设置用户头像
     * @param avatar - 头像URL
     */
    SET_AVATAR(avatar: string) {
      this.avatar = avatar;
    },
    /** 
     * 设置用户ID
     * @param userId - 用户ID
     */
    SET_USERID(userId: string) {
      this.userId = userId;
    },
    /** 
     * 设置用户名
     * @param username - 用户名
     */
    SET_USERNAME(username: string) {
      this.username = username;
    },
    /** 
     * 设置用户昵称
     * @param nickname - 用户昵称
     */
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    /** 
     * 设置用户角色
     * @param roles - 角色数组
     */
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    /** 
     * 设置用户权限
     * @param permissions - 权限数组
     */
    SET_PERMS(permissions: Array<string>) {
      this.permissions = permissions;
    },
    /** 
     * 设置记住登录状态
     * @param bool - 是否记住登录
     */
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    /** 
     * 设置免登录天数
     * @param value - 天数
     */
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
    /**
     * 更新用户最后活动时间
     */
    updateLastActivity() {
      this.lastActivity = Date.now();
    },

    /**
     * 设置认证信息
     * @param token - 访问令牌
     * @param refreshTokenValue - 刷新令牌
     * @param expiresIn - 过期时间（秒）
     */
    setAuth(token: string, refreshTokenValue: string, expiresIn: number) {
      // 转换过期时间为时间戳
      this.tokenExpiry = Date.now() + expiresIn * 1000;

      // 同时设置token到本地存储
      setToken({
        accessToken: token,
        refreshToken: refreshTokenValue,
        expires: new Date(this.tokenExpiry)
      });

      // 更新最后活动时间
      this.updateLastActivity();
    },

    /**
     * 清除认证信息
     */
    clearAuth() {
      // 清除本地存储的token信息
      removeToken();
    },

    /** 
     * 设置用户信息并保存到本地存储
     * @param data - 用户信息对象
     */
    setUserInfo(data: {
      avatar?: string;
      userId?: string;
      username?: string;
      nickname?: string;
      roles?: Array<string>;
      permissions?: Array<string>;
      refreshToken?: string;
      expires?: number;
    }) {
      // 更新store中的状态
      if (data.avatar !== undefined) this.SET_AVATAR(data.avatar);
      if (data.userId !== undefined) this.SET_USERID(data.userId);
      if (data.username !== undefined) this.SET_USERNAME(data.username);
      if (data.nickname !== undefined) this.SET_NICKNAME(data.nickname);
      if (data.roles !== undefined) this.SET_ROLES(data.roles);
      if (data.permissions !== undefined) this.SET_PERMS(data.permissions);

      // 保存到本地存储
      storageLocal().setItem(userKey, {
        refreshToken: data.refreshToken || "",
        expires: data.expires || 0,
        avatar: data.avatar || "",
        userId: data.userId || "",
        username: data.username || "",
        nickname: data.nickname || "",
        roles: data.roles || [],
        permissions: data.permissions || []
      });
    },

    /**
     * 刷新用户Token
     * @returns 刷新是否成功
     */
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
        console.error("Token刷新错误:", error);
        this.clearAuth();
        return false;
      } finally {
        this.loading = false;
      }
    },

    /** 
     * 使用用户名密码登录
     * @param data - 登录信息对象
     * @returns 登录结果
     */
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

          // 保存用户信息到本地存储
          this.setUserInfo({
            avatar: user.avatar || "",
            userId: user.id,
            username: user.username,
            nickname: user.username, // 如果后端没有提供nickname，使用username代替
            roles: [user.role],      // 将用户角色转为数组格式
            permissions: [],         // 根据实际情况填充权限列表
            refreshToken,
            expires: Date.now() + expiresIn * 1000
          });

          // 记住登录状态
          this.SET_ISREMEMBERED(!!data.remember);

          ElMessage.success("登录成功");
          return { success: true, data: response.data };
        }

        ElMessage.error(response.message || "登录失败，请检查用户名和密码");
        return { success: false, data: null };
      } catch (error) {
        console.error("登录错误:", error);
        ElMessage.error("登录过程中发生错误，请稍后重试");
        return { success: false, data: null };
      } finally {
        this.loading = false;
      }
    },

    /** 
     * 获取用户信息
     * @returns 获取结果，成功返回true，失败返回false
     */
    async fetchUserInfo(): Promise<boolean> {
      const token = getToken();
      if (!token) return false;

      this.loading = true;
      try {
        const response = await getUserInfo();

        if (response.code === 200 && response.data) {
          // 将后端返回的用户信息转换为前端用户模型
          const userInfo = response.data;

          // 保存用户信息到本地存储
          this.setUserInfo({
            avatar: userInfo.avatar || "",
            userId: userInfo.id,
            username: userInfo.username,
            nickname: userInfo.username, // 如果后端没有提供nickname，使用username代替
            roles: [userInfo.role],      // 将用户角色转为数组格式
            permissions: []              // 根据实际情况填充权限列表
          });
          
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
          this.logOut();
          return false;
        }
      } catch (error) {
        console.error("获取用户信息错误:", error);
        this.logOut();
        return false;
      } finally {
        this.loading = false;
      }
    },

    /** 
     * 用户登出
     * @param silent - 是否静默登出，默认为false
     */
    async logOut(silent: boolean = false) {
      try {
        // 如果不是静默登出，则调用登出接口
        if (!silent && this.token) {
          await logout();
        }
      } catch (error) {
        console.error("登出错误:", error);
      } finally {
        // 清除用户信息和认证信息
        this.clearAuth();
        // 清空用户信息
        this.setUserInfo({
          avatar: "",
          userId: "",
          username: "",
          nickname: "",
          roles: [],
          permissions: []
        });

        // 重置路由和标签
        useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
        resetRouter();

        // 跳转到登录页
        router.push("/auth/login");
      }
    },

    /** 
     * 刷新token（兼容PureAdmin的接口实现）
     * @param data - 传入的参数
     * @returns 刷新结果
     */
    async handRefreshToken(data: any) {
      // 调用内部刷新token方法
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

    /** 
     * 检查并自动刷新token
     * @param silent - 是否静默刷新，默认为false
     * @returns 检查结果
     */
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

    /**
     * 注册新用户
     * @param data - 注册信息
     * @returns 注册结果
     */
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
        console.error("注册错误:", error);
        return { success: false, message: "注册过程发生错误" };
      } finally {
        this.loading = false;
      }
    }
  }
});

/**
 * 用户Store钩子函数，用于在组件外部访问用户Store
 * @returns 用户Store实例
 */
export function useUserStoreHook() {
  return useUserStore(store);
}
