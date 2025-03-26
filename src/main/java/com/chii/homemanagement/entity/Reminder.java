package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class Reminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提醒类型: warranty-保修到期，maintenance-定期维护，return-借用归还，expiry-物品过期
     */
    private String type;

    /**
     * 关联物品ID
     */
    private Long itemId;

    /**
     * 提醒标题
     */
    private String title;

    /**
     * 提醒内容
     */
    private String content;

    /**
     * 提醒日期
     */
    private LocalDate remindDate;

    /**
     * 状态: pending-待提醒，sent-已提醒，processed-已处理
     */
    private String status;

    /**
     * 所属家庭ID
     */
    private Long familyId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 