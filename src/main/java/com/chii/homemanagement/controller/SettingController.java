package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.entity.SystemSetting;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/api/settings")
@Tag(name = "系统设置", description = "系统设置相关接口")
public class SettingController {

    @Autowired
    private SystemSettingService systemSettingService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 获取系统设置
     */
    @GetMapping("/system")
    @Operation(summary = "获取系统设置", description = "获取系统参数设置")
    public ApiResponse<Map<String, Object>> getSystemSettings(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }

        Map<String, Object> settings = systemSettingService.getSystemSettingsAsMap();
        return ApiResponse.success(settings);
    }

    /**
     * 更新系统设置
     */
    @PutMapping("/system")
    @Operation(summary = "更新系统设置", description = "更新系统参数设置")
    public ApiResponse<Boolean> updateSystemSettings(@RequestBody Map<String, Object> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
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
        
        systemSettingService.saveSystemSettings(settings, currentUser.getId());
        
        return ApiResponse.success(true);
    }
    
    /**
     * 初始化默认系统设置
     */
    @PostMapping("/system/init")
    @Operation(summary = "初始化默认系统设置", description = "初始化默认的系统参数设置")
    public ApiResponse<Boolean> initSystemSettings(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        systemSettingService.initDefaultSystemSettings(currentUser.getId());
        
        return ApiResponse.success(true);
    }
    
    /**
     * 删除系统设置
     */
    @DeleteMapping("/system/{key}")
    @Operation(summary = "删除系统设置", description = "删除指定的系统参数设置")
    public ApiResponse<Boolean> deleteSystemSetting(@PathVariable("key") String key, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        systemSettingService.deleteSystemSetting(key);
        
        return ApiResponse.success(true);
    }
    
    /**
     * 上传系统Logo
     */
    @PostMapping("/logo")
    @Operation(summary = "上传系统Logo", description = "上传系统Logo图片")
    public ApiResponse<Map<String, String>> uploadSystemLogo(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }

        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_VALID.getCode(), "请选择要上传的文件");
        }

        try {
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
            systemSettingService.saveSystemSettings(settings, currentUser.getId());
            
            // 返回Logo URL
            Map<String, String> result = new HashMap<>();
            result.put("url", logoUrl);
            
            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "Logo上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建系统备份
     */
    @PostMapping("/backup")
    @Operation(summary = "创建系统备份", description = "创建系统数据备份")
    public ApiResponse<Map<String, Object>> createBackup(@RequestBody(required = false) Map<String, String> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        try {
            String notes = params != null ? params.get("notes") : null;
            
            // TODO: 实现备份逻辑
            // String backupFileName = backupService.createBackup(notes);
            
            Map<String, Object> result = new HashMap<>();
            result.put("filename", "backup_" + System.currentTimeMillis() + ".zip");
            result.put("size", 1024 * 1024); // 示例大小，1MB
            result.put("createdAt", System.currentTimeMillis());
            result.put("notes", notes);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "创建备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取备份列表
     */
    @GetMapping("/backup")
    @Operation(summary = "获取备份列表", description = "获取系统备份文件列表")
    public ApiResponse<List<Map<String, Object>>> getBackups(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        try {
            // TODO: 实现获取备份列表逻辑
            // List<BackupInfo> backups = backupService.getBackups();
            
            List<Map<String, Object>> result = new ArrayList<>();
            // 示例数据
            Map<String, Object> backup1 = new HashMap<>();
            backup1.put("id", 1);
            backup1.put("filename", "backup_20230101120000.zip");
            backup1.put("size", 1024 * 1024);
            backup1.put("createdAt", System.currentTimeMillis() - 86400000); // 1天前
            backup1.put("notes", "手动备份");
            
            Map<String, Object> backup2 = new HashMap<>();
            backup2.put("id", 2);
            backup2.put("filename", "backup_20230102120000.zip");
            backup2.put("size", 2 * 1024 * 1024);
            backup2.put("createdAt", System.currentTimeMillis());
            backup2.put("notes", "自动备份");
            
            result.add(backup1);
            result.add(backup2);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取备份列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复备份
     */
    @PostMapping("/backup/{id}/restore")
    @Operation(summary = "恢复备份", description = "从指定备份恢复系统数据")
    public ApiResponse<Boolean> restoreBackup(@PathVariable("id") Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        try {
            // TODO: 实现恢复备份逻辑
            // boolean success = backupService.restoreBackup(id);
            boolean success = true; // 模拟成功
            
            return ApiResponse.success(success);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "恢复备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除备份
     */
    @DeleteMapping("/backup/{id}")
    @Operation(summary = "删除备份", description = "删除指定的系统备份")
    public ApiResponse<Boolean> deleteBackup(@PathVariable("id") Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        try {
            // TODO: 实现删除备份逻辑
            // boolean success = backupService.deleteBackup(id);
            boolean success = true; // 模拟成功
            
            return ApiResponse.success(success);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出数据", description = "导出系统数据")
    public ApiResponse<byte[]> exportData(@RequestParam(value = "type", defaultValue = "json") String type, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        try {
            // TODO: 实现数据导出逻辑
            // byte[] exportedData = exportService.exportData(type);
            byte[] exportedData = "模拟导出数据".getBytes(); // 模拟导出数据
            
            return ApiResponse.success(exportedData);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "导出数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入数据
     */
    @PostMapping("/import")
    @Operation(summary = "导入数据", description = "导入系统数据")
    public ApiResponse<Boolean> importData(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ApiResponse.error(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getMessage());
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage());
        }
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_VALID.getCode(), "请选择要导入的文件");
        }
        
        try {
            // TODO: 实现数据导入逻辑
            // boolean success = importService.importData(file);
            boolean success = true; // 模拟成功
            
            return ApiResponse.success(success);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "导入数据失败: " + e.getMessage());
        }
    }
} 