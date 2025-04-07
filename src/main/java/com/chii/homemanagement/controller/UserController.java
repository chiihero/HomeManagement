package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;

import com.chii.homemanagement.entity.SystemSetting;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.SystemSettingService;
import com.chii.homemanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private SystemSettingService systemSettingService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<User> getCurrentUser(HttpSession session) {
        // 从session中获取用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                user = userService.getUserByUsername(username);
                if (user != null) {
                    // 将用户存入session
                    session.setAttribute("user", user);
                    // 清除密码
                    user.setPassword(null);
                }
            }
        } else {
            // 清除密码
            user.setPassword(null);
        }

        if (user == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        return ApiResponse.success(user);
    }

    /**
     * 获取当前用户信息 (适用于前端导航栏和认证检查)
     */
    @PostMapping("/info")
    @Operation(summary = "获取当前用户基本信息", description = "获取当前登录用户的基本信息，用于导航栏显示和认证检查")
    public ApiResponse<Map<String, Object>> getUserInfo() {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            // 准备返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人资料", description = "更新当前登录用户的个人资料")
    public ApiResponse<User> updateProfile(@RequestBody User userParam, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        // 更新个人资料
        User user = new User();
        user.setId(currentUser.getId());
        // 只允许更新以下字段
        user.setNickname(userParam.getNickname());
        user.setEmail(userParam.getEmail());
        user.setPhone(userParam.getPhone());
        user.setUpdateTime(LocalDateTime.now());

        try {
            userService.updateUser(user);
            // 更新session中的用户信息
            User updatedUser = userService.getUserById(currentUser.getId());
            session.setAttribute("user", updatedUser);
            // 清除密码
            updatedUser.setPassword(null);
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    @Operation(summary = "更新密码", description = "更新当前登录用户的密码")
    public ApiResponse<Boolean> updatePassword(@RequestBody Map<String, String> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ApiResponse.error(ErrorCode.PARAM_IS_BLANK.getCode(), "当前密码和新密码不能为空");
        }

        // 验证当前密码
        if (!userService.validatePassword(currentUser.getUsername(), currentPassword)) {
            return ApiResponse.error(ErrorCode.USER_CREDENTIALS_ERROR.getCode(), "当前密码不正确");
        }

        // 更新密码
        User user = new User();
        user.setId(currentUser.getId());
        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());

        try {
            userService.updateUser(user);
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新密码失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传当前登录用户的头像")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.error(ErrorCode.PARAM_IS_BLANK.getCode(), "请选择要上传的文件");
        }

        try {
            // 确保上传目录存在
            File uploadDirFile = new File(uploadDir + "/avatars");
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(uploadDir + "/avatars/" + filename);
            Files.write(filePath, file.getBytes());
            
            // 更新用户头像URL
            User user = new User();
            user.setId(currentUser.getId());
            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            
            userService.updateUser(user);
            
            // 更新session中的用户信息
            User updatedUser = userService.getUserById(currentUser.getId());
            session.setAttribute("user", updatedUser);
            
            // 返回头像URL
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);
            
            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/avatar")
    @Operation(summary = "删除头像", description = "删除当前登录用户的头像")
    public ApiResponse<Boolean> deleteAvatar(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        // 更新用户信息，清空头像URL
        User user = new User();
        user.setId(currentUser.getId());
        user.setAvatar(null);
        user.setUpdateTime(LocalDateTime.now());

        try {
            userService.updateUser(user);
            // 更新session中的用户信息
            User updatedUser = userService.getUserById(currentUser.getId());
            session.setAttribute("user", updatedUser);
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除头像失败: " + e.getMessage());
        }
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/notifications")
    @Operation(summary = "更新通知设置", description = "更新当前登录用户的通知设置")
    public ApiResponse<Boolean> updateNotifications(@RequestBody Map<String, Object> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        try {
            // 保存邮件通知设置
            if (params.containsKey("emailNotification")) {
                SystemSetting emailNotificationSetting = new SystemSetting();
                emailNotificationSetting.setSettingKey("email_notification");
                emailNotificationSetting.setSettingValue(String.valueOf(params.get("emailNotification")));
                emailNotificationSetting.setType("user");
                systemSettingService.saveUserSetting(emailNotificationSetting, currentUser.getId());
            }
            
            // 保存到期提醒设置
            if (params.containsKey("expirationReminder")) {
                SystemSetting expirationReminderSetting = new SystemSetting();
                expirationReminderSetting.setSettingKey("expiration_reminder");
                expirationReminderSetting.setSettingValue(String.valueOf(params.get("expirationReminder")));
                expirationReminderSetting.setType("user");
                systemSettingService.saveUserSetting(expirationReminderSetting, currentUser.getId());
            }
            
            // 保存提前提醒天数设置
            if (params.containsKey("reminderDays")) {
                SystemSetting reminderDaysSetting = new SystemSetting();
                reminderDaysSetting.setSettingKey("reminder_days");
                reminderDaysSetting.setSettingValue(String.valueOf(params.get("reminderDays")));
                reminderDaysSetting.setType("user");
                systemSettingService.saveUserSetting(reminderDaysSetting, currentUser.getId());
            }
            
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新通知设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户通知设置
     */
    @GetMapping("/notifications")
    @Operation(summary = "获取用户通知设置", description = "获取当前登录用户的通知设置")
    public ApiResponse<Map<String, Object>> getNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }
        
        try {
            Map<String, Object> userSettings = systemSettingService.getUserSettingsAsMap(currentUser.getId());
            
            // 如果没有设置，返回默认值
            Map<String, Object> result = new HashMap<>();
            result.put("emailNotification", userSettings.getOrDefault("email_notification", "true"));
            result.put("expirationReminder", userSettings.getOrDefault("expiration_reminder", "true"));
            result.put("reminderDays", userSettings.getOrDefault("reminder_days", "7"));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取通知设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取用户个人设置", description = "获取当前登录用户的个人设置")
    public ApiResponse<Map<String, Object>> getUserSettings(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        Map<String, Object> settings = systemSettingService.getUserSettingsAsMap(currentUser.getId());
        return ApiResponse.success(settings);
    }
    
    /**
     * 更新用户个人设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新用户个人设置", description = "更新当前用户的个人设置")
    public ApiResponse<Boolean> updateUserSettings(@RequestBody Map<String, Object> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }

        try {
            Map<String, Object> existingSettings = systemSettingService.getUserSettingsAsMap(currentUser.getId());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(entry.getValue().toString());
                setting.setType("user");
                systemSettingService.saveUserSetting(setting, currentUser.getId());
            }
            
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新用户设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户个人设置
     */
    @DeleteMapping("/settings/{key}")
    @Operation(summary = "删除用户个人设置", description = "删除当前用户的指定个人设置")
    public ApiResponse<Boolean> deleteUserSetting(@PathVariable("key") String key, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), "未登录或登录已过期");
        }
        
        systemSettingService.deleteUserSetting(key, currentUser.getId());
        
        return ApiResponse.success(true);
    }
} 