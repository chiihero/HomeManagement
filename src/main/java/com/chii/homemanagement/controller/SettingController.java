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
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/api/settings")
@Tag(name = "系统设置", description = "系统设置相关接口")
@Slf4j
public class SettingController {

    @Autowired
    private SystemSettingService systemSettingService;
    @Autowired
    private UserService userService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 获取系统设置
     */
    @GetMapping("/system")
    @Operation(summary = "获取系统设置", description = "获取系统参数设置")
    public ApiResponse<Map<String, Object>> getSystemSettings() {
        try {
            log.info("获取系统设置");
            
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }

            Map<String, Object> settings = systemSettingService.getSystemSettingsAsMap();
            return ApiResponse.success(settings);
        } catch (Exception e) {
            log.error("获取系统设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取系统设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统设置
     */
    @PutMapping("/system")
    @Operation(summary = "更新系统设置", description = "更新系统参数设置")
    public ApiResponse<Boolean> updateSystemSettings(@RequestBody Map<String, Object> params) {
        try {
            log.info("更新系统设置: {}", params.keySet());
            
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            // 检查是否有管理员权限
            if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
                return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
            }

            List<SystemSetting> settings = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(entry.getValue().toString());
                settings.add(setting);
            }
            
            systemSettingService.saveSystemSettings(settings, currentUser.getUserId());
            
            log.info("更新系统设置成功");
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("更新系统设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新系统设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化默认系统设置
     */
    @PostMapping("/system/init")
    @Operation(summary = "初始化默认系统设置", description = "初始化默认的系统参数设置")
    public ApiResponse<Boolean> initSystemSettings() {
        try {
            log.info("初始化默认系统设置");
            
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            // 检查是否有管理员权限
            if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
                return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
            }
            
            systemSettingService.initDefaultSystemSettings(currentUser.getUserId());
            
            log.info("初始化默认系统设置成功");
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("初始化默认系统设置异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "初始化默认系统设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除系统设置
     */
    @DeleteMapping("/system/{key}")
    @Operation(summary = "删除系统设置", description = "删除指定的系统参数设置")
    public ApiResponse<Boolean> deleteSystemSetting(@PathVariable("key") String key) {
        try {
            log.info("删除系统设置: key={}", key);
            
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            // 检查是否有管理员权限
            if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
                return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
            }
            
            systemSettingService.deleteSystemSetting(key);
            
            log.info("删除系统设置成功: key={}", key);
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("删除系统设置异常: key={}", key, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除系统设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传系统Logo
     */
    @PostMapping("/logo")
    @Operation(summary = "上传系统Logo", description = "上传系统Logo图片")
    public ApiResponse<Map<String, String>> uploadSystemLogo(@RequestParam("file") MultipartFile file) {
        try {
            log.info("上传系统Logo");
            
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), "用户不存在");
            }
            
            // 检查是否有管理员权限
            if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
                return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
            }

            // 检查文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.error(ErrorCode.PARAM_NOT_VALID.getCode(), "请选择要上传的文件");
            }

            // 确保上传目录存在
            File uploadDirFile = new File(uploadDir + "/system");
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "logo" + extension;
            
            // 保存文件
            Path filePath = Paths.get(uploadDir + "/system/" + filename);
            Files.write(filePath, file.getBytes());
            
            // 更新系统Logo设置
            SystemSetting logoSetting = new SystemSetting();
            logoSetting.setSettingKey("system_logo");
            String logoUrl = "/uploads/system/" + filename;
            logoSetting.setSettingValue(logoUrl);
            
            List<SystemSetting> settings = new ArrayList<>();
            settings.add(logoSetting);
            systemSettingService.saveSystemSettings(settings, currentUser.getUserId());
            
            // 返回Logo URL
            Map<String, String> result = new HashMap<>();
            result.put("url", logoUrl);
            
            log.info("上传系统Logo成功: {}", logoUrl);
            return ApiResponse.success(result);
        } catch (IOException e) {
            log.error("上传系统Logo异常: ", e);
            return ApiResponse.error(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "Logo上传失败: " + e.getMessage());
        }
    }
    
} 