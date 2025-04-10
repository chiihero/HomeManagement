package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.mapper.EntityMapper;
import com.chii.homemanagement.mapper.ReminderMapper;
import com.chii.homemanagement.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提醒服务实现类
 */
@Service
public class ReminderServiceImpl extends ServiceImpl<ReminderMapper, Reminder> implements ReminderService {

    @Autowired
    private ReminderMapper reminderMapper;

    @Autowired
    private EntityMapper entityMapper;

    @Override
    public Reminder createReminder(Reminder reminder) {
        reminder.setCreateTime(LocalDateTime.now());
        reminder.setUpdateTime(LocalDateTime.now());
        save(reminder);
        return reminder;
    }

    @Override
    public Reminder updateReminder(Reminder reminder) {
        reminder.setUpdateTime(LocalDateTime.now());
        updateById(reminder);
        return reminder;
    }

    @Override
    public boolean deleteReminder(Long id) {
        return removeById(id);
    }

    @Override
    public Reminder getReminder(Long id) {
        return getById(id);
    }

    @Override
    public List<Reminder> getTodayReminders(Long userId) {
        return reminderMapper.findRemindersByDate(userId, LocalDate.now());
    }

    @Override
    public List<Object> getRecentReminders(Long userId, Integer limit) {
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reminder::getUserId, userId)
                .orderByDesc(Reminder::getCreateTime)
                .last("LIMIT " + limit);
        
        List<Reminder> reminders = list(queryWrapper);
        
        // 转换为前端需要的对象格式
        return reminders.stream().map(reminder -> {
            // 获取关联的物品名称
            String itemName = "";
            if (reminder.getEntityId() != null) {
                Entity entity = entityMapper.selectById(reminder.getEntityId());
                if (entity != null) {
                    itemName = entity.getName();
                }
            }
            
            // 构建返回对象
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", reminder.getId());
            result.put("type", reminder.getType());
            result.put("content", reminder.getContent());
            result.put("reminderDate", reminder.getRemindDate());
            result.put("status", reminder.getStatus());
            result.put("itemId", reminder.getEntityId());
            result.put("itemName", itemName);
            result.put("notificationMethods", reminder.getNotificationMethods());
            result.put("daysInAdvance", reminder.getDaysInAdvance());
            result.put("isRecurring", reminder.getIsRecurring());
            result.put("recurringCycle", reminder.getRecurringCycle());
            
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Reminder> getRemindersByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reminder::getUserId, userId)
                .ge(Reminder::getRemindDate, startDate)
                .le(Reminder::getRemindDate, endDate)
                .orderByAsc(Reminder::getRemindDate);
        return list(queryWrapper);
    }

    @Override
    public List<Reminder> getRemindersByEntityId(Long entityId) {
        return reminderMapper.findRemindersByEntityId(entityId);
    }

    @Override
    public void generateRemindersForItem(Long itemId) {
        // 此方法保留为向后兼容，现在直接调用generateRemindersForEntity
        generateRemindersForEntity(itemId);
    }
    
    @Override
    public void generateRemindersForEntity(Long entityId) {
        // 根据实体ID获取实体信息
        Entity entity = entityMapper.selectById(entityId);
        if (entity == null || !entity.isItem()) {
            return;
        }

        // 如果有保修信息，添加保修到期提醒
        if (entity.getWarrantyEndDate() != null) {
            // 默认提前30天提醒，可以通过系统设置修改
            int daysInAdvance = 30;
            
            // 检查是否已经存在相同的保修到期提醒
            LambdaQueryWrapper<Reminder> warrantyQuery = new LambdaQueryWrapper<>();
            warrantyQuery.eq(Reminder::getEntityId, entityId)
                    .eq(Reminder::getType, "warranty")
                    .eq(Reminder::getRemindDate, entity.getWarrantyEndDate().minusDays(daysInAdvance));

            if (count(warrantyQuery) == 0) {
                Reminder warrantyReminder = Reminder.builder()
                        .entityId(entityId)
                        .type("warranty")
                        .content("物品 [" + entity.getName() + "] 的保修将在" + daysInAdvance + "天后到期，到期日期：" + entity.getWarrantyEndDate())
                        .remindDate(entity.getWarrantyEndDate().minusDays(daysInAdvance))
                        .status("pending")
                        .userId(entity.getUserId())
                        .notificationMethods("system")  // 默认系统通知
                        .daysInAdvance(daysInAdvance)  // 默认提前30天
                        .isRecurring(false)            // 默认不重复
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                save(warrantyReminder);
            }
        }
    }

    @Override
    public void processExpiredReminders() {
        LocalDate today = LocalDate.now();

        // 查询过期但仍为待提醒的提醒
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reminder::getStatus, "pending")
                .lt(Reminder::getRemindDate, today);

        List<Reminder> expiredReminders = list(queryWrapper);

        // 更新过期提醒状态
        for (Reminder reminder : expiredReminders) {
            reminder.setStatus("sent");
            reminder.setUpdateTime(LocalDateTime.now());
            updateById(reminder);
        }
    }

    @Override
    public List<Reminder> getRemindersByStatus(Long userId, String status) {
        return reminderMapper.findRemindersByStatus(userId, status);
    }
    
    @Override
    public Reminder processReminder(Reminder reminder) {

        reminder.setStatus("processed");
        reminder.setUpdateTime(LocalDateTime.now());
        updateById(reminder);
        return reminder;
    }
    
    @Override
    public List<Reminder> getReminders(Long userId, Long entityId, String entityName, String type, String status, Integer page, Integer size) {
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加筛选条件
        if (userId != null) {
            queryWrapper.eq(Reminder::getUserId, userId);
        }
        
        if (entityId != null) {
            queryWrapper.eq(Reminder::getEntityId, entityId);
        }
        
        // 添加按实体名称搜索的功能
        if (StringUtils.hasText(entityName)) {
            // 先查询符合名称条件的实体ID列表
            LambdaQueryWrapper<Entity> entityQueryWrapper = new LambdaQueryWrapper<>();
            entityQueryWrapper.like(Entity::getName, entityName);
            List<Entity> entities = entityMapper.selectList(entityQueryWrapper);
            
            if (!entities.isEmpty()) {
                // 如果找到了匹配的实体，则按这些实体ID进行过滤
                List<Long> entityIds = entities.stream().map(Entity::getId).collect(Collectors.toList());
                queryWrapper.in(Reminder::getEntityId, entityIds);
            } else {
                // 如果没有找到匹配的实体，返回空列表
                return new ArrayList<>();
            }
        }

        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Reminder::getType, type);
        }
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Reminder::getStatus, status);
        }
        
        // 按提醒日期和创建时间排序
        queryWrapper.orderByAsc(Reminder::getRemindDate)
                   .orderByDesc(Reminder::getCreateTime);
        
        // 执行分页查询
        Page<Reminder> reminderPage = new Page<>(page, size);
        Page<Reminder> resultPage = page(reminderPage, queryWrapper);
        
        List<Reminder> reminders = resultPage.getRecords();
        
        // 为每个提醒设置物品名称
        for (Reminder reminder : reminders) {
            if (reminder.getEntityId() != null) {
                Entity entity = entityMapper.selectById(reminder.getEntityId());
                if (entity != null) {
                    reminder.setEntityName(entity.getName());
                }
            }
        }
        
        return reminders;
    }
} 