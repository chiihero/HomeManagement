package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
     * 提醒标题
     */
    @Schema(description = "提醒标题", required = true, example = "保修到期提醒")
    private String title;

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
     * 创建用户ID
     */
    @Schema(description = "创建用户ID")
    private Long createUserId;

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