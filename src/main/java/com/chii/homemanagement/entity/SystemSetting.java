package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
@TableName("system_settings")
@NoArgsConstructor
@Schema(description = "系统设置信息")
public class SystemSetting {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "设置类型：SYSTEM-系统设置, USER-用户个人设置", allowableValues = {"SYSTEM", "USER"}, required = true)
    private String type;

    @Schema(description = "用户ID（对于用户个人设置）")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "设置键", required = true)
    @TableField("setting_key")
    private String settingKey;

    @Schema(description = "设置值", required = true)
    @TableField("setting_value")
    private String settingValue;

    @Schema(description = "设置名称/描述")
    private String name;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "创建人ID")
    @TableField("created_by")
    private Long createdBy;

    @Schema(description = "更新人ID")
    @TableField("updated_by")
    private Long updatedBy;

    @Schema(description = "预设值")
    @TableField("default_value")
    private String defaultValue;
}