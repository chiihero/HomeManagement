package com.chii.homemanagement.service.impl;

import com.chii.homemanagement.entity.SystemSetting;
import com.chii.homemanagement.mapper.SystemSettingMapper;
import com.chii.homemanagement.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现类
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;

    private static final String SYSTEM_TYPE = "SYSTEM";
    private static final String USER_TYPE = "USER";

    @Override
    public List<SystemSetting> getSystemSettings() {
        return systemSettingMapper.findByType(SYSTEM_TYPE);
    }

    @Override
    public List<SystemSetting> getUserSettings(Long userId) {
        return systemSettingMapper.findByTypeAndUserId(USER_TYPE, userId);
    }

    @Override
    @Transactional
    public void saveSystemSetting(SystemSetting setting, Long operatorId) {
        setting.setType(SYSTEM_TYPE);
        setting.setUserId(null);
        setting.setUpdatedBy(operatorId);
        
        if (setting.getId() == null) {
            setting.setCreatedBy(operatorId);
            setting.setCreatedAt(LocalDateTime.now());
        }
        
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingMapper.saveOrUpdate(setting);
    }

    @Override
    @Transactional
    public void saveSystemSettings(List<SystemSetting> settings, Long operatorId) {
        for (SystemSetting setting : settings) {
            saveSystemSetting(setting, operatorId);
        }
    }

    @Override
    @Transactional
    public void saveUserSetting(SystemSetting setting, Long userId) {
        setting.setType(USER_TYPE);
        setting.setUserId(userId);
        setting.setCreatedBy(userId);
        setting.setUpdatedBy(userId);
        
        if (setting.getId() == null) {
            setting.setCreatedAt(LocalDateTime.now());
        }
        
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingMapper.saveOrUpdate(setting);
    }

    @Override
    @Transactional
    public void saveUserSettings(List<SystemSetting> settings, Long userId) {
        for (SystemSetting setting : settings) {
            saveUserSetting(setting, userId);
        }
    }

    @Override
    @Transactional
    public void deleteSystemSetting(String key) {
        systemSettingMapper.deleteSystemSetting(key);
    }

    @Override
    @Transactional
    public void deleteUserSetting(String key, Long userId) {
        systemSettingMapper.deleteUserSetting(userId, key);
    }

    @Override
    public Map<String, Object> getSystemSettingsAsMap() {
        List<SystemSetting> settings = getSystemSettings();
        return convertToMap(settings);
    }

    @Override
    public Map<String, Object> getUserSettingsAsMap(Long userId) {
        List<SystemSetting> settings = getUserSettings(userId);
        return convertToMap(settings);
    }
    
    /**
     * 将设置列表转换为Map
     */
    private Map<String, Object> convertToMap(List<SystemSetting> settings) {
        Map<String, Object> result = new HashMap<>();
        
        for (SystemSetting setting : settings) {
            String key = setting.getSettingKey();
            String value = setting.getSettingValue();
            
            // 尝试将值转换为布尔型或数字
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                result.put(key, Boolean.parseBoolean(value));
            } else {
                try {
                    // 尝试转换为整数
                    if (value.matches("^\\d+$")) {
                        result.put(key, Integer.parseInt(value));
                    } else if (value.matches("^\\d+\\.\\d+$")) {
                        // 尝试转换为浮点数
                        result.put(key, Double.parseDouble(value));
                    } else {
                        // 保持字符串
                        result.put(key, value);
                    }
                } catch (NumberFormatException e) {
                    // 如果转换失败，则保持字符串
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public void initDefaultSystemSettings(Long operatorId) {
        // 默认系统设置
        List<SystemSetting> defaultSettings = new ArrayList<>();
        
        // 语言设置
        SystemSetting language = new SystemSetting();
        language.setSettingKey("language");
        language.setSettingValue("zh_CN");
        language.setName("系统语言");
        language.setDefaultValue("zh_CN");
        defaultSettings.add(language);
        
        // 主题设置
        SystemSetting theme = new SystemSetting();
        theme.setSettingKey("theme");
        theme.setSettingValue("light");
        theme.setName("系统主题");
        theme.setDefaultValue("light");
        defaultSettings.add(theme);
        
        // 日期格式
        SystemSetting dateFormat = new SystemSetting();
        dateFormat.setSettingKey("dateFormat");
        dateFormat.setSettingValue("yyyy-MM-dd");
        dateFormat.setName("日期格式");
        dateFormat.setDefaultValue("yyyy-MM-dd");
        defaultSettings.add(dateFormat);
        
        // 货币单位
        SystemSetting currency = new SystemSetting();
        currency.setSettingKey("currency");
        currency.setSettingValue("CNY");
        currency.setName("货币单位");
        currency.setDefaultValue("CNY");
        defaultSettings.add(currency);
        
        // 自动备份
        SystemSetting autoBackup = new SystemSetting();
        autoBackup.setSettingKey("autoBackup");
        autoBackup.setSettingValue("false");
        autoBackup.setName("自动备份");
        autoBackup.setDefaultValue("false");
        defaultSettings.add(autoBackup);
        
        // 备份频率
        SystemSetting backupFrequency = new SystemSetting();
        backupFrequency.setSettingKey("backupFrequency");
        backupFrequency.setSettingValue("monthly");
        backupFrequency.setName("备份频率");
        backupFrequency.setDefaultValue("monthly");
        defaultSettings.add(backupFrequency);
        
        // 系统通知
        SystemSetting systemNotification = new SystemSetting();
        systemNotification.setSettingKey("systemNotification");
        systemNotification.setSettingValue("true");
        systemNotification.setName("系统通知");
        systemNotification.setDefaultValue("true");
        defaultSettings.add(systemNotification);
        
        // 记账提醒
        SystemSetting reminderEnabled = new SystemSetting();
        reminderEnabled.setSettingKey("reminderEnabled");
        reminderEnabled.setSettingValue("true");
        reminderEnabled.setName("记账提醒");
        reminderEnabled.setDefaultValue("true");
        defaultSettings.add(reminderEnabled);
        
        // 提醒时间
        SystemSetting reminderTime = new SystemSetting();
        reminderTime.setSettingKey("reminderTime");
        reminderTime.setSettingValue("20:00");
        reminderTime.setName("提醒时间");
        reminderTime.setDefaultValue("20:00");
        defaultSettings.add(reminderTime);
        
        // 保存所有默认设置
        saveSystemSettings(defaultSettings, operatorId);
    }
} 