package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒控制器
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    /**
     * 创建提醒
     */
    @PostMapping
    public ResponseInfo<Reminder> createReminder(@RequestBody Reminder reminder) {
        return ResponseInfo.successResponse(reminderService.createReminder(reminder));
    }

    /**
     * 更新提醒
     */
    @PutMapping("/{id}")
    public ResponseInfo<Reminder> updateReminder(@PathVariable Long id, @RequestBody Reminder reminder) {
        reminder.setId(id);
        return ResponseInfo.successResponse(reminderService.updateReminder(reminder));
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/{id}")
    public ResponseInfo<Boolean> deleteReminder(@PathVariable Long id) {
        return ResponseInfo.successResponse(reminderService.deleteReminder(id));
    }

    /**
     * 获取提醒详情
     */
    @GetMapping("/{id}")
    public ResponseInfo<Reminder> getReminder(@PathVariable Long id) {
        return ResponseInfo.successResponse(reminderService.getReminder(id));
    }

    /**
     * 获取当天提醒
     */
    @GetMapping("/today")
    public ResponseInfo<List<Reminder>> getTodayReminders(@RequestParam Long ownerId) {
        return ResponseInfo.successResponse(reminderService.getTodayReminders(ownerId));
    }

    /**
     * 获取日期范围内的提醒
     */
    @GetMapping("/date-range")
    public ResponseInfo<List<Reminder>> getRemindersByDateRange(
            @RequestParam Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseInfo.successResponse(reminderService.getRemindersByDateRange(ownerId, startDate, endDate));
    }

    /**
     * 获取物品相关的提醒
     */
    @GetMapping("/entity/{entityId}")
    public ResponseInfo<List<Reminder>> getRemindersByItemId(@PathVariable Long entityId) {
        return ResponseInfo.successResponse(reminderService.getRemindersByEntityId(entityId));
    }

    /**
     * 根据物品信息生成提醒
     */
    @PostMapping("/generate/{itemId}")
    public ResponseInfo<Void> generateRemindersForItem(@PathVariable Long itemId) {
        reminderService.generateRemindersForItem(itemId);
        return ResponseInfo.successResponse();
    }

    /**
     * 获取指定状态的提醒
     */
    @GetMapping("/status")
    public ResponseInfo<List<Reminder>> getRemindersByStatus(
            @RequestParam Long ownerId,
            @RequestParam String status) {
        return ResponseInfo.successResponse(reminderService.getRemindersByStatus(ownerId, status));
    }

    /**
     * 处理提醒（标记为已处理）
     */
    @PutMapping("/{id}/process")
    public ResponseInfo<Reminder> processReminder(@PathVariable Long id) {
        Reminder reminder = reminderService.getReminder(id);
        if (reminder != null) {
            reminder.setStatus("processed");
            return ResponseInfo.successResponse(reminderService.updateReminder(reminder));
        }
        return ResponseInfo.errorResponse("提醒不存在");
    }
} 