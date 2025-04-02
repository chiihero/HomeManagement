package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.SystemSetting;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/api/settings")
@Tag(name = "系统设置", description = "系统设置相关接口")
public class SettingController {

    @Autowired
    private SystemSettingService systemSettingService;

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
     * 获取用户个人设置
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户个人设置", description = "获取当前用户的个人设置")
    public ResponseInfo<Map<String, Object>> getUserSettings(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        Map<String, Object> settings = systemSettingService.getUserSettingsAsMap(currentUser.getId());
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
     * 更新用户个人设置
     */
    @PutMapping("/user")
    @Operation(summary = "更新用户个人设置", description = "更新当前用户的个人设置")
    public ResponseInfo<Boolean> updateUserSettings(@RequestBody Map<String, Object> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        List<SystemSetting> settings = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey(entry.getKey());
            setting.setSettingValue(entry.getValue().toString());
            settings.add(setting);
        }
        
        systemSettingService.saveUserSettings(settings, currentUser.getId());
        
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
     * 删除用户个人设置
     */
    @DeleteMapping("/user/{key}")
    @Operation(summary = "删除用户个人设置", description = "删除当前用户的指定个人设置")
    public ResponseInfo<Boolean> deleteUserSetting(@PathVariable("key") String key, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }
        
        systemSettingService.deleteUserSetting(key, currentUser.getId());
        
        return ResponseInfo.successResponse(true);
    }
} 