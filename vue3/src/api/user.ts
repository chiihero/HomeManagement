/**
 * 用户相关API接口
 * 处理用户信息、个人资料、设置等用户相关功能
 */
import { http } from "@/utils/http";
import type { User } from "@/types/user";
import type { ResponseResult } from "@/types/http";

/**
 * 用户个人资料接口
 */
export interface UserProfile {
  /** 用户ID */
  userId?: number;
  /** 用户名 */
  username: string;
  /** 用户昵称 */
  nickname: string;
  /** 电子邮箱 */
  email: string;
  /** 电话号码 */
  phone: string;
  /** 头像URL */
  avatar: string;
}

/**
 * 用户通知设置接口
 */
export interface UserNotificationSettings {
  /** 是否开启邮件通知 */
  emailNotification: boolean;
  /** 是否开启到期提醒 */
  expirationReminder: boolean;
  /** 提前提醒天数 */
  reminderDays: number;
}

/**
 * 用户设置接口
 */
export interface UserSettings {
  /** 设置ID */
  id?: number;
  /** 用户ID */
  userId: string;
  /** 设置键名 */
  key: string;
  /** 设置值 */
  value: string;
  /** 更新时间 */
  updatedAt?: string;
}

/**
 * 获取当前用户信息
 * @returns 包含用户信息的响应
 */
export function getUserInfo(): Promise<ResponseResult<Record<string, any>>> {
  return http.get("/users/info");
}

/**
 * 更新用户基本信息
 * @param data - 要更新的用户数据
 * @returns 更新后的用户信息
 */
export function updateUserInfo(
  data: Partial<User>
): Promise<ResponseResult<User>> {
  return http.post("/auth/update-info", {data: data});
}

/**
 * 更新用户个人资料
 * @param profile - 要更新的个人资料数据
 * @returns 更新后的个人资料
 */
export const updateUserProfile = (
  profile: Partial<UserProfile>
): Promise<ResponseResult<UserProfile>> => {
  return http.put("/users/profile", {data: profile});
};

/**
 * 更新用户密码
 * @param params - 包含当前密码和新密码的对象
 * @returns 更新结果
 */
export const updateUserPassword = (params: {
  
  currentPassword: string;
  newPassword: string;
}): Promise<ResponseResult<boolean>> => {
  return http.put("/users/password", {data: {userId, ...params}});
};

/**
 * 上传用户头像
 * @param file - 要上传的图片文件
 * @returns 上传后的头像URL
 */
export const uploadUserAvatar = (
  file: File
): Promise<ResponseResult<string>> => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("userId", userId);

  return http.post("/users/avatar", {data: formData}, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
};

/**
 * 删除用户头像
 * @returns 删除结果
 */
export const deleteUserAvatar = (): Promise<ResponseResult<boolean>> => {
  return http.delete("/users/avatar", {data: {userId}});
};

/**
 * 更新用户通知设置
 * @param data - 通知设置数据
 * @returns 更新后的通知设置
 */
export const updateUserNotifications = (
  data: UserNotificationSettings
): Promise<ResponseResult<UserNotificationSettings>> => {
  return http.put("/user/notifications",  {data: {userId, ...data}});
};

/**
 * 获取用户设置
 * @returns 用户所有设置项
 */
export const getUserSettings = (): Promise<ResponseResult<UserSettings[]>> => {
  return http.get("/settings/user", {data: {userId}});
};

/**
 * 更新特定用户设置
 * @param key - 设置键名
 * @param value - 设置值
 * @returns 更新后的设置项
 */
export const updateUserSetting = (
  key: string,
  value: string
): Promise<ResponseResult<UserSettings>> => {
  return http.put(`/settings/user/${key}`, { data: {userId, value} });
};
