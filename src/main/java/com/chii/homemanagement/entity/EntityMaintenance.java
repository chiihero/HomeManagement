package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "实体维护记录信息")
public class EntityMaintenance {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 实体ID
     */
    @Schema(description = "实体ID", required = true)
    private Long entityId;

    /**
     * 维护日期
     */
    @Schema(description = "维护日期", required = true, example = "2023-01-01")
    private LocalDate maintenanceDate;

    /**
     * 维护类型：repair-维修, clean-清洁, check-检查
     */
    @Schema(description = "维护类型", allowableValues = {"repair", "clean", "check"}, required = true)
    private String maintenanceType;

    /**
     * 维护费用
     */
    @Schema(description = "维护费用", example = "100.00")
    private BigDecimal cost;

    /**
     * 维护描述
     */
    @Schema(description = "维护描述", example = "例行检查")
    private String description;

    /**
     * 结果: success-成功，failure-失败
     */
    @Schema(description = "维护结果", allowableValues = {"success", "failure"}, defaultValue = "success")
    private String result;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID", required = true)
    private Long operatorId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;
}