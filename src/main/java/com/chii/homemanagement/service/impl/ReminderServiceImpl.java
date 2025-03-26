package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Item;
import com.chii.homemanagement.entity.ItemLending;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.mapper.ItemLendingMapper;
import com.chii.homemanagement.mapper.ItemMapper;
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
    private ItemMapper itemMapper;

    @Autowired
    private ItemLendingMapper itemLendingMapper;

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
    public List<Reminder> getTodayReminders(Long familyId) {
        return reminderMapper.findRemindersByDate(familyId, LocalDate.now());
    }

    @Override
    public List<Reminder> getRemindersByDateRange(Long familyId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reminder::getFamilyId, familyId)
                .ge(Reminder::getRemindDate, startDate)
                .le(Reminder::getRemindDate, endDate)
                .orderByAsc(Reminder::getRemindDate);
        return list(queryWrapper);
    }

    @Override
    public List<Reminder> getRemindersByItemId(Long itemId) {
        return reminderMapper.findRemindersByItemId(itemId);
    }

    @Override
    public void generateRemindersForItem(Long itemId) {
        // 根据物品ID获取物品信息
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            return;
        }

        // 如果有保修信息，添加保修到期提醒
        if (item.getWarrantyEndDate() != null) {
            // 检查是否已经存在相同的保修到期提醒
            LambdaQueryWrapper<Reminder> warrantyQuery = new LambdaQueryWrapper<>();
            warrantyQuery.eq(Reminder::getItemId, itemId)
                    .eq(Reminder::getType, "warranty")
                    .eq(Reminder::getRemindDate, item.getWarrantyEndDate().minusDays(30)); // 提前30天提醒

            if (count(warrantyQuery) == 0) {
                Reminder warrantyReminder = Reminder.builder()
                        .itemId(itemId)
                        .type("warranty")
                        .title("保修即将到期")
                        .content("物品 [" + item.getName() + "] 的保修将在30天后到期，到期日期：" + item.getWarrantyEndDate())
                        .remindDate(item.getWarrantyEndDate().minusDays(30))
                        .status("pending")
                        .familyId(item.getFamilyId())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                save(warrantyReminder);
            }
        }

        // 获取物品借出记录，生成归还提醒
        List<ItemLending> lendings = itemLendingMapper.findByItemId(itemId);
        for (ItemLending lending : lendings) {
            if ("lending".equals(lending.getStatus()) && lending.getExpectedReturnDate() != null) {
                // 检查是否已经存在相同的归还提醒
                LambdaQueryWrapper<Reminder> returnQuery = new LambdaQueryWrapper<>();
                returnQuery.eq(Reminder::getItemId, itemId)
                        .eq(Reminder::getType, "return")
                        .eq(Reminder::getRemindDate, lending.getExpectedReturnDate().minusDays(1)); // 提前1天提醒

                if (count(returnQuery) == 0) {
                    Reminder returnReminder = Reminder.builder()
                            .itemId(itemId)
                            .type("return")
                            .title("物品归还提醒")
                            .content("物品 [" + item.getName() + "] 借给 [" + lending.getBorrowerName() + "] 需要在 " + lending.getExpectedReturnDate() + " 归还")
                            .remindDate(lending.getExpectedReturnDate().minusDays(1))
                            .status("pending")
                            .familyId(item.getFamilyId())
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    save(returnReminder);
                }
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
    public List<Reminder> getRemindersByStatus(Long familyId, String status) {
        return reminderMapper.findRemindersByStatus(familyId, status);
    }
} 