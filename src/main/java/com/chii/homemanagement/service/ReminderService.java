package com.chii.homemanagement.service;

import com.chii.homemanagement.entity.Reminder;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒服务接口
 */
public interface ReminderService {

    /**
     * 创建提醒
     *
     * @param reminder 提醒对象
     * @return 创建后的提醒对象
     */
    Reminder createReminder(Reminder reminder);

    /**
     * 更新提醒
     *
     * @param reminder 提醒对象
     * @return 更新后的提醒对象
     */
    Reminder updateReminder(Reminder reminder);

    /**
     * 删除提醒
     *
     * @param id 提醒ID
     * @return 是否成功
     */
    boolean deleteReminder(Long id);

    /**
     * 根据ID查询提醒
     *
     * @param id 提醒ID
     * @return 提醒对象
     */
    Reminder getReminder(Long id);

    /**
     * 获取当天提醒
     *
     * @param userId 用户ID
     * @return 提醒列表
     */
    List<Reminder> getTodayReminders(Long userId);

    /**
     * 获取最近的提醒
     *
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 提醒列表
     */
    List<Object> getRecentReminders(Long userId, Integer limit);

    /**
     * 获取指定日期范围的提醒
     *
     * @param userId  用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 提醒列表
     */
    List<Reminder> getRemindersByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取实体相关的所有提醒
     *
     * @param entityId 实体ID
     * @return 提醒列表
     */
    List<Reminder> getRemindersByEntityId(Long entityId);

    /**
     * 根据物品信息自动生成提醒
     *
     * @param itemId 物品ID
     */
    void generateRemindersForItem(Long itemId);

    /**
     * 根据实体信息自动生成提醒
     *
     * @param entityId 实体ID
     */
    void generateRemindersForEntity(Long entityId);

    /**
     * 检查并更新过期提醒状态
     */
    void processExpiredReminders();

    /**
     * 按状态查询提醒
     *
     * @param userId 用户ID
     * @param status   状态
     * @return 提醒列表
     */
    List<Reminder> getRemindersByStatus(Long userId, String status);

    /**
     * 处理提醒（标记为已处理）
     *
     * @param reminder 提醒
     * @return 处理后的提醒对象
     */
    Reminder processReminder(Reminder reminder);

    /**
     * 获取提醒列表
     *
     * @param userId     用户ID
     * @param entityId   物品ID
     * @param entityName 物品名称
     * @param type       提醒类型
     * @param status     提醒状态
     * @param page       页码
     * @param size       每页大小
     * @return 提醒列表
     */
    List<Reminder> getReminders(Long userId, Long entityId, String entityName, String type, String status, Integer page, Integer size);
} 