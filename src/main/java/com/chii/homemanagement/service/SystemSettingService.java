package com.chii.homemanagement.service;

import com.chii.homemanagement.entity.SystemSetting;

import java.util.List;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemSettingService {
    
    /**
     * 获取系统参数设置
     */
    List<SystemSetting> getSystemSettings();
    
    /**
     * 获取用户个人设置
     */
    List<SystemSetting> getUserSettings(Long userId);
    
    /**
     * 保存系统参数设置
     */
    void saveSystemSetting(SystemSetting setting, Long operatorId);
    
    /**
     * 批量保存系统参数设置
     */
    void saveSystemSettings(List<SystemSetting> settings, Long operatorId);
    
    /**
     * 保存用户个人设置
     */
    void saveUserSetting(SystemSetting setting, Long userId);
    
    /**
     * 批量保存用户个人设置
     */
    void saveUserSettings(List<SystemSetting> settings, Long userId);
    
    /**
     * 删除系统参数设置
     */
    void deleteSystemSetting(String key);
    
    /**
     * 删除用户个人设置
     */
    void deleteUserSetting(String key, Long userId);
    
    /**
     * 获取系统参数设置(Map格式)
     */
    Map<String, Object> getSystemSettingsAsMap();
    
    /**
     * 获取用户个人设置(Map格式)
     */
    Map<String, Object> getUserSettingsAsMap(Long userId);
    
    /**
     * 初始化默认系统参数设置
     */
    void initDefaultSystemSettings(Long operatorId);
} 