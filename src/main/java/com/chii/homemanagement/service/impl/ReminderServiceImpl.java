package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.mapper.EntityMapper;
import com.chii.homemanagement.mapper.ReminderMapper;
import com.chii.homemanagement.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    public List<Reminder> getTodayReminders(Long ownerId) {
        return reminderMapper.findRemindersByDate(ownerId, LocalDate.now());
    }

    @Override
    public List<Reminder> getRemindersByDateRange(Long ownerId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reminder::getOwnerId, ownerId)
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
            // 检查是否已经存在相同的保修到期提醒
            LambdaQueryWrapper<Reminder> warrantyQuery = new LambdaQueryWrapper<>();
            warrantyQuery.eq(Reminder::getEntityId, entityId)
                    .eq(Reminder::getType, "warranty")
                    .eq(Reminder::getRemindDate, entity.getWarrantyEndDate().minusDays(30)); // 提前30天提醒

            if (count(warrantyQuery) == 0) {
                Reminder warrantyReminder = Reminder.builder()
                        .entityId(entityId)
                        .type("warranty")
                        .title("保修即将到期")
                        .content("物品 [" + entity.getName() + "] 的保修将在30天后到期，到期日期：" + entity.getWarrantyEndDate())
                        .remindDate(entity.getWarrantyEndDate().minusDays(30))
                        .status("pending")
                        .ownerId(entity.getOwnerId())
                        .createUserId(entity.getCreateUserId())
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
    public List<Reminder> getRemindersByStatus(Long ownerId, String status) {
        return reminderMapper.findRemindersByStatus(ownerId, status);
    }
} 