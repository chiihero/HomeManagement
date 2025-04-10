package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 提醒实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("reminder")
@Schema(description = "提醒信息")
public class Reminder {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 提醒类型: warranty-保修到期，maintenance-定期维护，lending-借用归还，expiry-物品过期，other-其他
     */
    @Schema(description = "提醒类型", allowableValues = {"warranty", "maintenance", "lending", "expiry", "other"}, required = true)
    private String type;

    /**
     * 关联实体ID
     */
    @Schema(description = "关联实体ID", required = true)
    private Long entityId;

    /**
     * 用于返回物品名称（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "物品名称")
    private String entityName;

    /**
     * 提醒内容
     */
    @Schema(description = "提醒内容", example = "您的物品保修即将到期，请及时处理。")
    private String content;

    /**
     * 提醒日期
     */
    @Schema(description = "提醒日期", required = true, example = "2023-01-01")
    private LocalDate remindDate;

    /**
     * 状态: pending-待提醒，sent-已提醒，processed-已处理，ignored-已忽略
     */
    @Schema(description = "提醒状态", allowableValues = {"pending", "sent", "processed", "ignored"}, defaultValue = "pending")
    private String status;

    /**
     * 所属用户ID
     */
    @Schema(description = "所属用户ID", required = true)
    private Long userId;
    
    /**
     * 通知方式: system-系统通知，email-邮箱通知，sms-短信通知，多个用逗号分隔
     */
    @Schema(description = "通知方式", example = "system,email", defaultValue = "system")
    private String notificationMethods;
    
    /**
     * 提前提醒天数
     */
    @Schema(description = "提前提醒天数", example = "3", defaultValue = "0")
    private Integer daysInAdvance;
    
    /**
     * 是否重复提醒
     */
    @Schema(description = "是否重复提醒", example = "false", defaultValue = "false")
    private Boolean isRecurring;
    
    /**
     * 重复周期: daily-每日, weekly-每周, monthly-每月, yearly-每年
     */
    @Schema(description = "重复周期", allowableValues = {"daily", "weekly", "monthly", "yearly"})
    private String recurringCycle;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    private LocalDateTime updateTime;
}