package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;

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
     * @param userId 用户ID
     * @param date   日期
     * @return 提醒列表
     */
    @Select("SELECT * FROM reminder WHERE user_id = #{userId} AND remind_date = #{date} ORDER BY id DESC")
    List<Reminder> findRemindersByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询某个状态的提醒
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 提醒列表
     */
    @Select("SELECT * FROM reminder WHERE user_id = #{userId} AND status = #{status} ORDER BY remind_date ASC, id DESC")
    List<Reminder> findRemindersByStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询实体相关的所有提醒
     *
     * @param entityId 实体ID
     * @return 提醒列表
     */
    @Select("SELECT * FROM reminder WHERE entity_id = #{entityId} ORDER BY remind_date ASC, id DESC")
    List<Reminder> findRemindersByEntityId(@Param("entityId") Long entityId);
} 