package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实体维护记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("entity_maintenance")
public class EntityMaintenance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实体ID
     */
    private Long entityId;

    /**
     * 维护日期
     */
    private LocalDate maintenanceDate;

    /**
     * 维护类型：repair-维修, clean-清洁, check-检查
     */
    private String maintenanceType;

    /**
     * 维护费用
     */
    private BigDecimal cost;

    /**
     * 维护描述
     */
    private String description;

    /**
     * 结果: success-成功，failure-失败
     */
    private String result;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 