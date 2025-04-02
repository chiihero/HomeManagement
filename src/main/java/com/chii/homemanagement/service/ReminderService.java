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
     * @param ownerId 所有者ID
     * @return 提醒列表
     */
    List<Reminder> getTodayReminders(Long ownerId);

    /**
     * 获取指定日期范围的提醒
     *
     * @param ownerId  所有者ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 提醒列表
     */
    List<Reminder> getRemindersByDateRange(Long ownerId, LocalDate startDate, LocalDate endDate);


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
     * @param ownerId 所有者ID
     * @param status   状态
     * @return 提醒列表
     */
    List<Reminder> getRemindersByStatus(Long ownerId, String status);
} 