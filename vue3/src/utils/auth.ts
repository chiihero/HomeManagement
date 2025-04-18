/**
 * 身份验证工具模块
 * 提供token管理、权限检查等身份验证相关功能
 */
import Cookies from "js-cookie";
import { useUserStoreHook } from "@/store/modules/user";
import { storageLocal, isString, isIncludeAllChildren } from "@pureadmin/utils";

/**
 * 用户数据信息接口
 * 定义用户相关的数据结构
 */
export interface DataInfo<T> {
  /** 访问令牌 */
  accessToken: string;
  /** `accessToken`的过期时间（时间戳） */
  expires: T;
  /** 用于调用刷新accessToken的接口时所需的token */
  refreshToken: string;
  /** 用户头像 */
  avatar?: string;
  /** 用户ID */
  userId?: string;
  /** 用户名 */
  username?: string;
  /** 用户昵称 */
  nickname?: string;
  /** 当前登录用户的角色 */
  roles?: Array<string>;
  /** 当前登录用户的按钮级别权限 */
  permissions?: Array<string>;
}

/** 用户信息存储键名 */
export const userKey = "user-info";

/** 令牌存储键名 */
export const TokenKey = "authorized-token";

/**
 * 多标签页会话状态键名
 * 通过`multiple-tabs`是否在`cookie`中，判断用户是否已经登录系统，
 * 从而支持多标签页打开已经登录的系统后无需再登录。
 * 浏览器完全关闭后`multiple-tabs`将自动从`cookie`中销毁，
 * 再次打开浏览器需要重新登录系统
 */
export const multipleTabsKey = "multiple-tabs";

/**
 * 获取用户token
 * @returns 用户token信息对象
 */
export function getToken(): DataInfo<number> {
  // 此处与`TokenKey`相同，此写法解决初始化时`Cookies`中不存在`TokenKey`报错
  return Cookies.get(TokenKey)
    ? JSON.parse(Cookies.get(TokenKey))
    : storageLocal().getItem(userKey);
}

/**
 * 设置用户token和用户信息
 * @description 设置`token`以及一些必要信息并采用无感刷新`token`方案
 * 无感刷新：后端返回`accessToken`（访问接口使用的`token`）、`refreshToken`（用于调用刷新`accessToken`的接口时所需的`token`，`refreshToken`的过期时间（比如30天）应大于`accessToken`的过期时间（比如2小时））、`expires`（`accessToken`的过期时间）
 * 将`accessToken`、`expires`、`refreshToken`这三条信息放在key值为authorized-token的cookie里（过期自动销毁）
 * 将`avatar`、`username`、`nickname`、`roles`、`permissions`、`refreshToken`、`expires`这七条信息放在key值为`user-info`的localStorage里（利用`multipleTabsKey`当浏览器完全关闭后自动销毁）
 * @param data - 包含token和用户信息的数据对象
 */
export function setToken(data: DataInfo<Date>) {
  let expires = 0;
  const { accessToken, refreshToken } = data;
  const { isRemembered, loginDay } = useUserStoreHook();
  expires = new Date(data.expires).getTime(); // 如果后端直接设置时间戳，将此处代码改为expires = data.expires，然后把上面的DataInfo<Date>改成DataInfo<number>即可
  const cookieString = JSON.stringify({ accessToken, expires, refreshToken });

  expires > 0
    ? Cookies.set(TokenKey, cookieString, {
        expires: (expires - Date.now()) / 86400000
      })
    : Cookies.set(TokenKey, cookieString);

  Cookies.set(
    multipleTabsKey,
    "true",
    isRemembered
      ? {
          expires: loginDay
        }
      : {}
  );

  /**
   * 设置用户基本信息到本地存储
   * @param param0 - 用户信息对象
   */
  function setUserKey({ avatar, userId, username, nickname, roles, permissions }) {
    useUserStoreHook().SET_AVATAR(avatar);
    useUserStoreHook().SET_USERID(userId);
    useUserStoreHook().SET_USERNAME(username);
    useUserStoreHook().SET_NICKNAME(nickname);
    useUserStoreHook().SET_ROLES(roles);
    useUserStoreHook().SET_PERMS(permissions);
    storageLocal().setItem(userKey, {
      expires,
      avatar,
      userId,
      username,
      nickname,
      roles,
      permissions
    });
  }

  // 如果有用户ID和角色信息，使用传入的数据
  if (data.userId && data.roles) {
    const { username, roles } = data;
    setUserKey({
      avatar: data?.avatar ?? "",
      userId: data?.userId ?? "",
      username,
      nickname: data?.nickname ?? "",
      roles,
      permissions: data?.permissions ?? []
    });
  } else {
    // 否则使用本地存储中已有的用户信息
    const avatar =
      storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "";
    const userId =
      storageLocal().getItem<DataInfo<number>>(userKey)?.userId ?? "";
    const username =
      storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "";
    const nickname =
      storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "";
    const roles =
      storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [];
    const permissions =
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [];
    setUserKey({
      avatar,
      userId,
      username,
      nickname,
      roles,
      permissions
    });
  }
}

/**
 * 删除用户token和相关信息
 * 清除`token`以及key值为`user-info`的localStorage信息
 */
export function removeToken() {
  Cookies.remove(TokenKey);
  Cookies.remove(multipleTabsKey);
  storageLocal().removeItem(userKey);
}

/**
 * 格式化token为Bearer格式（jwt格式）
 * @param token - 原始token字符串
 * @returns 格式化后的Bearer token
 */
export const formatToken = (token: string): string => {
  return "Bearer " + token;
};

/**
 * 检查用户是否拥有指定权限（根据登录接口返回的`permissions`字段进行判断）
 * @param value - 权限标识符或权限数组
 * @returns 是否拥有权限
 */
export const hasPerms = (value: string | Array<string>): boolean => {
  if (!value) return false;
  const allPerms = "*:*:*";
  const { permissions } = useUserStoreHook();
  
  // 如果无权限信息，返回false
  if (!permissions) return false;
  
  // 如果有通配符权限，返回true
  if (permissions.length === 1 && permissions[0] === allPerms) return true;
  
  // 检查特定权限
  const isAuths = isString(value)
    ? permissions.includes(value)
    : isIncludeAllChildren(value, permissions);
  
  return isAuths ? true : false;
};
