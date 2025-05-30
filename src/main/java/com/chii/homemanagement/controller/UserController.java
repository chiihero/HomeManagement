package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;

import com.chii.homemanagement.entity.SystemSetting;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.SystemSettingService;
import com.chii.homemanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户管理相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private SystemSettingService systemSettingService;
    
    /**
     * 获取当前用户信息 (适用于前端导航栏和认证检查)
     */
    @GetMapping("/info/{userId}")
    @Operation(summary = "获取当前用户基本信息", description = "获取当前登录用户的基本信息，用于导航栏显示和认证检查")
    public ApiResponse<Map<String, Object>> getUserInfo(
            @Parameter(description = "用户ID") @PathVariable(value = "userId") Long userId) {
        try {
            log.info("获取当前用户基本信息");
            // 获取用户实体信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            // 准备返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("roles", user.getRoles());
            userInfo.put("status", user.getStatus());
            
            log.info("获取当前用户基本信息成功: username={}", user.getUsername());
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人资料", description = "更新当前登录用户的个人资料")
    public ApiResponse<User> updateProfile(@RequestBody User userParam) {
        try {
            log.info("更新个人资料");
            

            // 获取用户实体信息
            User currentUser = userService.getUserById(userParam.getUserId());
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            // 更新个人资料
            User user = new User();
            user.setUserId(currentUser.getUserId());
            // 只允许更新以下字段
            user.setNickname(userParam.getNickname());
            user.setEmail(userParam.getEmail());
            user.setPhone(userParam.getPhone());
            user.setUpdateTime(LocalDateTime.now());

            userService.updateUser(user);
            
            // 更新session中的用户信息
            User updatedUser = userService.getUserById(currentUser.getUserId());
            
            // 清除密码
            updatedUser.setPassword(null);

            log.info("更新密码成功: username={}", updatedUser.getUsername());
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            log.error("更新个人资料异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    @Operation(summary = "更新密码", description = "更新当前登录用户的密码")
    public ApiResponse<Boolean> updatePassword(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId,
            @Parameter(description = "旧密码") @RequestParam(value = "currentPassword") String currentPassword,
            @Parameter(description = "新密码") @RequestParam(value = "newPassword") String newPassword
    ) {
        try {
            log.info("更新密码");
            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            if (currentPassword == null || newPassword == null) {
                return ApiResponse.error(ErrorCode.PARAM_IS_BLANK.getCode(), "当前密码和新密码不能为空");
            }

            // 验证当前密码
            if (!userService.validatePassword(currentUser.getUsername(), currentPassword)) {
                return ApiResponse.error(ErrorCode.USER_CREDENTIALS_ERROR.getCode(), "当前密码不正确");
            }

            // 更新密码
            User user = new User();
            user.setUserId(currentUser.getUserId());
            user.setPassword(newPassword);
            user.setUpdateTime(LocalDateTime.now());

            userService.updateUser(user);
            
            log.info("更新密码成功: username={}", user.getUsername());
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("更新密码异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新密码失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传当前登录用户的头像")
    public ApiResponse<String> uploadAvatar(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId,
            @Parameter(description = "图片") @RequestParam(value = "image") MultipartFile image) {
        try {
            log.info("上传头像");

            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            // 检查文件是否为空
            if (image.isEmpty()) {
                return ApiResponse.error(ErrorCode.PARAM_IS_BLANK.getCode(), "请选择要上传的文件");
            }
            
            User user = userService.uploadAvatar(currentUser.getUserId(), image);
            
            log.info("上传头像成功: username={}", currentUser.getUsername());
            return ApiResponse.success(user.getAvatar());
        } catch (Exception e) {
            log.error("上传头像异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/avatar/{userId}")
    @Operation(summary = "删除头像", description = "删除当前登录用户的头像")
    public ApiResponse<User> deleteAvatar(@Parameter(description = "用户ID") @PathVariable(value = "userId") Long userId) {
        try {
            log.info("删除头像");

            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            User user = userService.deeleteAvatar(currentUser.getUserId());
            
            log.info("删除头像成功: username={}", currentUser.getUsername());
            return ApiResponse.success(user);
        } catch (Exception e) {
            log.error("删除头像异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除头像失败: " + e.getMessage());
        }
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/notifications")
    @Operation(summary = "更新通知设置", description = "更新当前登录用户的通知设置")
    public ApiResponse<Boolean> updateNotifications(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId,
            @Parameter(description = "是否开启邮件通知") @RequestParam(value = "emailNotification") Boolean emailNotification,
            @Parameter(description = "是否开启到期提醒") @RequestParam(value = "expirationReminder") Boolean expirationReminder,
            @Parameter(description = "提前提醒天数") @RequestParam(value = "reminderDays") Integer reminderDays

            ) {
        try {
            log.info("更新通知设置: {},是否开启邮件通知: {},是否开启到期提醒: {},提前提醒天数: {}",userId,emailNotification,expirationReminder,reminderDays);

            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            // 保存邮件通知设置
            if (emailNotification) {
                SystemSetting emailNotificationSetting = new SystemSetting();
                emailNotificationSetting.setSettingKey("email_notification");
                emailNotificationSetting.setSettingValue(String.valueOf(emailNotification));
                emailNotificationSetting.setType("user");
                systemSettingService.saveUserSetting(emailNotificationSetting, currentUser.getUserId());
            }
            
            // 保存到期提醒设置
            if (expirationReminder) {
                SystemSetting expirationReminderSetting = new SystemSetting();
                expirationReminderSetting.setSettingKey("expiration_reminder");
                expirationReminderSetting.setSettingValue(String.valueOf(expirationReminder));
                expirationReminderSetting.setType("user");
                systemSettingService.saveUserSetting(expirationReminderSetting, currentUser.getUserId());
            }
            
            // 保存提前提醒天数设置
            if (reminderDays !=null) {
                SystemSetting reminderDaysSetting = new SystemSetting();
                reminderDaysSetting.setSettingKey("reminder_days");
                reminderDaysSetting.setSettingValue(String.valueOf(reminderDays));
                reminderDaysSetting.setType("user");
                systemSettingService.saveUserSetting(reminderDaysSetting, currentUser.getUserId());
            }
            
            log.info("更新通知设置成功: username={}", currentUser.getUsername());
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("更新通知设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新通知设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户通知设置
     */
    @GetMapping("/notifications")
    @Operation(summary = "获取用户通知设置", description = "获取当前登录用户的通知设置")
    public ApiResponse<Map<String, Object>> getNotifications(@Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        try {
            log.info("获取用户通知设置");

            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            Map<String, Object> userSettings = systemSettingService.getUserSettingsAsMap(currentUser.getUserId());
            
            // 如果没有设置，返回默认值
            Map<String, Object> result = new HashMap<>();
            result.put("emailNotification", userSettings.getOrDefault("email_notification", "true"));
            result.put("expirationReminder", userSettings.getOrDefault("expiration_reminder", "true"));
            result.put("reminderDays", userSettings.getOrDefault("reminder_days", "7"));
            
            log.info("获取用户通知设置成功: username={}", currentUser.getUsername());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取通知设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取通知设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取用户个人设置", description = "获取当前登录用户的个人设置")
    public ApiResponse<Map<String, Object>> getUserSettings(@Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        try {
            log.info("获取用户个人设置");
            
            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            Map<String, Object> settings = systemSettingService.getUserSettingsAsMap(currentUser.getUserId());
            
            log.info("获取用户个人设置成功: username={}", currentUser.getUsername());
            return ApiResponse.success(settings);
        } catch (Exception e) {
            log.error("获取用户设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取用户设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户个人设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新用户个人设置", description = "更新当前用户的个人设置")
    public ApiResponse<Boolean> updateUserSettings(@Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId,
                                                   @RequestBody Map<String, Object> params) {
        try {
            log.info("更新用户个人设置: {}", params.keySet());
            
            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(entry.getValue().toString());
                setting.setType("user");
                systemSettingService.saveUserSetting(setting, currentUser.getUserId());
            }
            
            log.info("更新用户个人设置成功: username={}", currentUser.getUsername());
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("更新用户设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新用户设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户个人设置
     */
    @DeleteMapping("/settings/{key}")
    @Operation(summary = "删除用户个人设置", description = "删除当前用户的指定个人设置")
    public ApiResponse<Boolean> deleteUserSetting(@Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId,
                                                  @PathVariable("key") String key) {
        try {
            log.info("删除用户个人设置: key={}", key);
            
            // 获取用户实体信息
            User currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            systemSettingService.deleteUserSetting(key, currentUser.getUserId());
            
            log.info("删除用户个人设置成功: key={}, username={}", key, currentUser.getUsername());
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("删除用户设置异常: key={}", key, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除用户设置失败: " + e.getMessage());
        }
    }
} 