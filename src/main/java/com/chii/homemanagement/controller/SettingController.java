package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ResponseInfo;
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
    public ResponseInfo<Map<String, Object>> getSystemSettings(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        Map<String, Object> settings = systemSettingService.getSystemSettingsAsMap();
        return ResponseInfo.successResponse(settings);
    }

    /**
     * 更新系统设置
     */
    @PutMapping("/system")
    @Operation(summary = "更新系统设置", description = "更新系统参数设置")
    public ResponseInfo<Boolean> updateSystemSettings(@RequestBody Map<String, Object> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }

        List<SystemSetting> settings = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey(entry.getKey());
            setting.setSettingValue(entry.getValue().toString());
            settings.add(setting);
        }
        
        systemSettingService.saveSystemSettings(settings, currentUser.getId());
        
        return ResponseInfo.successResponse(true);
    }
    
    /**
     * 初始化默认系统设置
     */
    @PostMapping("/system/init")
    @Operation(summary = "初始化默认系统设置", description = "初始化默认的系统参数设置")
    public ResponseInfo<Boolean> initSystemSettings(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }
        
        systemSettingService.initDefaultSystemSettings(currentUser.getId());
        
        return ResponseInfo.successResponse(true);
    }
    
    /**
     * 删除系统设置
     */
    @DeleteMapping("/system/{key}")
    @Operation(summary = "删除系统设置", description = "删除指定的系统参数设置")
    public ResponseInfo<Boolean> deleteSystemSetting(@PathVariable("key") String key, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }
        
        systemSettingService.deleteSystemSetting(key);
        
        return ResponseInfo.successResponse(true);
    }
    
    /**
     * 上传系统Logo
     */
    @PostMapping("/logo")
    @Operation(summary = "上传系统Logo", description = "上传系统Logo图片")
    public ResponseInfo<Map<String, String>> uploadSystemLogo(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }

        // 检查文件是否为空
        if (file.isEmpty()) {
            return ResponseInfo.errorResponse("请选择要上传的文件");
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
            
            return ResponseInfo.successResponse(result);
        } catch (IOException e) {
            return ResponseInfo.errorResponse("Logo上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建系统备份
     */
    @PostMapping("/backup")
    @Operation(summary = "创建系统备份", description = "创建系统数据备份")
    public ResponseInfo<Map<String, Object>> createBackup(@RequestBody(required = false) Map<String, String> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
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
            
            return ResponseInfo.successResponse(result);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("创建备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取备份列表
     */
    @GetMapping("/backup")
    @Operation(summary = "获取备份列表", description = "获取系统备份文件列表")
    public ResponseInfo<List<Map<String, Object>>> getBackups(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
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
            backup2.put("size", 1024 * 512);
            backup2.put("createdAt", System.currentTimeMillis());
            backup2.put("notes", "自动备份");
            
            result.add(backup1);
            result.add(backup2);
            
            return ResponseInfo.successResponse(result);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("获取备份列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复备份
     */
    @PostMapping("/backup/{id}/restore")
    @Operation(summary = "恢复备份", description = "从指定备份恢复系统数据")
    public ResponseInfo<Boolean> restoreBackup(@PathVariable("id") Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }
        
        try {
            // TODO: 实现恢复备份逻辑
            // backupService.restoreBackup(id);
            
            return ResponseInfo.successResponse(true);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("恢复备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除备份
     */
    @DeleteMapping("/backup/{id}")
    @Operation(summary = "删除备份", description = "删除指定的系统备份")
    public ResponseInfo<Boolean> deleteBackup(@PathVariable("id") Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }
        
        try {
            // TODO: 实现删除备份逻辑
            // backupService.deleteBackup(id);
            
            return ResponseInfo.successResponse(true);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("删除备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出数据", description = "导出系统数据")
    public ResponseInfo<byte[]> exportData(@RequestParam(value = "type", defaultValue = "json") String type, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        try {
            // TODO: 实现导出数据逻辑
            // byte[] data = exportService.exportData(type, currentUser.getId());
            
            // 示例数据
            byte[] data = new byte[1024];
            
            return ResponseInfo.successResponse(data);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("导出数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入数据
     */
    @PostMapping("/import")
    @Operation(summary = "导入数据", description = "导入系统数据")
    public ResponseInfo<Boolean> importData(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        // 检查是否有管理员权限
        if (currentUser.getRole() == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseInfo.errorResponse("没有权限执行此操作");
        }
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ResponseInfo.errorResponse("请选择要导入的文件");
        }
        
        try {
            // TODO: 实现导入数据逻辑
            // importService.importData(file.getInputStream(), currentUser.getId());
            
            return ResponseInfo.successResponse(true);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("导入数据失败: " + e.getMessage());
        }
    }
} 