package com.chii.homemanagement.util;

import com.chii.homemanagement.entity.SystemSetting;
import com.chii.homemanagement.service.SystemSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库初始化工具类
 */
@Slf4j
@Component
public class DbInitializer implements CommandLineRunner {

    @Autowired
    private SystemSettingService systemSettingService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查系统设置是否存在
            Map<String, Object> settings = systemSettingService.getSystemSettingsAsMap();
            
            // 如果没有系统设置，则初始化默认设置
            if (settings == null || settings.isEmpty()) {
                log.info("未检测到系统设置，开始初始化默认设置...");
                initDefaultSystemSettings();
                log.info("默认系统设置初始化完成");
            } else {
                log.info("系统设置已存在，无需初始化");
            }
        } catch (Exception e) {
            log.error("初始化系统设置时发生错误", e);
        }
    }
    
    /**
     * 初始化默认系统设置
     */
    private void initDefaultSystemSettings() {
        List<SystemSetting> defaultSettings = new ArrayList<>();
        
        // 语言设置
        SystemSetting language = new SystemSetting();
        language.setType("SYSTEM");
        language.setSettingKey("language");
        language.setSettingValue("zh_CN");
        language.setName("系统语言");
        language.setDefaultValue("zh_CN");
        defaultSettings.add(language);
        
        // 主题设置
        SystemSetting theme = new SystemSetting();
        theme.setType("SYSTEM");
        theme.setSettingKey("theme");
        theme.setSettingValue("light");
        theme.setName("系统主题");
        theme.setDefaultValue("light");
        defaultSettings.add(theme);
        
        // 日期格式
        SystemSetting dateFormat = new SystemSetting();
        dateFormat.setType("SYSTEM");
        dateFormat.setSettingKey("dateFormat");
        dateFormat.setSettingValue("yyyy-MM-dd");
        dateFormat.setName("日期格式");
        dateFormat.setDefaultValue("yyyy-MM-dd");
        defaultSettings.add(dateFormat);
        
        // 货币单位
        SystemSetting currency = new SystemSetting();
        currency.setType("SYSTEM");
        currency.setSettingKey("currency");
        currency.setSettingValue("CNY");
        currency.setName("货币单位");
        currency.setDefaultValue("CNY");
        defaultSettings.add(currency);
        
        // 自动备份
        SystemSetting autoBackup = new SystemSetting();
        autoBackup.setType("SYSTEM");
        autoBackup.setSettingKey("autoBackup");
        autoBackup.setSettingValue("false");
        autoBackup.setName("自动备份");
        autoBackup.setDefaultValue("false");
        defaultSettings.add(autoBackup);
        
        // 备份频率
        SystemSetting backupFrequency = new SystemSetting();
        backupFrequency.setType("SYSTEM");
        backupFrequency.setSettingKey("backupFrequency");
        backupFrequency.setSettingValue("monthly");
        backupFrequency.setName("备份频率");
        backupFrequency.setDefaultValue("monthly");
        defaultSettings.add(backupFrequency);
        
        // 备份时间
        SystemSetting backupTime = new SystemSetting();
        backupTime.setType("SYSTEM");
        backupTime.setSettingKey("backupTime");
        backupTime.setSettingValue("03:00");
        backupTime.setName("备份时间");
        backupTime.setDefaultValue("03:00");
        defaultSettings.add(backupTime);
        
        // 备份保留数量
        SystemSetting backupRetention = new SystemSetting();
        backupRetention.setType("SYSTEM");
        backupRetention.setSettingKey("backupRetention");
        backupRetention.setSettingValue("5");
        backupRetention.setName("备份保留数量");
        backupRetention.setDefaultValue("5");
        defaultSettings.add(backupRetention);
        
        // 系统通知
        SystemSetting systemNotification = new SystemSetting();
        systemNotification.setType("SYSTEM");
        systemNotification.setSettingKey("systemNotification");
        systemNotification.setSettingValue("true");
        systemNotification.setName("系统通知");
        systemNotification.setDefaultValue("true");
        defaultSettings.add(systemNotification);
        
        // 记账提醒
        SystemSetting reminderEnabled = new SystemSetting();
        reminderEnabled.setType("SYSTEM");
        reminderEnabled.setSettingKey("reminderEnabled");
        reminderEnabled.setSettingValue("true");
        reminderEnabled.setName("记账提醒");
        reminderEnabled.setDefaultValue("true");
        defaultSettings.add(reminderEnabled);
        
        // 提醒时间
        SystemSetting reminderTime = new SystemSetting();
        reminderTime.setType("SYSTEM");
        reminderTime.setSettingKey("reminderTime");
        reminderTime.setSettingValue("20:00");
        reminderTime.setName("提醒时间");
        reminderTime.setDefaultValue("20:00");
        defaultSettings.add(reminderTime);
        
        // 会话超时时间
        SystemSetting sessionTimeout = new SystemSetting();
        sessionTimeout.setType("SYSTEM");
        sessionTimeout.setSettingKey("sessionTimeout");
        sessionTimeout.setSettingValue("30");
        sessionTimeout.setName("会话超时时间(分钟)");
        sessionTimeout.setDefaultValue("30");
        defaultSettings.add(sessionTimeout);
        
        // 批量保存默认设置
        Long adminId = 1L; // 假设ID为1的用户是管理员
        systemSettingService.saveSystemSettings(defaultSettings, adminId);
    }
} 