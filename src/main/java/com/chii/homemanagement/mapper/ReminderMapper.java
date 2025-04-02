package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒Mapper接口
 */
@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {

    /**
     * 查询指定日期的提醒
     *
     * @param ownerId 所有者ID
     * @param date     日期
     * @return 提醒列表
     */
    List<Reminder> findRemindersByDate(@Param("ownerId") Long ownerId, @Param("date") LocalDate date);

    /**
     * 查询某个状态的提醒
     *
     * @param ownerId 所有者ID
     * @param status   状态
     * @return 提醒列表
     */
    List<Reminder> findRemindersByStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    /**
     * 查询实体相关的所有提醒
     *
     * @param entityId 实体ID
     * @return 提醒列表
     */
    List<Reminder> findRemindersByEntityId(@Param("entityId") Long entityId);
} 