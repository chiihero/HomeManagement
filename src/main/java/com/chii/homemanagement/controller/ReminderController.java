package com.chii.homemanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 提醒控制器
 */
@Tag(name = "提醒管理", description = "提醒相关接口")
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    @Autowired
    private ReminderService reminderService;
    /**
     * 获取提醒列表
     */
    @Operation(summary = "获取提醒列表", description = "获取提醒列表，支持分页和筛选")
    @GetMapping("/page")
    public ApiResponse<IPage<Reminder>> pageReminders(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "物品ID") @RequestParam(required = false) Long entityId,
            @Parameter(description = "物品名称") @RequestParam(required = false) String entityName,
            @Parameter(description = "提醒类型") @RequestParam(required = false) String type,
            @Parameter(description = "提醒状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        logger.info("Fetching reminders with filters: userId={}, entityId={}, entityName={}, type={}, status={}, current={}, size={}",
                userId, entityId, entityName, type, status, current, size);
        // 构建分页对象
        Page<Reminder> page = new Page<>(current, size);
        IPage<Reminder> result = reminderService.pageReminders(page,userId, entityId,entityName , type, status);

        return ApiResponse.success(result);

    }
    /**
     * 获取提醒列表
     */
    @Operation(summary = "获取提醒列表", description = "获取提醒列表")
    @GetMapping
    public ApiResponse<List<Reminder>> getReminders(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "物品ID") @RequestParam(required = false) Long entityId,
            @Parameter(description = "物品名称") @RequestParam(required = false) String entityName,
            @Parameter(description = "提醒类型") @RequestParam(required = false) String type,
            @Parameter(description = "提醒状态") @RequestParam(required = false) String status){
        logger.info("Fetching reminders with filters: userId={}, entityId={}, entityName={}, type={}, status={}, current={}, size={}",
                userId, entityId, entityName, type, status);
        return ApiResponse.success( reminderService.getReminders(userId,entityId,entityName , type, status));

    }
    /**
     * 创建提醒
     */
    @Operation(summary = "创建提醒", description = "创建一个新的提醒")
    @PostMapping
    public ApiResponse<Reminder> createReminder(@RequestBody Reminder reminder) {
        logger.info("Creating reminder: {}", reminder);
        return ApiResponse.success(reminderService.createReminder(reminder));
    }

    /**
     * 更新提醒
     */
    @Operation(summary = "更新提醒", description = "根据ID更新提醒信息")
    @PutMapping("/{id}")
    public ApiResponse<Reminder> updateReminder(@PathVariable Long id, @RequestBody Reminder reminder) {
        logger.info("Updating reminder with ID: {}", id);
        reminder.setId(id);
        return ApiResponse.success(reminderService.updateReminder(reminder));
    }

    /**
     * 删除提醒
     */
    @Operation(summary = "删除提醒", description = "根据ID删除提醒")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteReminder(@PathVariable Long id) {
        logger.info("Deleting reminder with ID: {}", id);
        return ApiResponse.success(reminderService.deleteReminder(id));
    }

    /**
     * 获取提醒详情
     */
    @Operation(summary = "获取提醒详情", description = "根据ID获取提醒的详细信息")
    @GetMapping("/{id}")
    public ApiResponse<Reminder> getReminder(@PathVariable Long id) {
        logger.info("Fetching reminder with ID: {}", id);
        return ApiResponse.success(reminderService.getReminder(id));
    }


   
    /**
     * 获取物品相关的提醒
     */
    @Operation(summary = "获取物品相关的提醒", description = "根据物品ID获取相关的提醒列表")
    @GetMapping("/entity/{entityId}")
    public ApiResponse<List<Reminder>> getRemindersByItemId(@PathVariable Long entityId) {
        logger.info("Fetching reminders for entity ID: {}", entityId);
        return ApiResponse.success(reminderService.getRemindersByEntityId(entityId));
    }

    /**
     * 根据物品信息生成提醒
     */
    @Operation(summary = "根据物品信息生成提醒", description = "根据物品ID生成相关的提醒")
    @PostMapping("/generate/{itemId}")
    public ApiResponse<Void> generateRemindersForItem(@PathVariable Long itemId) {
        logger.info("Generating reminders for item ID: {}", itemId);
        reminderService.generateRemindersForItem(itemId);
        return ApiResponse.success();
    }

   
    /**
     * 处理提醒（标记为已处理）
     */
    @Operation(summary = "处理提醒", description = "根据ID标记提醒为已处理")
    @PutMapping("/{id}/process")
    public ApiResponse<Reminder> processReminder(@PathVariable Long id) {
        logger.info("Processing reminder with ID: {}", id);
        Reminder reminder = reminderService.getReminder(id);
        if (reminder == null) {
            logger.warn("Reminder with ID: {} not found", id);
            return ApiResponse.error(ErrorCode.DATA_NOT_EXIST.getCode(), "提醒不存在");
        }
        return ApiResponse.success(reminderService.processReminder(reminder));
    }


}