package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
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
    public ApiResponse<Reminder> createReminder(@RequestBody Reminder reminder) {
        return ApiResponse.success(reminderService.createReminder(reminder));
    }

    /**
     * 更新提醒
     */
    @PutMapping("/{id}")
    public ApiResponse<Reminder> updateReminder(@PathVariable Long id, @RequestBody Reminder reminder) {
        reminder.setId(id);
        return ApiResponse.success(reminderService.updateReminder(reminder));
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteReminder(@PathVariable Long id) {
        return ApiResponse.success(reminderService.deleteReminder(id));
    }

    /**
     * 获取提醒详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Reminder> getReminder(@PathVariable Long id) {
        return ApiResponse.success(reminderService.getReminder(id));
    }

    /**
     * 获取当天提醒
     */
    @GetMapping("/today")
    public ApiResponse<List<Reminder>> getTodayReminders(@RequestParam Long userId) {
        return ApiResponse.success(reminderService.getTodayReminders(userId));
    }

    /**
     * 获取日期范围内的提醒
     */
    @GetMapping("/date-range")
    public ApiResponse<List<Reminder>> getRemindersByDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(reminderService.getRemindersByDateRange(userId, startDate, endDate));
    }

    /**
     * 获取物品相关的提醒
     */
    @GetMapping("/entity/{entityId}")
    public ApiResponse<List<Reminder>> getRemindersByItemId(@PathVariable Long entityId) {
        return ApiResponse.success(reminderService.getRemindersByEntityId(entityId));
    }

    /**
     * 根据物品信息生成提醒
     */
    @PostMapping("/generate/{itemId}")
    public ApiResponse<Void> generateRemindersForItem(@PathVariable Long itemId) {
        reminderService.generateRemindersForItem(itemId);
        return ApiResponse.success();
    }

    /**
     * 获取指定状态的提醒
     */
    @GetMapping("/status")
    public ApiResponse<List<Reminder>> getRemindersByStatus(
            @RequestParam Long userId,
            @RequestParam String status) {
        return ApiResponse.success(reminderService.getRemindersByStatus(userId, status));
    }

    /**
     * 处理提醒（标记为已处理）
     */
    @PutMapping("/{id}/process")
    public ApiResponse<Reminder> processReminder(@PathVariable Long id) {
        Reminder reminder = reminderService.getReminder(id);
        if (reminder == null) {
            return ApiResponse.error(ErrorCode.DATA_NOT_EXIST.getCode(), "提醒不存在");
        }
        return ApiResponse.success(reminderService.updateReminder(reminder));
    }
} 