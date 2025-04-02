package com.chii.homemanagement.config;

import com.chii.homemanagement.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private ReminderService reminderService;

    /**
     * 每天凌晨1点处理提醒状态
     * 将到期提醒的状态从pending更新为sent
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processReminders() {
        reminderService.processExpiredReminders();
    }

    /**
     * 每天凌晨2点检查并更新借用状态
     * 将已过期但仍为lending状态的借用记录更新为overdue状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateLendingStatus() {
    }
} 