package com.chii.homemanagement.mapper;

import com.chii.homemanagement.entity.SystemSetting;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统设置数据访问层
 */
@Mapper
@Repository
public interface SystemSettingMapper {
    
    /**
     * 根据类型和用户ID获取设置列表
     */
    @Select("SELECT * FROM system_settings WHERE type = #{type} AND (user_id = #{userId} OR user_id IS NULL)")
    List<SystemSetting> findByTypeAndUserId(@Param("type") String type, @Param("userId") Long userId);
    
    /**
     * 根据类型获取设置列表（仅系统设置）
     */
    @Select("SELECT * FROM system_settings WHERE type = #{type} AND user_id IS NULL")
    List<SystemSetting> findByType(@Param("type") String type);
    
    /**
     * 根据类型、键和用户ID获取设置
     */
    @Select("SELECT * FROM system_settings WHERE type = #{type} AND setting_key = #{key} AND user_id = #{userId}")
    SystemSetting findByTypeAndKeyAndUserId(@Param("type") String type, @Param("key") String key, @Param("userId") Long userId);
    
    /**
     * 根据类型和键获取系统设置
     */
    @Select("SELECT * FROM system_settings WHERE type = #{type} AND setting_key = #{key} AND user_id IS NULL")
    SystemSetting findByTypeAndKey(@Param("type") String type, @Param("key") String key);
    
    /**
     * 保存或更新设置
     */
    @Insert("INSERT INTO system_settings(type, user_id, setting_key, setting_value, name, created_at, updated_at, created_by, updated_by, default_value) " +
            "VALUES(#{type}, #{userId}, #{settingKey}, #{settingValue}, #{name}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy}, #{defaultValue}) " +
            "ON DUPLICATE KEY UPDATE setting_value = #{settingValue}, updated_at = #{updatedAt}, updated_by = #{updatedBy}")
    void saveOrUpdate(SystemSetting setting);
    
    /**
     * 删除用户设置
     */
    @Delete("DELETE FROM system_settings WHERE user_id = #{userId} AND setting_key = #{key}")
    void deleteUserSetting(@Param("userId") Long userId, @Param("key") String key);
    
    /**
     * 删除系统设置
     */
    @Delete("DELETE FROM system_settings WHERE user_id IS NULL AND setting_key = #{key}")
    void deleteSystemSetting(@Param("key") String key);
} 