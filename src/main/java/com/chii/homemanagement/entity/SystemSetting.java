package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
@TableName("system_settings")
@NoArgsConstructor
public class SystemSetting {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 设置类型：SYSTEM-系统设置, USER-用户个人设置
     */
    private String type;
    
    /**
     * 用户ID（对于用户个人设置）
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 设置键
     */
    @TableField("setting_key")
    private String settingKey;
    
    /**
     * 设置值
     */
    @TableField("setting_value")
    private String settingValue;
    
    /**
     * 设置名称/描述
     */
    private String name;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;
    
    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;
    
    /**
     * 预设值
     */
    @TableField("default_value")
    private String defaultValue;
} 