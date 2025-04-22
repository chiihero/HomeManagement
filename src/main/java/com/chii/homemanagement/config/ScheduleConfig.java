package com.chii.homemanagement.config;

import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.ReminderService;
import com.chii.homemanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务配置
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleConfig.class);

    @Autowired
    private ReminderService reminderService;
    
    @Autowired
    private EntityService entityService;
    
    @Autowired
    private UserService userService;

    /**
     * 每天凌晨1点处理提醒状态
     * 将到期提醒的状态从pending更新为sent
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processReminders() {
        reminderService.processExpiredReminders();
    }


    /**
     * 每天凌晨3点执行，检查即将过期和已过期的物品，生成提醒
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void checkExpiringAndExpiredItems() {
        logger.info("开始执行物品过期检查任务");
        
        // 获取所有用户
        List<User> users = userService.getAllUsers();
        
        for (User user : users) {
            try {
                Long userId = user.getUserId();
                logger.info("检查用户 ID: {} 的物品过期情况", userId);
                
                // 检查即将过期的物品（30天内）
                checkExpiringItems(userId, 30);
                
                // 检查已过期的物品
                checkExpiredItems(userId);
                
            } catch (Exception e) {
                logger.error("处理用户 ID: {} 的过期物品时发生错误", user.getUserId(), e);
            }
        }
        
        logger.info("物品过期检查任务执行完成");
    }
    
    /**
     * 检查即将过期的物品并生成提醒
     * 
     * @param userId 用户ID
     * @param days 提前天数
     */
    private void checkExpiringItems(Long userId, int days) {
        List<Entity> expiringEntities = entityService.listExpiringEntities(days, userId);
        logger.info("用户 ID: {} 有 {} 个物品即将在{}天内过期", userId, expiringEntities.size(), days);
        
        for (Entity entity : expiringEntities) {
            // 检查是否已经存在相同类型的提醒
            List<Reminder> existingReminders = reminderService.getRemindersByEntityId(entity.getId());
            boolean hasWarrantyReminder = existingReminders.stream()
                    .anyMatch(r -> "warranty".equals(r.getType()) && 
                              ("pending".equals(r.getStatus()) || "sent".equals(r.getStatus())));
            
            if (!hasWarrantyReminder) {
                // 创建保修到期提醒
                Reminder reminder = Reminder.builder()
                        .entityId(entity.getId())
                        .type("warranty")
                        .content("物品 [" + entity.getName() + "] 的保修将在" + days + "天内到期，到期日期：" + entity.getWarrantyEndDate())
                        .remindDate(LocalDate.now())
                        .status("pending")
                        .userId(userId)
                        .notificationMethods("system")
                        .daysInAdvance(days)
                        .isRecurring(false)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                
                reminderService.createReminder(reminder);
                logger.info("为物品 ID: {} 创建了保修到期提醒", entity.getId());
            }
        }
    }
    
    /**
     * 检查已过期的物品并生成提醒
     * 
     * @param userId 用户ID
     */
    private void checkExpiredItems(Long userId) {
        List<Entity> expiredEntities = entityService.listExpiredEntities(userId);
        logger.info("用户 ID: {} 有 {} 个物品已过期", userId, expiredEntities.size());
        
        for (Entity entity : expiredEntities) {
            // 检查是否已经存在相同类型的提醒
            List<Reminder> existingReminders = reminderService.getRemindersByEntityId(entity.getId());
            boolean hasExpiryReminder = existingReminders.stream()
                    .anyMatch(r -> "expiry".equals(r.getType()) && 
                              ("pending".equals(r.getStatus()) || "sent".equals(r.getStatus())));
            
            if (!hasExpiryReminder) {
                // 创建物品过期提醒
                Reminder reminder = Reminder.builder()
                        .entityId(entity.getId())
                        .type("expiry")
                        .content("物品 [" + entity.getName() + "] 的保修已于 " + entity.getWarrantyEndDate() + " 过期，请注意处理")
                        .remindDate(LocalDate.now())
                        .status("pending")
                        .userId(userId)
                        .notificationMethods("system")
                        .daysInAdvance(0)
                        .isRecurring(false)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                
                reminderService.createReminder(reminder);
                logger.info("为物品 ID: {} 创建了过期提醒", entity.getId());
            }
        }
    }
}